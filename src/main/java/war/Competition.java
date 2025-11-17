package war;

import cli.Options;
import memory.MemoryEventListener;
import utils.EventMulticaster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Competition {
    public final static int MAXIMUM_SPEED = -1;
    public final static int MAX_ROUND = 200000;
    private final WarriorRepository warriorRepository;
    private final Options options;

    private CompetitionIterator competitionIterator;
    private EventMulticaster competitionEventCaster, memoryEventCaster;
    private CompetitionEventListener competitionEventListener;
    private MemoryEventListener memoryEventListener;

    private ExecutorService executorService;
    private War currentWar;
    private long seed = 0;
    private boolean abort;
    private int speed;
    private static final long DELAY_UNIT = 200;

    private final List<WarResult> warResults = new ArrayList<>();
    private final AtomicInteger warIdCounter = new AtomicInteger(0);
    private WarEventSocketServer socketServer;

    public Competition(Options options) throws IOException {
        this(true, options);
    }

    public Competition(boolean shouldReadWarriorsFile, Options options) throws IOException {
        warriorRepository = new WarriorRepository(shouldReadWarriorsFile, options);
        this.options = options;
        this.speed = MAXIMUM_SPEED;
        this.abort = false;

        competitionEventCaster = new EventMulticaster(CompetitionEventListener.class);
        competitionEventListener = (CompetitionEventListener) competitionEventCaster.getProxy();
        memoryEventCaster = new EventMulticaster(MemoryEventListener.class);
        memoryEventListener = (MemoryEventListener) memoryEventCaster.getProxy();
    }

    public void startWebSocketServer() {
        if (socketServer == null && options.headless) {
            socketServer = new WarEventSocketServer(8887);
            socketServer.start();
        }
    }

    public void shutdown() {
        try {
            if (socketServer != null) {
                socketServer.stop();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public List<WarResult> getWarResults() { return warResults; }

    public void runCompetition(int warsPerCombination, int warriorsPerGroup, boolean startPaused) throws Exception {
        competitionIterator = new CompetitionIterator(warriorRepository.getNumberOfGroups(), warriorsPerGroup);
        competitionEventListener.onCompetitionStart();

        int totalWars = getTotalNumberOfWars();
        for (int warCount = 0; warCount < totalWars; warCount++) {
            if (abort) break;
            WarriorGroup[] groups = warriorRepository.createGroupList(competitionIterator.next());
            runWar(groups, startPaused);
            this.seed++;
        }

        competitionEventListener.onCompetitionEnd();
        warriorRepository.saveScoresToFile(options.outputFile);
    }

    public void runCompetitionInParallel(int warsPerCombination, int warriorsPerGroup, int threads) throws InterruptedException {
        competitionIterator = new CompetitionIterator(warriorRepository.getNumberOfGroups(), warriorsPerGroup);
        competitionEventListener.onCompetitionStart();
        executorService = Executors.newFixedThreadPool(threads);

        int totalWars = getTotalNumberOfWars();
        for (int i = 0; i < totalWars; i++) {
            if (abort) break;
            WarriorGroup[] groups = warriorRepository.createGroupList(competitionIterator.next());
            long warSeed = this.seed + i;
            executorService.submit(() -> {
                try {
                    runWarInParallel(groups, warSeed);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);

        competitionEventListener.onCompetitionEnd();
        warriorRepository.saveScoresToFile(options.outputFile);
    }

    public void runWar(WarriorGroup[] warriorGroups, boolean startPaused) throws Exception {
        int warId = warIdCounter.getAndIncrement();
        currentWar = new War(memoryEventListener, competitionEventListener, startPaused, options);
        currentWar.setSeed(this.seed);

        competitionEventListener.onWarStart(currentWar.getSeed());
        currentWar.loadWarriorGroups(warriorGroups);

        int round = 0;
        while (round < MAX_ROUND && !currentWar.isOver() && !abort) {
            currentWar.setCurrentRound(round);
            competitionEventListener.onRound(round);

            if (!startPaused) {
                if (speed != MAXIMUM_SPEED && round > 0 && round % speed == 0) Thread.sleep(DELAY_UNIT);
                while (currentWar.isPaused()) Thread.sleep(DELAY_UNIT);
                if (currentWar.isSingleRound()) currentWar.pause();
                currentWar.nextRound();
            } else {
                 while (currentWar.isPaused()) Thread.sleep(DELAY_UNIT);
                 if (currentWar.isSingleRound()) currentWar.pause();
                 if (currentWar.isOver()) break;
                 currentWar.nextRound();
            }
            competitionEventListener.onEndRound();
            round++;
        }
        currentWar.setCurrentRound(round);

        int numAlive = currentWar.getNumRemainingSurvivors();
        String[] winners = currentWar.getRemainingWarriorNames().split(", ");
        float score = (numAlive > 0) ? 1.0f / numAlive : 0;
        warResults.add(new WarResult(warId, currentWar.getSeed(), winners, warriorGroups,
            currentWar.getKills(), currentWar.getCloseCalls(),
            currentWar.getZombieTilesOverwritten(), currentWar.getPlayerTilesOverwritten(), round));
        
        competitionEventListener.onWarEnd(0, String.join(", ", winners));
        currentWar.updateScores(warriorRepository);
        currentWar = null;
    }

    private void runWarInParallel(WarriorGroup[] warriorGroups, long seed) throws Exception {
        int warId = warIdCounter.getAndIncrement();
        // --- FIX: Pass the main competition listener directly, not a proxy ---
        War war = new War(null, this.competitionEventListener, false, options);
        war.setSeed(seed);

        competitionEventListener.onWarStart(seed);
        war.loadWarriorGroups(warriorGroups);

        int round = 0;
        while (round < MAX_ROUND && !war.isOver()) {
            war.setCurrentRound(round);
            war.nextRound();
            round++;
        }
        war.setCurrentRound(round);

        int numAlive = war.getNumRemainingSurvivors();
        String[] winners = war.getRemainingWarriorNames().split(", ");
        if (winners.length == 1 && winners[0].isEmpty()) winners = new String[0];
        float score = (numAlive > 0) ? 1.0f / numAlive : 0;

        synchronized(warResults) {
            warResults.add(new WarResult(warId, seed, winners, warriorGroups,
                war.getKills(), war.getCloseCalls(),
                war.getZombieTilesOverwritten(), war.getPlayerTilesOverwritten(), round));
        }

        competitionEventListener.onWarEnd(0, String.join(", ", winners));

        synchronized (warriorRepository) {
          war.updateScores(warriorRepository);
        }
    }

    public void rerunWarForReplay(WarResult result, String outputFilename) throws Exception {
        System.out.printf("   -> Rerunning War ID %d (Seed: %d) for replay...%n", result.getId(), result.getSeed());

        // --- THIS IS THE CORRECTED LOGIC ---
        Competition replayComp = new Competition(false, this.options); // Temporary, no server
        ReplayRecorder recorder = new ReplayRecorder(outputFilename, replayComp);

        War replayWar = new War(recorder, recorder, false, this.options);

        // Explicitly link the recorder to the war it needs to observe
        recorder.setCurrentWar(replayWar);

        replayWar.setSeed(result.getSeed());
        recorder.onWarStart(result.getSeed()); // Manually trigger this now that the link is made
        replayWar.loadWarriorGroups(result.getParticipatingGroups());

        int round = 0;
        while(round < MAX_ROUND && !replayWar.isOver()) {
            replayWar.setCurrentRound(round);
            recorder.onRound(round);
            replayWar.nextRound();
            round++;
        }
        replayWar.setCurrentRound(round);

        String[] winners = replayWar.getRemainingWarriorNames().split(", ");
        recorder.onWarEnd(0, String.join(", ", winners));
    }

    public int getTotalNumberOfWars() {
        if (competitionIterator == null) {
             competitionIterator = new CompetitionIterator(warriorRepository.getNumberOfGroups(), options.combinationSize);
        }
        return (int) competitionIterator.getNumberOfCombos() * options.battlesPerCombo;
    }
    
    public WarriorRepository getWarriorRepository() { return warriorRepository; }
    public void setSeed(long seed){ this.seed = seed; }
    public long getSeed(){ return this.seed; }
    public void setAbort(boolean abort) { this.abort = abort; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getSpeed() { return this.speed; }
    public void addCompetitionEventListener(CompetitionEventListener lis) { competitionEventCaster.add(lis); }
    public void removeCompetitionEventListener(CompetitionEventListener lis) { competitionEventCaster.remove(lis); }
    public void addMemoryEventLister(MemoryEventListener lis) { memoryEventCaster.add(lis); }
    public void removeMemoryEventLister(MemoryEventListener lis) { memoryEventCaster.remove(lis); }
    public War getCurrentWar(){ return currentWar; }
    public int getCurrentWarrior() { return (currentWar != null) ? currentWar.getCurrentWarrior() : -1; }
}