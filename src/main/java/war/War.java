package war;

import cli.Options;
import cpu.CpuException;
import memory.MemoryEventListener;
import memory.MemoryException;
import memory.RealModeAddress;
import memory.RealModeMemoryImpl;
import utils.Unsigned;

import java.util.*;


public class War {
    public final static short ARENA_SEGMENT = 0x1000;
    public final static int ARENA_SIZE =
        RealModeAddress.PARAGRAPHS_IN_SEGMENT * RealModeAddress.PARAGRAPH_SIZE;
    private final static short STACK_SIZE = 2*1024;
    private final static short GROUP_SHARED_MEMORY_SIZE = 1024;
    private final static byte ARENA_BYTE = (byte)0xCC;
    private final static int MAX_WARRIORS = 20;
    private final static int MAX_LOADING_TRIES = 100;
    private final static int MIN_GAP = 1024;

    private Warrior[] m_warriors;
    private int m_numWarriors;
    private int m_numWarriorsAlive;
    /** Number of survivors still alive */
    private int m_numSurvivorsAlive;
    /**
     * Addresses equal or larger than this are still unused.
     * An address can be 'used' either by the Arena, or by the private stacks.
     */
    private int m_nextFreeAddress;
    private RealModeMemoryImpl m_core;

    private int m_currentWarrior;
    private int currentRound = 0; // Added for replay recorder

    private CompetitionEventListener m_warListener;
    private final Options options;

    public War(MemoryEventListener memoryListener,
               CompetitionEventListener warListener,
               boolean startPaused,
               Options options) {
        this.options = options;
    	isPaused = startPaused;
        m_warListener = warListener;
        m_warriors = new Warrior[MAX_WARRIORS];
        m_numWarriors = 0;
        m_numWarriorsAlive = 0;
        m_numSurvivorsAlive = 0;
        m_core = new RealModeMemoryImpl();
        m_nextFreeAddress = RealModeAddress.PARAGRAPH_SIZE *
            (ARENA_SEGMENT + RealModeAddress.PARAGRAPHS_IN_SEGMENT);

        for (int offset = 0; offset < ARENA_SIZE; ++offset) {
            RealModeAddress tmp = new RealModeAddress(ARENA_SEGMENT, (short)offset);
            m_core.writeByte(tmp, ARENA_BYTE);			
        }

        isSingleRound = false;
        m_core.setListener(memoryListener);
    }

    // --- NEW METHOD ---
    public void setCurrentRound(int round) {
        this.currentRound = round;
    }
    public int getCurrentRound() {
        return this.currentRound;
    }

    public void loadWarriorGroups(WarriorGroup[] warriorGroups) throws Exception {
        m_currentWarrior = 0;
        List<WarriorGroup> groupsLeftToLoad = new ArrayList<>(Arrays.asList(warriorGroups));
               
        while (!groupsLeftToLoad.isEmpty()) {
        	int randomInt = rand.nextInt(groupsLeftToLoad.size());
        	loadWarriorGroup(groupsLeftToLoad.get(randomInt));
        	groupsLeftToLoad.remove(randomInt);
        }
    }
	
    // --- METHOD SIGNATURE CORRECTED ---
    public void nextRound() {
        for (int i = 0; i < m_numWarriors; ++i) {
            Warrior warrior = m_warriors[i];
            m_currentWarrior = i;

            if (warrior.isAlive()) {
                try {
                    warrior.nextOpcode();

                    if (warrior.isZombie()) {
                        int remainingRounds =
                                (options.zombieSpeed * ((warrior.getType() == WarriorType.ZOMBIE_H) ? 2 : 1)) - 1;
                        for (int j = 0; j < remainingRounds; j++) {
                            warrior.nextOpcode();
                        }
                    } else {
                        // --- THIS IS THE CORRECTED LINE ---
                        updateWarriorEnergy(warrior, this.currentRound);
                        if (shouldRunExtraOpcode(warrior)) {
                            warrior.nextOpcode();
                        }
                    }

                } catch (CpuException e) {
                    m_warListener.onWarriorDeath(warrior.getName(), "CPU exception");
                    warrior.kill();
                    --m_numWarriorsAlive;
                    if (!warrior.isZombie()) {
                        --m_numSurvivorsAlive;
                    }

                } catch (MemoryException e) {
                    m_warListener.onWarriorDeath(warrior.getName(), "memory exception");
                    warrior.kill();
                    --m_numWarriorsAlive;
                    if (!warrior.isZombie()) {
                        --m_numSurvivorsAlive;
                    }
                }
            }
        }
    }

    /**
     * Checks if the war is over. A war is defined as complete when all living survivors (that is, not zombies) are from
     * the same group.
     */
    public boolean isOver() {
        return m_numWarriorsAlive <= 1
                || m_numSurvivorsAlive == 1
                || (m_numSurvivorsAlive == 2 && countAliveSurvivorGroups() == 1);
    }

    /**
     * Count the number of alive survivor groups - defined as survivor groups with at least one alive survivor.
     */
    private long countAliveSurvivorGroups() {
        return Arrays.stream(m_warriors)
                .filter(Objects::nonNull)  // m_warriors contains null references for indices >= m_numWarriors
                .filter(Warrior::isAlive)
                .filter(w -> !w.isZombie())
                .map(Warrior::getGroupName)
                .distinct().count();
    }

    private void updateWarriorEnergy(Warrior warrior, int round) {
        if ((round % DECELERATION_ROUNDS) == 0) {
            int energy = Unsigned.unsignedShort(warrior.getEnergy());

            if (energy > 0 ) {
                warrior.setEnergy((short)(energy-1));
            }
        }
    }

    private boolean shouldRunExtraOpcode(Warrior warrior) {
        int energy = Unsigned.unsignedShort(warrior.getEnergy());
        int speed = calculateWarriorSpeed(energy);
        return (rand.nextInt(MAX_SPEED) < speed);
    }

    private final int MAX_SPEED = 16;
    private final int DECELERATION_ROUNDS = 5;

    private int calculateWarriorSpeed(int energy) {
        if (energy == 0) {
            return 0;
        } else {
            return Math.min(MAX_SPEED, 1 + (int)(Math.log(energy) / Math.log(2)));
        }
    }
	
    private void loadWarriorGroup(WarriorGroup warriorGroup) throws Exception {
        List<WarriorData> warriors = warriorGroup.getWarriors();
        RealModeAddress groupSharedMemory = allocateCoreMemory(GROUP_SHARED_MEMORY_SIZE);

        for (WarriorData warriorData : warriors) {
            String warriorName = warriorData.getName();
            byte[] warriorCode = warriorData.getCode();

            short loadOffset = getLoadOffset(warriorCode.length);

            RealModeAddress loadAddress = new RealModeAddress(ARENA_SEGMENT, loadOffset);
            RealModeAddress stackMemory = allocateCoreMemory(STACK_SIZE);
            RealModeAddress initialStack = new RealModeAddress(stackMemory.getSegment(), STACK_SIZE);

            Warrior warrior = new Warrior(
                warriorName, warriorGroup.getName(),
                    warriorCode.length, m_core,
                loadAddress, initialStack, groupSharedMemory,
                GROUP_SHARED_MEMORY_SIZE, warriorData.getType()
            );

            m_warriors[m_numWarriors++] = warrior;

            for (int offset = 0; offset < warriorCode.length; ++offset) {
                RealModeAddress tmp = new RealModeAddress(ARENA_SEGMENT, (short) (loadOffset + offset));
                m_core.writeByte(tmp, warriorCode[offset]);
            }
            ++m_numWarriorsAlive;
            ++m_currentWarrior;

            if (!warrior.isZombie()) {
                ++m_numSurvivorsAlive;
            }

            // notify listener
            m_warListener.onWarriorBirth(warriorName);
        }
    }

    private RealModeAddress allocateCoreMemory(short size) throws Exception {
        if ((size % RealModeAddress.PARAGRAPH_SIZE) != 0) {
            throw new Exception();
        }
        RealModeAddress allocatedMemory = new RealModeAddress(m_nextFreeAddress);
        m_nextFreeAddress += size;
        return allocatedMemory;
    }

    private short getLoadOffset(int warriorSize) throws Exception {
        int loadAddress = 0;
        boolean found = false;
        int numTries = 0;
        while ((!found) && (numTries < MAX_LOADING_TRIES)) {
            ++numTries;
            loadAddress = rand.nextInt(ARENA_SIZE);
            found = true;
            if (loadAddress < MIN_GAP || loadAddress + warriorSize > ARENA_SIZE - MIN_GAP) {
                found = false;
                continue;
            }
            for (int i = 0; i < m_numWarriors; ++i) {
                int otherLoadAddress = Unsigned.unsignedShort(m_warriors[i].getLoadOffset());
                int otherSize = m_warriors[i].getCodeSize();
                int otherStart = otherLoadAddress - MIN_GAP;
                int otherEnd = otherLoadAddress + otherSize + MIN_GAP;
                if ((loadAddress + warriorSize >= otherStart) && (loadAddress < otherEnd)) {
                    found = false;
                    break;
                }
            }
        }
        if (!found) throw new Exception("Could not find a place to load warrior.");
        return (short)loadAddress;
    }

    public int getCurrentWarrior() {
        return m_currentWarrior;
    }
    public Warrior getWarrior(int index) {
        return m_warriors[index];
    }
    public int getNumWarriors() {
        return m_numWarriors;
    }
    
    /** @return the number of survivors still alive. */
    public int getNumRemainingSurvivors() {
    	return m_numSurvivorsAlive;
    }

    public String getRemainingWarriorNames() {
        StringBuilder names = new StringBuilder();
    	for (int i = 0; i < m_numWarriors; ++i) {
            Warrior warrior = m_warriors[i];
            if (warrior.isAlive()) {
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(warrior.getName());
            }
    	}
    	return names.toString();
    }

    public void updateScores(WarriorRepository repository) {
        if (m_numWarriorsAlive == 0) return;
        float score =  1.0f / m_numSurvivorsAlive;
    	for (int i = 0; i < m_numWarriors; i++) {
            Warrior warrior = m_warriors[i];
            if (warrior.isAlive() && !warrior.isZombie()) {
                repository.addScore(warrior.getName(), score);
            }
        }
    }
    
    private Random rand = new Random();
    private boolean isSingleRound;
    private boolean isPaused;
    public void pause(){ isPaused = true; }
    public boolean isPaused(){ return isPaused; }
    public void resume(){ isPaused = false; isSingleRound = false; }
    public void runSingleRound(){ this.resume(); isSingleRound = true; }
    public boolean isSingleRound(){ return this.isSingleRound; }
    public RealModeMemoryImpl getMemory(){ return m_core; }

    public long getSeed() {
        // We need to get the seed from the Random object
        // Since Random doesn't let us get the seed directly after it's used,
        // we'll store it.
        return this.seed;
    }

    // Also, add a member variable for the seed and set it.
    private long seed;
    public void setSeed(long seed){
        this.seed = seed;
        rand.setSeed(seed);
    }
}