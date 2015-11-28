package il.co.codeguru.corewars8086.war;

import il.co.codeguru.corewars8086.hardware.Address;
import il.co.codeguru.corewars8086.hardware.InterruptException;
import il.co.codeguru.corewars8086.hardware.Machine;
import il.co.codeguru.corewars8086.hardware.cpu.Cpu;
import il.co.codeguru.corewars8086.hardware.cpu.CpuException;
import il.co.codeguru.corewars8086.hardware.cpu.CpuState;
import il.co.codeguru.corewars8086.hardware.memory.MemoryException;
import il.co.codeguru.corewars8086.hardware.AbstractAddress;
import il.co.codeguru.corewars8086.hardware.memory.RealModeMemory;
import il.co.codeguru.corewars8086.hardware.memory.RealModeMemoryRegion;
import il.co.codeguru.corewars8086.hardware.memory.MemoryAccessProtection;


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
        Machine machine,
        AbstractAddress loadAddress,
        AbstractAddress initialStack,
        AbstractAddress groupSharedMemory,
        short groupSharedMemorySize) {

        m_name = name;
        m_codeSize = codeSize;
        m_loadAddress = loadAddress;

		m_machine = machine;

        initializeCpuState(loadAddress, initialStack, groupSharedMemory);

        // initialize read-access regions
        AbstractAddress lowestStackAddress = new Address(initialStack.getSegment(), (short)0);
        AbstractAddress lowestCoreAddress = new Address(loadAddress.getSegment(), (short)0);
        AbstractAddress highestCoreAddress = new Address(loadAddress.getSegment(), (short)-1);
        AbstractAddress highestGroupSharedMemoryAddress = new Address(groupSharedMemory.getSegment(), (short)(groupSharedMemorySize-1));

        RealModeMemoryRegion[] readAccessRegions = new RealModeMemoryRegion[] {
                new RealModeMemoryRegion(lowestStackAddress, initialStack),
                new RealModeMemoryRegion(lowestCoreAddress, highestCoreAddress),
                new RealModeMemoryRegion(groupSharedMemory, highestGroupSharedMemoryAddress)
            };

        // initialize write-access regions
        RealModeMemoryRegion[] writeAccessRegions = new RealModeMemoryRegion[] {
                new RealModeMemoryRegion(lowestStackAddress, initialStack),
                new RealModeMemoryRegion(lowestCoreAddress, highestCoreAddress),
                new RealModeMemoryRegion(groupSharedMemory, highestGroupSharedMemoryAddress)
            };

        // initialize execute-access regions
        RealModeMemoryRegion[] executeAccessRegions = new RealModeMemoryRegion[] {
                new RealModeMemoryRegion(lowestCoreAddress, highestCoreAddress)
            };

		machine.memoryProtection.setProtection(lowestStackAddress, initialStack.getLinearAddress() - lowestStackAddress.getLinearAddress(), MemoryAccessProtection.PROT_READ_WRITE);
		machine.memoryProtection.setProtection(lowestCoreAddress, highestCoreAddress.getLinearAddress() - lowestCoreAddress.getLinearAddress(), MemoryAccessProtection.PROT_READ_WRITE | MemoryAccessProtection.PROT_EXEC);
		machine.memoryProtection.setProtection(groupSharedMemory, highestGroupSharedMemoryAddress.getLinearAddress() - groupSharedMemory.getLinearAddress(), MemoryAccessProtection.PROT_READ_WRITE);

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

    /**
     * @return the warrior's name.
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return the warrior's load offset.
     */
    public int getLoadOffset() {
        return m_loadAddress.getOffset();
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
    public int getEnergy() {
        return m_machine.state.getEnergy();
    }
    public void setEnergy(int value) {
        m_machine.state.setEnergy(value);
    }

    /**
     * Performs the warrior's next turn (= next opcode).
     * @throws CpuException     on any CPU error.
     * @throws MemoryException  on any Memory error.
     */
    public void nextOpcode() throws CpuException, MemoryException, InterruptException {
        m_machine.cpu.nextOpcode();
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
        AbstractAddress loadAddress, AbstractAddress initialStack,
        AbstractAddress groupSharedMemory) {

        // initialize registers
		m_machine.state.setAX((short)loadAddress.getOffset());
		m_machine.state.setBX((short)0);
		m_machine.state.setCX((short)0);
		m_machine.state.setDX((short)0);

		m_machine.state.setDS((short)loadAddress.getSegment());
		m_machine.state.setES((short)groupSharedMemory.getSegment());
		m_machine.state.setSI((short)0);
		m_machine.state.setDI((short)0);

		m_machine.state.setSS((short)initialStack.getSegment());
		m_machine.state.setBP((short)0);
		m_machine.state.setSP((short)initialStack.getOffset());

		m_machine.state.setCS((short)loadAddress.getSegment());
		m_machine.state.setIP((short)loadAddress.getOffset());
		m_machine.state.setFlags((short)0);

        // initialize Energy
		m_machine.state.setEnergy((short)0);

        // initialize bombs
		m_machine.state.setBomb1Count((byte)2);
		m_machine.state.setBomb2Count((byte)1);
    }
    
    public CpuState getCpuState(){
    	return m_machine.state;
    }

    /** Warrior's name */
    private final String m_name;	
    /** Warrior's initial code size */	
    private final int m_codeSize;
    /** Warrior's initial load address */	
    private final AbstractAddress m_loadAddress;
    /** Whether or not the warrior is still alive */
    private boolean m_isAlive;

	private Machine m_machine;
}
