package il.co.codeguru.corewars8086.war;

import il.co.codeguru.corewars8086.memory.MemoryEventListener;
import il.co.codeguru.corewars8086.utils.EventMulticaster;

import java.io.IOException;
import java.util.Random;

public class Competition {

    /** Maximum number of rounds in a single war. */
    public final static int MAX_ROUND = 200000;
    private static final String SCORE_FILENAME= "scores.csv";

    private CompetitionIterator competitionIterator;

    private EventMulticaster competitionEventCaster, memoryEventCaster;
    private CompetitionEventListener competitionEventListener;
    private MemoryEventListener memoryEventListener;

    private WarriorRepository warriorRepository;

    private War currentWar;

    private int warsPerCombination= 20;

    private int speed;
    public static final int MAXIMUM_SPEED = 100;
    public static final double SPEED_BASE = 1.04;
    private static final long DELAY_UNIT = 10;

    private boolean abort, tillEnd, specificGroup, hasCustomSeed, pauseFlag;
    private long seed;

    public Competition(boolean runEndlessly) throws IOException {
        warriorRepository = new WarriorRepository();

        competitionEventCaster = new EventMulticaster(CompetitionEventListener.class);
        competitionEventListener = (CompetitionEventListener) competitionEventCaster.getProxy();
        memoryEventCaster = new EventMulticaster(MemoryEventListener.class);
        memoryEventListener = (MemoryEventListener) memoryEventCaster.getProxy();
        speed = MAXIMUM_SPEED;
        abort = false;
        tillEnd = runEndlessly;
        seed = 0;
        hasCustomSeed = false;
        pauseFlag = false;
    }

    public void runAndSaveCompetition (int warsPerCombination, int warriorsPerGroup, String groupName, boolean binomicalRun) throws Exception {
    	runCompetition(warsPerCombination, warriorsPerGroup, groupName, binomicalRun);
    	warriorRepository.saveScoresToFile(SCORE_FILENAME);
    }

    public String runCompetition (int warsPerCombination, int warriorsPerGroup, String groupName, boolean binomicalRun) throws Exception {
    	abort = false;
        this.warsPerCombination = warsPerCombination;
        
        if (binomicalRun)
        	competitionIterator = new BinomialIterator(
        			warriorRepository.getNumberOfGroups(), warriorsPerGroup);
        else
        	competitionIterator = new RandomIterator(
        			warriorRepository.getNumberOfGroups(), warriorsPerGroup);
        
        specificGroup = !groupName.equals("");
        
        // run on every possible combination of warrior groups
        competitionEventListener.onCompetitionStart();
        for (int i = 0; i < warsPerCombination; i++)
        {
        	competitionIterator.reset();
        	
        	while (competitionIterator.hasNext())
        	{
        		if (hasCustomSeed)
            		seed++;
            	else
            		seed = Math.abs(rand.nextLong());
            	
            	WarriorGroup[] curGroups = warriorRepository.createGroupList(competitionIterator.next());
                	
                if (specificGroup)
                {
                	boolean found = false;
                    for (int j = 0; j < curGroups.length && !found; j++)
                    	if (curGroups[j].getName().equalsIgnoreCase(groupName))
                    		found = true;
                    	
                    if (!found)
                    	continue;
                }
                
                runWar(curGroups);
                if (abort) {
                	break;
        		}
        	}
        	
        	if (abort) {
        		break;
        	}
        }
        
        competitionEventListener.onCompetitionEnd();
        hasCustomSeed = false;
        return warriorRepository.getScores();
    }

    public int getTotalNumberOfWars() {
        return (int) (warsPerCombination * competitionIterator.getNumberOfItems(specificGroup ? 1 : 0));
    }

    public void runWar(WarriorGroup[] warriorGroups) throws Exception {
        currentWar = new War(memoryEventListener, competitionEventListener);
        currentWar.setPausedFlag(pauseFlag);
        currentWar.setSeed(seed);
        
        competitionEventListener.onWarStart();
        currentWar.loadWarriorGroups(warriorGroups);
        
        // go go go!
        int round = 0;
        while (round < MAX_ROUND) {
            competitionEventListener.onRound(round);
            
            competitionEventListener.onEndRound();
            
            // apply speed limits
            if (speed != MAXIMUM_SPEED) {
                // note: if speed is 1 (meaning game is paused), this will
                // always happen
                Thread.sleep( (long) (Math.pow(SPEED_BASE, MAXIMUM_SPEED - speed) * DELAY_UNIT));
                
                if (speed == 1) { // paused
                	continue;
                }
            }
            
            //System.out.println(System.nanoTime() + ": " + speed);
            
            //pause - originally done by kirill
            while(currentWar.getPausedFlag())
            	Thread.sleep(DELAY_UNIT);
                            
            //Single step run - stop next time - originally done by kirill
            if(currentWar.getSingleRoundFlag())
            {
            	currentWar.setPausedFlag(true);
            	currentWar.setSingleRoundFlag(false);
            }
            
            if (currentWar.isOver() && !tillEnd && round != 0)
                break;
            
            currentWar.nextRound(round);
            
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

    public void removeCompetitionEventListener(CompetitionEventListener lis) {
    	competitionEventCaster.remove(lis);
    }
    
    public void addMemoryEventLister(MemoryEventListener lis) {
        memoryEventCaster.add(lis);
    }

    public void removeMemoryEventLister(MemoryEventListener lis) {
    	memoryEventCaster.remove(lis);
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
    
    public void setTillEnd(boolean tillEnd)
    {
    	this.tillEnd = tillEnd;
    }
    
    public boolean getTillEnd()
    {
    	return tillEnd;
    }
    
    public void setSeed(long seed)
    {
    	this.seed = seed - 1;
    	hasCustomSeed = true;
    }
    public long getSeed()
    {
    	return seed;
    }
    
    public void setPausedFlag(boolean pause)
    {
    	pauseFlag = pause;
    }
    public boolean getPausedFlag()
    {
    	return pauseFlag;
    }
    
    public War getCurrentWar()
    {
    	return currentWar;
    }
    
    
    /** this random generates the seed for the war random, in order to be able to fetch the seed from it */
    private Random rand = new Random();
}