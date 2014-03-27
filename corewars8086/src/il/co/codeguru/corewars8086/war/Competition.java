package il.co.codeguru.corewars8086.war;

import il.co.codeguru.corewars8086.memory.MemoryEventListener;
import il.co.codeguru.corewars8086.utils.EventMulticaster;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


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

    private boolean abort, tillEnd;

    public Competition(boolean tillEnd) throws IOException {
        warriorRepository = new WarriorRepository();

        competitionEventCaster = new EventMulticaster(CompetitionEventListener.class);
        competitionEventListener = (CompetitionEventListener) competitionEventCaster.getProxy();
        memoryEventCaster = new EventMulticaster(MemoryEventListener.class);
        memoryEventListener = (MemoryEventListener) memoryEventCaster.getProxy();
        speed = MAXIMUM_SPEED;
        abort = false;
        this.tillEnd = tillEnd;
    }

    public void runAndSaveCompetition(int warsPerCombination, int warriorsPerGroup, String groupName) throws Exception
    {
    	String results = runCompetition(warsPerCombination, warriorsPerGroup, groupName);
    	try
    	{
            FileOutputStream fos = new FileOutputStream(SCORE_FILENAME);
            PrintStream ps = new PrintStream(fos);
            ps.print(results);
            fos.close();
        }
    	catch (FileNotFoundException e)
    	{
            e.printStackTrace();
        }
    	catch (IOException e)
    	{
            e.printStackTrace();
        }
    }
    
    public String runCompetition (int warsPerCombination, int warriorsPerGroup, String groupName) throws Exception {
    	this.warsPerCombination = warsPerCombination;
        competitionIterator = new BinomialIterator(
            warriorRepository.getNumberOfGroups(), warriorsPerGroup);

        // run on every possible combination of warrior groups
        competitionEventListener.onCompetitionStart();
        for (int i = 0; i < warsPerCombination; i++)
        {
        	competitionIterator.reset();
        	while (competitionIterator.hasNext())
        	{
        		WarriorGroup[] curGroups = warriorRepository.createGroupList((int[])competitionIterator.next());
            	
            	if (groupName != "")
            	{
            		boolean found = false;
                	for (int j = 0; j < curGroups.length && !found; j++)
                		if (curGroups[j].getName().equalsIgnoreCase(groupName))
                			found = true;
                	
                	if (!found)
                		continue;
            	}
            	
                runWar(1, curGroups);
                
                if (abort)
                {
                    break;
                }
            }
        }
        competitionEventListener.onCompetitionEnd();
        
        return warriorRepository.getScores();
    }

    public int getTotalNumberOfWars() {
        return (int) competitionIterator.getNumberOfItems() * warsPerCombination;
    }

    public void runWar(int numberOfRounds, WarriorGroup[] warriorGroups) throws Exception {
        for(int war = 0; war < numberOfRounds; war++) {
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
            	
                if (currentWar.isOver() && !tillEnd) {
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
        }
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