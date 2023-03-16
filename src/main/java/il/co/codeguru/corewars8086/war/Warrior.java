package il.co.codeguru.corewars8086.war;

import il.co.codeguru.corewars8086.cpu.Cpu;
import il.co.codeguru.corewars8086.cpu.CpuException;
import il.co.codeguru.corewars8086.cpu.CpuState;
import il.co.codeguru.corewars8086.memory.MemoryException;
import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.memory.RealModeMemory;
import il.co.codeguru.corewars8086.memory.RealModeMemoryRegion;
import il.co.codeguru.corewars8086.memory.RestrictedAccessRealModeMemory;


/**
 * A single CoreWars warrior.
 * 
 * @author DL
 */
public class Warrior {

    /**
     * Constructor.
     * 
     * @param name	            Warrior's name.
     * @param codeSize          Warrior's code size.
     * @param core              Real mode memory used as core.
     * @param loadAddress       Warrior's load address in the core (initial CS:IP).
     * @param initialStack      Warrior's private stack in the core (initial SS:SP).
     * @param groupSharedMemory Warrior group's shared memroy address (initial ES).
     * @param groupSharedMemorySize Warrior group's shared memory size. 
     */
    public Warrior(
        String name,
        int codeSize,
        RealModeMemory core,
        RealModeAddress loadAddress,
        RealModeAddress initialStack,
        RealModeAddress groupSharedMemory,
        short groupSharedMemorySize,
        WarriorType type) {

        this.type = type;
        m_name = name;
        m_codeSize = codeSize;
        m_loadAddress = loadAddress;

        m_state = new CpuState();
        initializeCpuState(loadAddress, initialStack, groupSharedMemory);

        // initialize read-access regions
        RealModeAddress lowestStackAddress =
                new RealModeAddress(initialStack.getSegment(), (short) 0);
        RealModeAddress lowestCoreAddress =
                new RealModeAddress(loadAddress.getSegment(), (short) 0);
        RealModeAddress highestCoreAddress =
                new RealModeAddress(loadAddress.getSegment(), (short) -1);
        RealModeAddress highestGroupSharedMemoryAddress =
                new RealModeAddress(groupSharedMemory.getSegment(),
                        (short) (groupSharedMemorySize - 1));

        RealModeMemoryRegion[] readAccessRegions =
                new RealModeMemoryRegion[]{
                        new RealModeMemoryRegion(lowestStackAddress, initialStack),
                        new RealModeMemoryRegion(lowestCoreAddress, highestCoreAddress),
                        new RealModeMemoryRegion(groupSharedMemory, highestGroupSharedMemoryAddress)
                };

        // initialize write-access regions
        RealModeMemoryRegion[] writeAccessRegions =
                new RealModeMemoryRegion[]{
                        new RealModeMemoryRegion(lowestStackAddress, initialStack),
                        new RealModeMemoryRegion(lowestCoreAddress, highestCoreAddress),
                        new RealModeMemoryRegion(groupSharedMemory, highestGroupSharedMemoryAddress)
                };

        // initialize execute-access regions
        RealModeMemoryRegion[] executeAccessRegions =
                new RealModeMemoryRegion[]{
                        new RealModeMemoryRegion(lowestCoreAddress, highestCoreAddress)
                };

        m_memory = new RestrictedAccessRealModeMemory(
            core, readAccessRegions, writeAccessRegions, executeAccessRegions);

        m_cpu = new Cpu(m_state, m_memory);

        m_isAlive = true;		
    }

    /**
     * @return whether or not the warrior is still alive.
     */
    public boolean isAlive() {
        return m_isAlive;
    }

    /**
     * Kills the warrior.
     */
    public void kill() {
        m_isAlive = false;
    }

    public boolean isZombie() {
        return type == WarriorType.ZOMBIE || type == WarriorType.ZOMBIE_H || type == WarriorType.ZOMBIE_BOX;
    }

    public WarriorType getType() {
        return type;
    }

    /**
     * @return the warrior's name.
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return the warrior's load offset.
     */
    public short getLoadOffset() {
        return m_loadAddress.getOffset();
    }
    public void setLoadOffset(RealModeAddress value) {
        m_loadAddress = value;
    }
    /**
     * @return the warrior's initial code size.
     */
    public int getCodeSize() {
        return m_codeSize;
    }

    /**
     * Accessors for the warrior's Energy value (used to calculate
     * the warrior's speed).
     */
    public short getEnergy() {
        return m_state.getEnergy();
    }
    public void setEnergy(short value) {
        m_state.setEnergy(value);
    }

    /**
     * Performs the warrior's next turn (= next opcode).
     * @throws CpuException     on any CPU error.
     * @throws MemoryException  on any Memory error.
     */
    public void nextOpcode() throws CpuException, MemoryException {
        m_cpu.nextOpcode();
    }

    /**
     * Initializes the Cpu registers & flags:
     *  CS,DS                    - set to the core's segment.
     *  ES                       - set to the group's shared memory segment.
     *  AX,IP                    - set to the load address.
     *  SS                       - set to the private stack's segment.
     *  SP                       - set to the private stack's offset.
     *  BX,CX,DX,SI,DI,BP, flags - set to zero.
     * 
     * @param loadAddress       Warrior's load address in the core.
     * @param initialStack      Warrior's private stack (initial SS:SP).
     * @param groupSharedMemory The warrior's group shared memory.
     */
    private void initializeCpuState(
        RealModeAddress loadAddress, RealModeAddress initialStack,
        RealModeAddress groupSharedMemory) {

        // initialize registers
        m_state.setAX(loadAddress.getOffset());
        m_state.setBX((short)0);
        m_state.setCX((short)0);
        m_state.setDX((short)0);

        m_state.setDS(loadAddress.getSegment());
        m_state.setES(groupSharedMemory.getSegment());
        m_state.setSI((short)0);
        m_state.setDI((short)0);

        m_state.setSS(initialStack.getSegment());
        m_state.setBP((short)0);
        m_state.setSP(initialStack.getOffset());

        m_state.setCS(loadAddress.getSegment());
        m_state.setIP(loadAddress.getOffset());
        m_state.setFlags((short)0);

        // initialize Energy
        m_state.setEnergy((short)0);

        // initialize bombs
        m_state.setBomb1Count((byte)2);
        m_state.setBomb2Count((byte)1);
        m_state.setBomb3Count((byte)1);

    }
    
    public CpuState getCpuState(){
    	return m_state;
    }

    public RestrictedAccessRealModeMemory getRestrictedAccessRealModeMemory(){
        return m_memory;
    }
    public void setName(String value){m_name = value;};
    /** Warrior's name */
    private String m_name;
    /** Warrior's initial code size */	
    private final int m_codeSize;
    /** Warrior's initial load address */	
    private RealModeAddress m_loadAddress;
    /** Current state of registers & flags */	
    private CpuState m_state;
    /** Applies restricted access logic on top of the actual core memory */
    private RestrictedAccessRealModeMemory m_memory;
    /** CPU instance */
    private Cpu m_cpu;
    /** Whether or not the warrior is still alive */
    private boolean m_isAlive;

    private final WarriorType type;
}