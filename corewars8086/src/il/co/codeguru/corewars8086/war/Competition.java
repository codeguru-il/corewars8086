package il.co.codeguru.corewars8086.war;

import il.co.codeguru.corewars8086.memory.MemoryEventListener;
import il.co.codeguru.corewars8086.utils.EventMulticaster;

import java.io.IOException;


public class Competition {

    /** Maximum number of rounds in a single war. */
    public final static int MAX_ROUND = 200000;
    private static final String SCORE_FILENAME= "scores.csv";

    private BinomialIterator competitionIterator;

    private EventMulticaster competitionEventCaster, memoryEventCaster;
    private CompetitionEventListener competitionEventListener;
    private MemoryEventListener memoryEventListener;

    private WarriorRepository warriorRepository;

    private War currentWar;

    private int warsPerCombination= 20;

    private int speed;
    public static final int MAXIMUM_SPEED = -1;
    private static final long DELAY_UNIT = 10;

    private boolean abort;

    public Competition() throws IOException {
        warriorRepository = new WarriorRepository();

        competitionEventCaster = new EventMulticaster(CompetitionEventListener.class);
        competitionEventListener = (CompetitionEventListener) competitionEventCaster.getProxy();
        memoryEventCaster = new EventMulticaster(MemoryEventListener.class);
        memoryEventListener = (MemoryEventListener) memoryEventCaster.getProxy();
        speed = MAXIMUM_SPEED;
        abort = false;
    }

    public void runCompetition (int warsPerCombination, int warriorsPerGroup) throws Exception {
        this.warsPerCombination = warsPerCombination;
        competitionIterator = new BinomialIterator(
            warriorRepository.getNumberOfGroups(), warriorsPerGroup);

        // run on every possible combination of warrior groups
        competitionEventListener.onCompetitionStart();
        for (int i = 0; i < warsPerCombination; i++)
        {
        	competitionIterator.reset();
        	while (competitionIterator.hasNext()) {
                runWar(warriorRepository.createGroupList((int[])competitionIterator.next()));
                if (abort) {
                    break;
                }
            }
        }
        competitionEventListener.onCompetitionEnd();
        warriorRepository.saveScoresToFile(SCORE_FILENAME);
    }

    public int getTotalNumberOfWars() {
        return (int) competitionIterator.getNumberOfItems() * warsPerCombination;
    }

    public void runWar(WarriorGroup[] warriorGroups) throws Exception {
        currentWar = new War(memoryEventListener, competitionEventListener);
        competitionEventListener.onWarStart();
        currentWar.loadWarriorGroups(warriorGroups);
            
        // go go go!
        int round = 0;
        while (round < MAX_ROUND) {
            competitionEventListener.onRound(round);

            // apply speed limits
            if (speed != MAXIMUM_SPEED) {
                // note: if speed is 1 (meaning game is paused), this will
                // always happen
                if (round % speed == 0) {
                    Thread.sleep(DELAY_UNIT);
                }
                    
                if (speed == 1) { // paused
                	continue;
                }
           }
                
          	currentWar.nextRound(round);
            	
            if (currentWar.isOver()) {
                break;
            }
                
            ++round;
        }
        competitionEventListener.onRound(round);

        int numAlive = currentWar.getNumRemainingWarriors();
        String names = currentWar.getRemainingWarriorNames();

        if (numAlive == 1) { // we have a single winner!
            competitionEventListener.onWarEnd(CompetitionEventListener.SINGLE_WINNER, names);
        } else if (round == MAX_ROUND) { // maximum round reached
            competitionEventListener.onWarEnd(CompetitionEventListener.MAX_ROUND_REACHED, names);
        } else { // user abort
            competitionEventListener.onWarEnd(CompetitionEventListener.ABORTED, names);
        }
        currentWar.updateScores(warriorRepository);
        currentWar = null;
    }

    public int getCurrentWarrior() {
        if (currentWar != null) {
            return currentWar.getCurrentWarrior();
        } else {
            return -1;
        }
    }

    public void addCompetitionEventListener(CompetitionEventListener lis) {
        competitionEventCaster.add(lis);
    }

    public void addMemoryEventLister(MemoryEventListener lis) {
        memoryEventCaster.add(lis);
    }

    public WarriorRepository getWarriorRepository() {
        return warriorRepository;
    }

    /**
     * Set the speed of the competition, wither MAX_SPEED or a positive integer 
     * when 1 is the slowest speed
     * @param speed
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public void setAbort() {
        this.abort = true;
    }
}