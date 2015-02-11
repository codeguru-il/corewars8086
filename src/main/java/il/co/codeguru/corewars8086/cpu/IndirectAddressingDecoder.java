package il.co.codeguru.corewars8086.cpu;

import il.co.codeguru.corewars8086.memory.MemoryException;
import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.memory.RealModeMemory;

/**
 * Decodes indirect-addressing opcodes (translates between the CPU's internal
 * representation of indirect-addressing, to the actual real-mode address).
 * 
 * The CPU supports four indirect addressing modes:
 *  (0) [BX+SI]         indirect
 *  (1) [BX+SI+12h]     indirect + imm8
 *  (2) [BX+SI+1234h]   indirect + imm16
 *  (3) AX              direct register mode
 * 
 * Each indirect-addressing opcode has two operands: a register, and one of the
 * above. e.g:
 *               ADD [BX+SI], AX
 * 
 * @author DL
 */
public class IndirectAddressingDecoder {
	
    /**
     * Constructor.
     * @param state    CPU registers.
     * @param memory   Memory. 
     * @param fetcher  Used to fetch additional opcode bytes.
     */
    public IndirectAddressingDecoder(
        CpuState state, RealModeMemory memory, OpcodeFetcher fetcher) {

        m_state = state;
        m_memory = memory;
        m_fetcher = fetcher;
        m_regs = new RegisterIndexingDecoder(m_state);

        m_regIndex = 0;
        m_memIndex = 0;
        m_memAddress = null;
    }

    /**
     * Fetches & decodes the bytes currently pointed by the OpcodeFetcher.
     * @throws MemoryException on any error while reading from memory.
     */
    public void reset() throws MemoryException {

        // read the 'mode' byte (MM RRR III)
        // M - indirect addressing mode mux
        // R - register indexing
        // I - indirect addressing indexing 
        byte modeByte = m_fetcher.nextByte();

        byte mode = (byte)((modeByte >> 6) & 0x03);
        m_regIndex = (byte)((modeByte >> 3) & 0x07);		
        m_memIndex = (byte)(modeByte & 0x07);

        // decode the opcode according to the indirect-addressing mode, and
        // retrieve the address operand
        switch (mode) {
            case 0:
                m_memAddress = getMode0Address();
                break;
            case 1:
                m_memAddress = getMode1Address();
                break;
            case 2:
                m_memAddress = getMode2Address();
                break;
            case 3:			
                m_memAddress = getMode3Address();
                break;
            default:
                throw new RuntimeException();
        }
    }

    /**
     * @return 3 bits representing the internal register indexing.
     */
    public byte getRegIndex() {
        return m_regIndex;
    }

    /**
     * @return The indirect memory operand's address (or null if the latter
     *         refers to a register).
     */
    public RealModeAddress getMemAddress() {
        return m_memAddress;
    }

    /**
     * Assuming the opcode operand referred to an 8bit register, returns the
     * corresponding register's value.
     * @return 8bit register value. 
     */
    public byte getReg8() {
        return m_regs.getReg8(m_regIndex);
    }

    /**
     * Returns the 8bit value pointed by the indirect memory operand (or register,
     * depands on the indirect-addressing mode). 
     * @return Indirect address (or register) 8bit value. 
     */
    public byte getMem8() throws MemoryException {
        if (m_memAddress != null) {
            return m_memory.readByte(m_memAddress);
        }
        return m_regs.getReg8(m_memIndex);
    }

    /**
     * Assuming the opcode operand referred to a 16bit register, returns the
     * corresponding register's value.
     * @return 16bit register value. 
     */
    public short getReg16() {
        return m_regs.getReg16(m_regIndex);
    }

    /**
     * Assuming the opcode operand referred to a segment register, returns the
     * corresponding register's value.
     * @return segment register value. 
     */
    public short getSeg() {
        return m_regs.getSeg(m_regIndex);
    }

    /**
     * Returns the 16bit value pointed by the indirect memory operand (or register,
     * depands on the indirect-addressing mode). 
     * @return Indirect address (or register) 16bit value. 
     */
    public short getMem16() throws MemoryException {
        if (m_memAddress != null) {
            return m_memory.readWord(m_memAddress);
        }
        return m_regs.getReg16(m_memIndex);
    }

    /**
     * Assuming the opcode operand referred to an 8bit register, sets the
     * corresponding register's value.
     * @param value    New value for the 8bit register.
     */
    public void setReg8(byte value) {
        m_regs.setReg8(m_regIndex, value);
    }

    /**
     * Sets the 8bit value pointed by the indirect memory operand (or register,
     * depands on the indirect-addressing mode).
     * @param value    Value to set. 
     */
    public void setMem8(byte value) throws MemoryException {
        if (m_memAddress != null) {
            m_memory.writeByte(m_memAddress, value);
        } else {
            m_regs.setReg8(m_memIndex, value);
        }
    }

    /**
     * Assuming the opcode operand referred to a 16bit register, sets the
     * corresponding register's value.
     * @param value    New value for the segment register.
     */
    public void setReg16(short value) {
        m_regs.setReg16(m_regIndex, value);
    }

    /**
     * Assuming the opcode operand referred to a segment register, sets the
     * corresponding register's value.
     * @param value    New value for the segment register.
     */
    public void setSeg(short value) {
        m_regs.setSeg(m_regIndex, value);
    }

    /**
     * Sets the 16bit value pointed by the indirect memory operand (or register,
     * depands on the indirect-addressing mode).
     * @param value    Value to set. 
     */
    public void setMem16(short value) throws MemoryException {
        if (m_memAddress != null) {
            m_memory.writeWord(m_memAddress, value);
        } else {
            m_regs.setReg16(m_memIndex, value);
        }
    }

    /**
     * Decodes the indirect-memory operand corresponding to mode #0.
     * @return the real-mode address to which the indirect-memory operand
     *         refers to.
     * @throws MemoryException on any error while reading from memory.
     */
    private RealModeAddress getMode0Address() throws MemoryException {
        switch (m_memIndex) {
            case 0:
                return new RealModeAddress(
                    m_state.getDS(), (short)(m_state.getBX() + m_state.getSI()));
            case 1:
                return new RealModeAddress(
                    m_state.getDS(), (short)(m_state.getBX() + m_state.getDI()));
            case 2:
                return new RealModeAddress(
                    m_state.getSS(), (short)(m_state.getBP() + m_state.getSI()));
            case 3:
                return new RealModeAddress(
                    m_state.getSS(), (short)(m_state.getBP() + m_state.getDI()));
            case 4:
                return new RealModeAddress(m_state.getDS(), m_state.getSI());
            case 5:
                return new RealModeAddress(m_state.getDS(), m_state.getDI());
            case 6:
                return new RealModeAddress(m_state.getDS(), m_fetcher.nextWord());
            case 7:
                return new RealModeAddress(m_state.getDS(), m_state.getBX());
            default:
                throw new RuntimeException();			
        }
    }

    /**
     * Decodes the indirect-memory operand corresponding to mode #1.
     * @return the real-mode address to which the indirect-memory operand
     *         refers to.
     * @throws MemoryException on any error while reading from memory.
     */
    private RealModeAddress getMode1Address() throws MemoryException {
        switch (m_memIndex) {
            case 0:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getBX() + m_state.getSI() + m_fetcher.nextByte()));
            case 1:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getBX() + m_state.getDI() + m_fetcher.nextByte()));
            case 2:
                return new RealModeAddress(m_state.getSS(),
                    (short)(m_state.getBP() + m_state.getSI() + m_fetcher.nextByte()));
            case 3:
                return new RealModeAddress(m_state.getSS(),
                    (short)(m_state.getBP() + m_state.getDI() + m_fetcher.nextByte()));
            case 4:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getSI() + m_fetcher.nextByte()));
            case 5:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getDI() + m_fetcher.nextByte()));
            case 6:
                return new RealModeAddress(m_state.getSS(),
                    (short)(m_state.getBP() + m_fetcher.nextByte()));
            case 7:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getBX() + m_fetcher.nextByte()));
            default:
                throw new RuntimeException();			
        }
    }

    /**
     * Decodes the indirect-memory operand corresponding to mode #2.
     * @return the real-mode address to which the indirect-memory operand
     *         refers to.
     * @throws MemoryException on any error while reading from memory.
     */
    private RealModeAddress getMode2Address() throws MemoryException {
        switch (m_memIndex) {
            case 0:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getBX() + m_state.getSI() + m_fetcher.nextWord()));
            case 1:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getBX() + m_state.getDI() + m_fetcher.nextWord()));
            case 2:
                return new RealModeAddress(m_state.getSS(),
                    (short)(m_state.getBP() + m_state.getSI() + m_fetcher.nextWord()));
            case 3:
                return new RealModeAddress(m_state.getSS(),
                    (short)(m_state.getBP() + m_state.getDI() + m_fetcher.nextWord()));
            case 4:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getSI() + m_fetcher.nextWord()));
            case 5:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getDI() + m_fetcher.nextWord()));
            case 6:
                return new RealModeAddress(m_state.getSS(),
                    (short)(m_state.getBP() + m_fetcher.nextWord()));
            case 7:
                return new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getBX() + m_fetcher.nextWord()));
            default:
                throw new RuntimeException();			
        }
    }

    /**
     * Decodes the indirect-memory operand corresponding to mode #3.
     * Since in this mode the indirect-memory operand actually referes to one
     * of the registers, the method simply returns 'null'.
     * @return null (meaning the indirect operand refers to a register).
     */
    private RealModeAddress getMode3Address() {
        return null;
    }

    /** CPU registers */
    private final CpuState m_state;

    /** Memory */
    private final RealModeMemory m_memory;

    /** Used to fetch additional opcode bytes. */
    private final OpcodeFetcher m_fetcher;

    /** Used to decode the non-memory part of the opcode */
    private final RegisterIndexingDecoder m_regs;

    private byte m_regIndex;
    private byte m_memIndex;
    private RealModeAddress m_memAddress;
}
