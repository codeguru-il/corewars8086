package il.co.codeguru.corewars8086.cpu;

import il.co.codeguru.corewars8086.memory.MemoryException;
import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.memory.RealModeMemory;

/**
 * Wraps opcode fetching from CS:IP.
 * 
 * @author DL
 */
public class OpcodeFetcher {

    /**
     * Constructor.
     * @param state   Used to read & update CS:IP.
     * @param memory  Used to actually read the fetched bytes.
     */
    public OpcodeFetcher(CpuState state, RealModeMemory memory) {
        m_state = state;
        m_memory = memory;
    }

    /**
     * @return the next byte pointed by CS:IP (and advances IP).
     * @throws MemoryException  on any error.
     */
    public byte nextByte() throws MemoryException {
        RealModeAddress address = new RealModeAddress(
            m_state.getCS(), m_state.getIP());
        m_state.setIP((short)(m_state.getIP() + 1));
        return m_memory.readExecuteByte(address);
    }

    /**
     * @return the next word pointed by CS:IP (and advances IP).
     * @throws MemoryException  on any error.
     */
    public short nextWord() throws MemoryException {
        RealModeAddress address = new RealModeAddress(
            m_state.getCS(), m_state.getIP());
        m_state.setIP((short)(m_state.getIP() + 2));
        return m_memory.readExecuteWord(address);
    }

    /** Used to read & update CS:IP. */
    private final CpuState m_state;

    /** Used to actually read the fetched bytes. */
    private final RealModeMemory m_memory;
}