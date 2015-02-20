/*
 * IndirectAddressingDecoder.java
 *
 * Copyright (C) 2005 - 2006 Danny Leshem <dleshem@users.sourceforge.net>
 * Copyright (C) 2006 - 2008 Erdem Güven <zuencap@users.sourceforge.net>.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package seksen.util.disasm;

import seksen.util.Hex;

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
 * @author Erdem Guven
 */
public class IndirectAddressingDecoder {
    private Disassembler disasm;
    private byte m_regIndex;
    private byte m_memIndex;
    private String m_memAddress;

    /**
     * Constructor.
     * @param state    CPU registers.
     * @param memory   Memory.
     * @param fetcher  Used to fetch additional opcode bytes.
     */
    public IndirectAddressingDecoder(Disassembler disasm) {
        this.disasm = disasm;
        m_regIndex = 0;
        m_memIndex = 0;
        m_memAddress = null;
    }

    /**
     * Fetches & decodes the bytes currently pointed by the OpcodeFetcher.
     * @throws MemoryException on any error while reading from memory.
     */
    public void reset() {

        // read the 'mode' byte (MM RRR III)
        // M - indirect addressing mode mux
        // R - register indexing
        // I - indirect addressing indexing
        byte modeByte = disasm.nextByte();

        byte mode = (byte) ((modeByte >> 6) & 0x03);
        m_regIndex = (byte) ((modeByte >> 3) & 0x07);
        m_memIndex = (byte) (modeByte & 0x07);

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

        if(m_memAddress != null) {
        if(forcedSegReg != -1) {
        	m_memAddress = getSeg((byte) forcedSegReg) +":"+m_memAddress;
        }
        m_memAddress = "["+m_memAddress+"]";
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
    public String getMemAddress() {
        return m_memAddress;
    }

    public static String getReg8(byte index) {
        switch (index) {
            case 0:
                return "al";
            case 1:
                return "cl";
            case 2:
                return "dl";
            case 3:
                return "bl";
            case 4:
            	return "ah";
            case 5:
                return "ch";
            case 6:
                return "dh";
            case 7:
                return "bh";
            default:
                throw new RuntimeException();
        }
    }

    public static String getReg16(byte index) {
        switch (index) {
            case 0:
                return "ax";
            case 1:
                return "cx";
            case 2:
                return "dx";
            case 3:
                return "bx";
            case 4:
                return "sp";
            case 5:
                return "bp";
            case 6:
                return "si";
            case 7:
                return "di";
            default:
                throw new RuntimeException();
        }
    }

    public static String getSeg(byte index) {
        switch (index & 3) {
            case 0:
                return "es";
            case 1:
                return "cs";
            case 2:
                return "ss";
            case 3:
                return "ds";
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Assuming the opcode operand referred to an 8bit register, returns the
     * corresponding register's value.
     * @return 8bit register value.
     */
    public String getReg8() {
        return getReg8(m_regIndex);
    }

    /**
     * Returns the 8bit value pointed by the indirect memory operand (or register,
     * depands on the indirect-addressing mode).
     * @return Indirect address (or register) 8bit value.
     */
    public String getMem8() {
        if (m_memAddress != null) {
            return m_memAddress;
        }
        return getReg8(m_memIndex);
    }

    /**
     * Assuming the opcode operand referred to a 16bit register, returns the
     * corresponding register's value.
     * @return 16bit register value.
     */
    public String getReg16() {
        return getReg16(m_regIndex);
    }

    /**
     * Assuming the opcode operand referred to a segment register, returns the
     * corresponding register's value.
     * @return segment register value.
     */
    public String getSeg() {
        return getSeg(m_regIndex);
    }

    /**
     * Returns the 16bit value pointed by the indirect memory operand (or register,
     * depands on the indirect-addressing mode).
     * @return Indirect address (or register) 16bit value.
     */
    public String getMem16() {
        if (m_memAddress != null) {
            return m_memAddress;
        }
        return getReg16(m_memIndex);
    }

    /**
     * Mode1 example: ADD [BX+SI], AL
     *
     * @param mode   Mode byte (@see c'tor).
     * @throws MemoryException
     */
    /**
     * Decodes the indirect-memory operand corresponding to mode #0.
     * @return the real-mode address to which the indirect-memory operand
     *         refers to.
     * @throws MemoryException on any error while reading from memory.
     */
    private String getMode0Address() {
        switch (m_memIndex) {
            case 0:
                return "bx+si";

            case 1:
                return "bx+di";

            case 2:
                return "bp+si";

            case 3:
                return "bp+di";

            case 4:
                return "si";

            case 5:
                return "di";

            case 6:
                return "0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);

            case 7:
                return "bx";//fff8

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
    private String getMode1Address() {
        switch (m_memIndex) {
            case 0:
		return "bx+si"+Hex.toHexStringSigned(disasm.nextByte(),4);//fe26 ff8a

            case 1:
		return "bx+di"+Hex.toHexStringSigned(disasm.nextByte(),4);

            case 2:
		return "bp+si"+Hex.toHexStringSigned(disasm.nextByte(),2);

            case 3:
		return "bp+di"+Hex.toHexStringSigned(disasm.nextByte(),2);//fd89

            case 4:
		return "si"+Hex.toHexStringSigned(disasm.nextByte(),2);//ffdf yi bu etkiliyor.

            case 5:
        return "di"+Hex.toHexStringSigned(disasm.nextByte(),2);

            case 6:
		return "bp"+Hex.toHexStringSigned(disasm.nextByte(),2);//fd7b fccd

            case 7:
		return "bx"+Hex.toHexStringSigned(disasm.nextByte(),2);//fcc4 fd72

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
    private String getMode2Address() {
        switch (m_memIndex) {
            case 0:
		return "bx+si+0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);//ff22 ff4e ff87 ff8d

            case 1:
		return "bx+di+0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);//fd13

            case 2:
		return "bp+si+0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);//ff55 ff93

            case 3:
		return "bp+di+0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);

           case 4:
		return "si+0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);

            case 5:
		return "di+0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);

            case 6:
		return "bp+0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);

            case 7:
		return "bx+0x"+Hex.toHexStringUnsigned(disasm.nextWord(),4);

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
    private String getMode3Address() {
        return null;
    }

    private int forcedSegReg = -1;

    void forceSegReg(int regno) {
        forcedSegReg = regno;
    }

 /*   Address newAddress(int segIndex, int offset) {
        if (forcedSegReg >= 0) {
            segIndex = forcedSegReg;
            forcedSegReg = -1;
        }
        int segment = m_regs.getSeg((byte) segIndex);
        return m_memory.newAddress(segment, offset);
    }
*/
















}
