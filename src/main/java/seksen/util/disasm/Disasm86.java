/*
 * Disasm86.java
 *
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

import seksen.hardware.Address;
import seksen.hardware.cpu.RegisterIndexingDecoder;
import seksen.util.Hex;

/**
 *
 * @author Erdem Guven
 * @author Yigit Ozen
 */
public class Disasm86 extends Disassembler {

	IndirectAddressingDecoder m_indirect;

	public Disasm86(Binary binary) {
		super(binary);
		m_indirect = new IndirectAddressingDecoder(this);
	}

	@Override
	public String nextLine() {
		byte opcode = nextByte();
		String str = processOpcode(opcode);
		if (str != null) {
			return str;
		}
		//return "db " + Hex.toHexString(opcode,2);  // Doğrusu bu..
		return "db " + Hex.toHexStringUnsigned(opcode, 8).toUpperCase();		//Test için

	}

	protected String processOpcode(byte opcode) {
		switch (opcode & 0xF0) {
			case 0x00:
				return opcode0X(opcode);
			case 0x10:
				return opcode1X(opcode);
			case 0x20:
				return opcode2X(opcode);
			case 0x30:
				return opcode3X(opcode);
			case 0x40:
				return opcode4X(opcode);
			case 0x50:
				return opcode5X(opcode);
			case 0x60:
				return opcode6X(opcode);
			case 0x70:
				return opcode7X(opcode);
			case 0x80:
				return opcode8X(opcode);
			case 0x90:
				return opcode9X(opcode);
			case 0xA0:
				return opcodeAX(opcode);
			case 0xB0:
				return opcodeBX(opcode);
			case 0xC0:
				return opcodeCX(opcode);
			case 0xD0:
				return opcodeDX(opcode);
			case 0xE0:
				return opcodeEX(opcode);
			case 0xF0:
				return opcodeFX(opcode);


		}
		return null;
	}

	protected String opcode0X(byte opcode) {
		switch (opcode) {
			case (byte) 0x00: // ADD [X], reg8
				m_indirect.reset();
				return "add " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x01: // ADD [X], reg16
				m_indirect.reset();
				return "add " + m_indirect.getMem16() + "," + m_indirect.getReg16();

			case (byte) 0x02: // ADD reg8, [X]
				m_indirect.reset();
				return "add " + m_indirect.getReg8() + "," + m_indirect.getMem8();

			case (byte) 0x03: // ADD reg16, [X]
				m_indirect.reset();
				return "add " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x04: // ADD AL, imm8
				return "add al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0x05: // ADD AX, imm16
				return "add ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0x06: // PUSH ES
				return "push es";

			case (byte) 0x07: // POP ES
				return "pop es";

			case (byte) 0x08: // OR [X], reg8
				m_indirect.reset();
				return "or " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x09: // OR [X], reg16
				m_indirect.reset();
				return "or " + m_indirect.getMem16() + "," + m_indirect.getReg16();

			case (byte) 0x0A: // OR reg8, [X]
				m_indirect.reset();
				return "or " + m_indirect.getReg8() + "," + m_indirect.getMem8();

			case (byte) 0x0B: // OR reg16, [X]
				m_indirect.reset();
				return "or " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x0C: // OR AL, imm8
				return "or al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0x0D: // OR AX, imm16
				return "or ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0x0E: // PUSH CS
				return "push cs";

			case (byte) 0x0F:
				return null;

			default:
				throw new RuntimeException();
		}
	}

	protected String opcode1X(byte opcode) {
		switch (opcode) {
			case (byte) 0x10: // ADC [X], reg8
				m_indirect.reset();
				return "adc " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x11: // ADC [X], reg16
				m_indirect.reset();
				return "adc " + m_indirect.getMem16() + "," + m_indirect.getReg16();


			case (byte) 0x12: // ADC reg8, [X]
				m_indirect.reset();
				return "adc " + m_indirect.getReg8() + "," + m_indirect.getMem8();


			case (byte) 0x13: // ADC reg16, [X]
				m_indirect.reset();
				return "adc " + m_indirect.getReg16() + "," + m_indirect.getMem16();


			case (byte) 0x14: // ADC AL, imm8
				return "adc al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);


			case (byte) 0x15: // ADC AX, imm16
				return "adc ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);


			case (byte) 0x16: // PUSH SS
				return "push ss";


			case (byte) 0x17: // POP SS
				return "pop ss";


			case (byte) 0x18: // SBB [X], reg8
				m_indirect.reset();
				return "sbb " + m_indirect.getMem8() + "," + m_indirect.getReg8();


			case (byte) 0x19: // SBB [X], reg16
				m_indirect.reset();
				return "sbb " + m_indirect.getMem16() + "," + m_indirect.getReg16();


			case (byte) 0x1A: // SBB reg8, [X]
				m_indirect.reset();
				return "sbb " + m_indirect.getReg8() + "," + m_indirect.getMem8();


			case (byte) 0x1B: // SBB reg16, [X]
				m_indirect.reset();
				return "sbb " + m_indirect.getReg16() + "," + m_indirect.getMem16();


			case (byte) 0x1C: // SBB AL, imm8
				return "sbb al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);


			case (byte) 0x1D: // SBB AX, imm16
				return "sbb ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0x1E: // PUSH DS
				return "push ds";

			case (byte) 0x1F: // POP DS
				return "pop ds";

			default:
				throw new RuntimeException();
		}
	}

	protected String opcode2X(byte opcode) {
		switch (opcode) {
			case (byte) 0x20: // AND [X], reg8
				m_indirect.reset();
				return "and " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x21: // AND [X], reg16
				m_indirect.reset();
				return "and " + m_indirect.getMem16() + "," + m_indirect.getReg16();

			case (byte) 0x22: // AND reg8, [X]
				m_indirect.reset();
				return "and " + m_indirect.getReg8() + "," + m_indirect.getMem8();

			case (byte) 0x23: // AND reg16, [X]
				m_indirect.reset();
				return "and " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x24: // AND AL, imm8
				return "and al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0x25: // AND AX, imm16
				return "and ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0x26: { // ES prefix
				m_indirect.forceSegReg(RegisterIndexingDecoder.ES_INDEX);
				String s = processOpcode(nextByte());
				m_indirect.forceSegReg(-1);
				return s;
			}

			case (byte) 0x27: // DAA
				return "daa";

			case (byte) 0x28: // SUB [X], reg8
				m_indirect.reset();
				return "sub " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x29: // SUB [X], reg16
				m_indirect.reset();
				return "sub " + m_indirect.getMem16() + "," + m_indirect.getReg16();

			case (byte) 0x2A: // SUB reg8, [X]
				m_indirect.reset();
				return "sub " + m_indirect.getReg8() + "," + m_indirect.getMem8();

			case (byte) 0x2B: // SUB reg16, [X]
				m_indirect.reset();
				return "sub " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x2C: // SUB AL, imm8
				return "sub al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0x2D: // SUB AX, imm16
				return "sub ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0x2E: { // 'CS:' prefix
				m_indirect.forceSegReg(RegisterIndexingDecoder.CS_INDEX);
				String s = processOpcode(nextByte());
				m_indirect.forceSegReg(-1);
				return s;
			}
			case (byte) 0x2F: // DAS
				return "das";

			default:
				throw new RuntimeException();
		}
	}

	protected String opcode3X(byte opcode) {
		switch (opcode) {

			case (byte) 0x30: // XOR [X], reg8
				m_indirect.reset();
				return "xor " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x31: // XOR [X], reg16
				m_indirect.reset();
				return "xor " + m_indirect.getMem16() + "," + m_indirect.getReg16();

			case (byte) 0x32: // XOR reg8, [X]
				m_indirect.reset();
				return "xor " + m_indirect.getReg8() + "," + m_indirect.getMem8();

			case (byte) 0x33: // XOR reg16, [X]
				m_indirect.reset();
				return "xor " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x34: // XOR AL, imm8
				return "xor al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0x35: // XOR AX, imm16
				return "xor ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0x36: {// 'SS:' prefix
				m_indirect.forceSegReg(RegisterIndexingDecoder.SS_INDEX);
				String s = processOpcode(nextByte());
				m_indirect.forceSegReg(-1);
				return s;
			}

			case (byte) 0x37: // TODO: AAA
				return "aaa";

			case (byte) 0x38: // CMP [X], reg8
				m_indirect.reset();
				return "cmp " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x39: // CMP [X], reg16
				m_indirect.reset();
				return "cmp " + m_indirect.getMem16() + "," + m_indirect.getReg16();

			case (byte) 0x3A: // CMP reg8, [X]
				m_indirect.reset();
				return "cmp " + m_indirect.getReg8() + "," + m_indirect.getMem8();

			case (byte) 0x3B: // CMP reg16, [X]
				m_indirect.reset();
				return "cmp " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x3C: // CMP AL, imm8
				return "cmp al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0x3D: // CMP AX, imm16
				return "cmp ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0x3E: { // 'DS:' prefix
				m_indirect.forceSegReg(RegisterIndexingDecoder.SS_INDEX);
				String s = processOpcode(nextByte());
				m_indirect.forceSegReg(-1);
				return "ds " + s;
			}

			case (byte) 0x3F: // TODO: AAS
				return "aas";

			default:
				throw new RuntimeException();
		}
	}

	protected String opcode4X(byte opcode) {
		byte index = (byte) (opcode & 0x07);
		switch (opcode) {
			case (byte) 0x40: // INC reg16
			case (byte) 0x41:
			case (byte) 0x42:
			case (byte) 0x43:
			case (byte) 0x44:
			case (byte) 0x45:
			case (byte) 0x46:
			case (byte) 0x47:
				return "inc " + IndirectAddressingDecoder.getReg16(index);

			case (byte) 0x48: // DEC reg16
			case (byte) 0x49:
			case (byte) 0x4A:
			case (byte) 0x4B:
			case (byte) 0x4C:
			case (byte) 0x4D:
			case (byte) 0x4E:
			case (byte) 0x4F:
				return "dec " + IndirectAddressingDecoder.getReg16(index);


			default:
				throw new RuntimeException();
		}
	}

	protected String opcode5X(byte opcode) {
		byte index = (byte) (opcode & 0x07);
		switch (opcode) {
			case (byte) 0x50: // PUSH reg16
			case (byte) 0x51:
			case (byte) 0x52:
			case (byte) 0x53:
			case (byte) 0x54:
			case (byte) 0x55:
			case (byte) 0x56:
			case (byte) 0x57:
				return "push " + IndirectAddressingDecoder.getReg16(index);



			case (byte) 0x58: // POP reg16
			case (byte) 0x59:
			case (byte) 0x5A:
			case (byte) 0x5B:
			case (byte) 0x5C:
			case (byte) 0x5D:
			case (byte) 0x5E:
			case (byte) 0x5F:
				return "pop " + IndirectAddressingDecoder.getReg16(index);

			default:
				throw new RuntimeException();
		}
	}

	protected String opcode6X(byte opcode) {
		// 0x60.. 0x6F - invalid opcodes
		return null;
	}

	protected String opcode7X(byte opcode) {
		int offset = (nextByte() % 0xff) + 2;
		switch (opcode) {
			case (byte) 0x70: // JO
				return "jo 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x71: // JNO
				return "jno 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x72: // JC,JB,JNAE
				return "jc 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x73: // JNC,JNC,JAE
				return "jnc 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x74: // JE,JZ
				return "jz 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x75: // JNE,JNZ
				return "jnz 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x76: // JBE,JNA
				return "jna 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x77: // JNBE,JA
				return "ja 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x78: // JS
				return "js 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x79: // JNS
				return "jns 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x7A: // JP,JPE
				return "jpe 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x7B: // JNP,JPO
				return "jpo 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x7C: // JL,JNGE
				return "jl 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x7D: // JNL,JGE
				return "jnl 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x7E: // JLE,JNG
				return "jng 0x" + Hex.toHexStringUnsigned(offset, 4);

			case (byte) 0x7F: // JNLE,JG
				return "jg 0x" + Hex.toHexStringUnsigned(offset, 4);

			default:
				throw new RuntimeException();

		}
	}

	protected String opcode8X(byte opcode) {
		switch (opcode) {
			case (byte) 0x80: // <?> byte ptr [X], imm8
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: { // ADD
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "add " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}

					case 1: { // OR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "or " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}

					case 2: { // ADC
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "adc " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}

					case 3: { // SBB
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "sbb " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}

					case 4: { // AND
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "and " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}

					case 5: { // SUB
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "sub " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}

					case 6: { // XOR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "xor " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}

					case 7: { // CMP
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "cmp " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}

					default:
						throw new RuntimeException();
				}

			case (byte) 0x81: // <?> word ptr [X], imm16
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: { // ADD
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "add " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					case 1: { // OR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "or " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					case 2: { // ADC
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "adc " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					case 3: { // SBB
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "sbb " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					case 4: { // AND
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "and " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					case 5: { // SUB
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "sub " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					case 6: { // XOR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "xor " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					case 7: { // CMP
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "cmp " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					default:
						throw new RuntimeException();
				}

			case (byte) 0x82:
				return null;

			case (byte) 0x83: // <?> word ptr [X], sign-extended imm8
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: { // ADD
						String mem = m_indirect.getMem16() + ",byte " + Hex.toHexStringSigned(nextByte(), 2);
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "add " + mem;
					}

					case 1: { // OR
						String mem = m_indirect.getMem16() + ",byte " + Hex.toHexStringSigned(nextByte(), 2);
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "or " + mem;
					}

					case 2: { // ADC
						String mem = m_indirect.getMem16() + ",byte " + Hex.toHexStringSigned(nextByte(), 2);
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "adc " + mem;
					}

					case 3: { // SBB
						String mem = m_indirect.getMem16() + ",byte " + Hex.toHexStringSigned(nextByte(), 2);
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "sbb " + mem;
					}

					case 4: { // AND
						String mem = m_indirect.getMem16() + ",byte " + Hex.toHexStringSigned(nextByte(), 2);
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "and " + mem;
					}

					case 5: { // SUB
						String mem = m_indirect.getMem16() + ",byte " + Hex.toHexStringSigned(nextByte(), 2);
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "sub " + mem;
					}

					case 6: { // XOR
						String mem = m_indirect.getMem16() + ",byte " + Hex.toHexStringSigned(nextByte(), 2);
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "xor " + mem;
					}

					case 7: { // CMP
						String mem = m_indirect.getMem16() + ",byte " + Hex.toHexStringSigned(nextByte(), 2);
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "cmp " + mem;
					}

					default:
						throw new RuntimeException();
				}

			case (byte) 0x84: // TEST reg8, [X]
				m_indirect.reset();
				return "test " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x85: // TEST reg16, [X]
				m_indirect.reset();
				return "test " + m_indirect.getMem16() + "," + m_indirect.getReg16();

			case (byte) 0x86: // XCHG reg8, [X]
				m_indirect.reset();
				return "xchg " + m_indirect.getReg8() + "," + m_indirect.getMem8();

			case (byte) 0x87: // XCHG reg16, [X]
				m_indirect.reset();
				return "xchg " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x88: // MOV [X], reg8
				m_indirect.reset();
				return "mov " + m_indirect.getMem8() + "," + m_indirect.getReg8();

			case (byte) 0x89: // MOV [X], reg16
				m_indirect.reset();
				return "mov " + m_indirect.getMem16() + "," + m_indirect.getReg16();

			case (byte) 0x8A: // MOV reg8, [X]
				m_indirect.reset();
				return "mov " + m_indirect.getReg8() + "," + m_indirect.getMem8();

			case (byte) 0x8B: // MOV reg16, [X]
				m_indirect.reset();
				return "mov " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x8C: // MOV [X], seg
				m_indirect.reset();
				return "mov " + m_indirect.getMem16() + "," + m_indirect.getSeg();

			case (byte) 0x8D: // LEA reg16, [X]
				m_indirect.reset();
				String address = m_indirect.getMemAddress();
				if (address == null) {
					// "LEA reg16, reg16" is an invalid opcode
					prevByte();
					return null;
				}
				return "lea " + m_indirect.getReg16() + "," + m_indirect.getMem16();

			case (byte) 0x8E: // MOV seg, [X]
				m_indirect.reset();
				return "mov " + m_indirect.getSeg() + "," + m_indirect.getMem16();

			case (byte) 0x8F: // POP [X]
				// Note: since Reg index bits are ignored, there are 8 different
				// machine-code representations for this opcode :-)
				m_indirect.reset();
				if (m_indirect.getRegIndex() != 0) {
					prevByte();
					return null;
				}
				return "pop word " + m_indirect.getMem16();

			default:
				throw new RuntimeException();
		}
	}

	protected String opcode9X(byte opcode) {
		switch (opcode) {
			case (byte) 0x90: // XCHG reg16, AX
				return "nop";

			case (byte) 0x91:
			case (byte) 0x92:
			case (byte) 0x93:
			case (byte) 0x94:
			case (byte) 0x95:
			case (byte) 0x96:
			case (byte) 0x97:
				byte index = (byte) (opcode & 0x07);
				return "xchg ax," + IndirectAddressingDecoder.getReg16(index);

			case (byte) 0x98: // CBW
				return "cbw";

			case (byte) 0x99: // CWD
				return "cwd";

			case (byte) 0x9A: // CALL far imm16:imm16
				String call1 = Hex.toHexStringUnsigned(nextWord(), 4);
				String call2 = Hex.toHexStringUnsigned(nextWord(), 4);
				return "call 0x" + call2 + ":0x" + call1;

			case (byte) 0x9B: // WAIT
				//Nothing to do
				return "wait";

			case (byte) 0x9C: // PUSHF
				return "pushf";

			case (byte) 0x9D: // POPF
				return "popf";

			case (byte) 0x9E:
				// TODO: handle reserved bits (1,3,5)
				return "sahf";

			case (byte) 0x9F: // LAHF
				return "lahf";

			default:
				throw new RuntimeException();
		}
	}

	protected String opcodeAX(byte opcode) {
		switch (opcode) {
			case (byte) 0xA0: // MOV AL, [imm16]
				return "mov al,[0x" + Hex.toHexStringUnsigned(nextWord(), 4) + "]";

			case (byte) 0xA1: // MOV AX, [imm16]
				return "mov ax,[0x" + Hex.toHexStringUnsigned(nextWord(), 4) + "]";

			case (byte) 0xA2: // MOV [imm16], AL
				return "mov [0x" + Hex.toHexStringUnsigned(nextWord(), 4) + "],al";

			case (byte) 0xA3: // MOV [imm16], AX
				return "mov [0x" + Hex.toHexStringUnsigned(nextWord(), 4) + "],ax";

			case (byte) 0xA4: // MOVSB
				return "movsb";

			case (byte) 0xA5: // MOVSW
				return "movsw";

			case (byte) 0xA6: // CMPSB
				return "cmpsb";

			case (byte) 0xA7: // CMPSW
				return "cmpsw";

			case (byte) 0xA8: // TEST AL, imm8
				return "test al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0xA9: // TEST AX, imm16
				return "test ax,0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0xAA: // STOSB
				return "stosb";

			case (byte) 0xAB: // STOSW
				return "stosw";

			case (byte) 0xAC: // LODSB
				return "lodsb";

			case (byte) 0xAD: // LODSW
				return "lodsw";

			case (byte) 0xAE: // SCASB
				return "scasb";

			case (byte) 0xAF: // SCASW
				return "scasw";

			default:
				throw new RuntimeException();
		}
	}

	protected String opcodeBX(byte opcode) {
		byte index = (byte) (opcode & 0x07);
		switch (opcode) {
			case (byte) 0xB0: // MOV reg8, imm8
			case (byte) 0xB1:
			case (byte) 0xB2:
			case (byte) 0xB3:
			case (byte) 0xB4:
			case (byte) 0xB5:
			case (byte) 0xB6:
			case (byte) 0xB7:
				return "mov " + IndirectAddressingDecoder.getReg8(index) + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0xB8: // MOV reg16, imm16
			case (byte) 0xB9:
			case (byte) 0xBA:
			case (byte) 0xBB:
			case (byte) 0xBC:
			case (byte) 0xBD:
			case (byte) 0xBE:
			case (byte) 0xBF:
				return "mov " + IndirectAddressingDecoder.getReg16(index) + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			default:
				throw new RuntimeException();
		}
	}

	protected String opcodeCX(byte opcode) {
		short sizeToPop;
		Address address1 = null;
		Address address2 = null;
		switch (opcode) {
			case (byte) 0xC0:
			case (byte) 0xC1:
				// 0xC0.. 0xC1 - invalid opcodes
				return null;

			case (byte) 0xC2: // RETN [imm16]
				return "ret 0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0xC3: // RETN
				return "ret";

			case (byte) 0xC4:{ // LES reg16, [X]
				m_indirect.reset();
				String address = m_indirect.getMemAddress();
				if (address == null) {
					// "LES reg16, reg16" is an invalid opcode
					prevByte();
					return null;
				}
				return "les " + m_indirect.getReg16() + "," + m_indirect.getMem16();
			}

			case (byte) 0xC5:{ // LDS reg16, [X]
				m_indirect.reset();
				String address = m_indirect.getMemAddress();
				if (address == null) {
					// "LDS reg16, reg16" is an invalid opcode
					prevByte();
					return null;
				}
				return "lds " + m_indirect.getReg16() + "," + m_indirect.getMem16();
			}

			case (byte) 0xC6: // MOV [X], imm8
				// Note: since Reg index bits are ignored, there are 8 different
				// machine-code representations for this opcode :-)
				m_indirect.reset();
				if (m_indirect.getRegIndex() != 0) {
					prevByte();
					return null;
				}
				return "mov byte " + m_indirect.getMem8() + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0xC7: // MOV [X], imm16
				// Note: since Reg index bits are ignored, there are 8 different
				// machine-code representations for this opcode :-)
				m_indirect.reset();
				if (m_indirect.getRegIndex() != 0) {
					prevByte();
					return null;
				}
				return "mov word " + m_indirect.getMem8() + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0xC8:
			case (byte) 0xC9:
				// 0xC8.. 0xC9 - invalid opcodes
				return null;

			case (byte) 0xCA: // RETF [imm16]
				return "retf 0x" + Hex.toHexStringUnsigned(nextWord(), 4);

			case (byte) 0xCB: // RETF
				return "retf";

			case (byte) 0xCC: // INT3
				return "int3";

			case (byte) 0xCD: // INT [imm8]
				return "int 0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0xCE: // INTO
				return "into";

			case (byte) 0xCF: // IRET
				return "iret";

			default:
				throw new RuntimeException();
		}
	}

	protected String opcodeDX(byte opcode) {
		switch (opcode) {
			case (byte) 0xD0: // <?> byte ptr [X], 1
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case (byte) 0x00: { // ROL
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "rol " + mem + ",1";
					}

					case (byte) 0x01: { // ROR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "ror " + mem + ",1";
					}

					case (byte) 0x02: { // RCL
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "rcl " + mem + ",1";
					}

					case (byte) 0x03: { // RCR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "rcr " + mem + ",1";
					}

					case (byte) 0x04: { // SHL
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "shl " + mem + ",1";
					}

					case (byte) 0x05: { // SHR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "shr " + mem + ",1";
					}

					case (byte) 0x06: // invalid opcode
						prevByte();
						return null;

					case (byte) 0x07: { // SAR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "sar " + mem + ",1";
					}

					default:
						throw new RuntimeException();
				}

			case (byte) 0xD1: // <?> word ptr [X], 1
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case (byte) 0x00: { // ROL
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "rol " + mem + ",1";
					}

					case (byte) 0x01: { // ROR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "ror " + mem + ",1";
					}

					case (byte) 0x02: { // RCL
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "rcl " + mem + ",1";
					}

					case (byte) 0x03: { // RCR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "rcr " + mem + ",1";
					}

					case (byte) 0x04: { // SHL
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "shl " + mem + ",1";
					}

					case (byte) 0x05: { // SHR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "shr " + mem + ",1";
					}

					case (byte) 0x06: // invalid opcode
						prevByte();
						return null;

					case (byte) 0x07: { // SAR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "sar " + mem + ",1";
					}

					default:
						throw new RuntimeException();
				}

			case (byte) 0xD2: // <?> byte ptr [X], CL
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case (byte) 0x00: { // ROL
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "rol " + mem + ",cl";
					}

					case (byte) 0x01: { // ROR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "ror " + mem + ",cl";
					}

					case (byte) 0x02: { // RCL
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "rcl " + mem + ",cl";
					}

					case (byte) 0x03: { // RCR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "rcr " + mem + ",cl";
					}

					case (byte) 0x04: { // SHL
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "shl " + mem + ",cl";
					}

					case (byte) 0x05: { // SHR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "shr " + mem + ",cl";
					}

					case (byte) 0x06: // invalid opcode
						prevByte();
						return null;

					case (byte) 0x07: { // SAR
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "sar " + mem + ",cl";
					}

					default:
						throw new RuntimeException();
				}

			case (byte) 0xD3: // <?> word ptr [x], CL
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case (byte) 0x00: { // ROL
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "rol " + mem + ",cl";
					}

					case (byte) 0x01: { // ROR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "ror " + mem + ",cl";
					}

					case (byte) 0x02: { // RCL
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "rcl " + mem + ",cl";
					}

					case (byte) 0x03: { // RCR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "rcr " + mem + ",cl";
					}

					case (byte) 0x04: { // SHL
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "shl " + mem + ",cl";
					}

					case (byte) 0x05: { // SHR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "shr " + mem + ",cl";
					}

					case (byte) 0x06: // invalid opcode
						prevByte();
						return null;

					case (byte) 0x07: { // SAR
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "sar " + mem + ",cl";
					}

					default:
						throw new RuntimeException();
				}

			case (byte) 0xD4: { // AAM
				int aam = nextByte();
				if (aam == 10) {
					return "aam";
				} else {
					return "aam 0x" + Hex.toHexStringUnsigned(aam, 2);
				}
			}

			case (byte) 0xD5: { // AAD
				int aam = nextByte();
				if (aam == 10) {
					return "aad";
				} else {
					return "aad 0x" + Hex.toHexStringUnsigned(aam, 2);
				}
			}

			case (byte) 0xD6:
				return null;

			case (byte) 0xD7: // XLAT, XLATB
				return "xlatb";

			case (byte) 0xD8: // FADD dword
				//return "fadd dword " + m_indirect.getMem8();
				return null;

			case (byte) 0xD9: // FLD dword
				//return "fld dword " + m_indirect.getMem8();
				return null;

			case (byte) 0xDA: // FIADD dword
				return null;

			case (byte) 0xDB: // FILD dword
				//m_indirect.reset();
				//return "fild dword " + m_indirect.getMem8();
				return null;

			case (byte) 0xDC: // FADD qword
				//m_indirect.reset();
				//return "fadd qword " + m_indirect.getMem8();
				return null;

			case (byte) 0xDD: // FLD qword
				//m_indirect.reset();
				//return "fisttp qword " + m_indirect.getMem8();
				return null;

			case (byte) 0xDE: // FIADD word
				//m_indirect.reset();
				//return "fiadd word " + m_indirect.getMem8();
				return null;

			case (byte) 0xDF: // FILD word
				return null;

			default:
				throw new RuntimeException();
		}
	}

	protected String opcodeEX(byte opcode) {
		switch (opcode) {
			case (byte) 0xE0: // LOOPNZ, LOOPNE
				return "loopne 0x" + Hex.toHexStringUnsigned(nextByte() + binary.getOffset(), 4);

			case (byte) 0xE1: // LOOPZ, LOOPE
				return "loope 0x" + Hex.toHexStringUnsigned(nextByte() + binary.getOffset(), 4);

			case (byte) 0xE2: // LOOP
				return "loop 0x" + Hex.toHexStringUnsigned(nextByte() + binary.getOffset(), 4);

			case (byte) 0xE3: // JCXZ
				return "jcxz 0x" + Hex.toHexStringUnsigned(nextByte() + binary.getOffset(), 4);

			case (byte) 0xE4: // IN AL, imm8
				return "in al,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0xE5: // IN AX, imm8
				return "in ax,0x" + Hex.toHexStringUnsigned(nextByte(), 2);

			case (byte) 0xE6: // OUT imm8, AL
				return "out 0x" + Hex.toHexStringUnsigned(nextByte(), 2) + ",al";

			case (byte) 0xE7: // OUT imm8, AX
				return "out 0x" + Hex.toHexStringUnsigned(nextByte(), 2) + ",ax";

			case (byte) 0xE8: // CALL near imm16
				return "call 0x" + Hex.toHexStringUnsigned(nextWord() + binary.getOffset(), 4);

			case (byte) 0xE9: // JMP  imm16
				return "jmp 0x" + Hex.toHexStringUnsigned(nextWord() + binary.getOffset(), 4);

			case (byte) 0xEA: // JMP far imm16:imm16
				String jmp1 = Hex.toHexStringUnsigned(nextWord(), 4);
				String jmp2 = Hex.toHexStringUnsigned(nextWord(), 4);
				return "jmp 0x" + jmp2 + ":0x" + jmp1;

			case (byte) 0xEB: // JMP short imm8
				return "jmp short 0x" + Hex.toHexStringUnsigned(nextByte() + binary.getOffset(), 4);

			case (byte) 0xEC: // IN AL, DX
				return "in al,dx";

			case (byte) 0xED: // IN AX, DX
				return "in ax,dx";

			case (byte) 0xEE: // OUT DX, AL
				return "out dx,al";

			case (byte) 0xEF: // OUT DX, AX
				return "out dx,ax";

			default:
				throw new RuntimeException();
		}
	}

	protected String opcodeFX(byte opcode) {
		switch (opcode) {
			case (byte) 0xF0: // LOCK
				return "lock " + processOpcode(nextByte());

			case (byte) 0xF1:
				return null;

			case (byte) 0xF2: // REPNZ
				return "repne " + processOpcode(nextByte());

			case (byte) 0xF3: { // REP, REPZ
				boolean loopdone = false;
				return "rep " + processOpcode(nextByte());
			}

			case (byte) 0xF4: // HLT
				return "hlt";

			case (byte) 0xF5: // CMC
				return "cmc";

			case (byte) 0xF6: // <?> byte ptr [X]
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: { // TEST imm8
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "test " + mem + ",0x" + Hex.toHexStringUnsigned(nextByte(), 2);
					}
					case 1:
						prevByte();
						return null;

					case 2: { // NOT
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "not " + mem;
					}

					case 3: { // NEG
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "neg " + mem;
					}

					case 4: { // MUL
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "mul " + mem;
					}

					case 5: { // TODO: IMUL
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "imul " + mem;
					}

					case 6: { // DIV
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "div " + mem;
					}

					case 7: { // TODO: IDIV
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "idiv " + mem;
					}

					default:
						throw new RuntimeException();
				}
			case (byte) 0xF7: // <?> word ptr [X]
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: { // TEST imm16
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "test " + mem + ",0x" + Hex.toHexStringUnsigned(nextWord(), 4);
					}

					case 1:
						prevByte();
						return null;

					case 2: { // NOT
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "not " + mem;
					}

					case 3: { // NEG
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "neg " + mem;
					}

					case 4: { // MUL
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "mul " + mem;
					}

					case 5: { // TODO: check IMUL
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "imul " + mem;
					}

					case 6: { // DIV
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "div " + mem;
					}

					case 7: { // TODO: check IDIV
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "idiv " + mem;
					}

					default:
						throw new RuntimeException();
				}

			case (byte) 0xF8: // CLC
				return "clc";

			case (byte) 0xF9: // STC
				return "stc";

			case (byte) 0xFA: // CLI
				return "cli";

			case (byte) 0xFB: // STI
				return "sti";

			case (byte) 0xFC: // CLD
				return "cld";

			case (byte) 0xFD: // STD
				return "std";

			case (byte) 0xFE: // <?> byte ptr [X]
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: { // INC
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "inc " + mem;
					}

					case 1: { // DEC
						String mem = m_indirect.getMem8();
						if (mem.charAt(0) == '[') {
							mem = "byte " + mem;
						}
						return "dec " + mem;
					}

					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						// invalid opcodes
						prevByte();
						return null;

					default:
						throw new RuntimeException();
				}

			case (byte) 0xFF: // <?> word ptr [X]
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: { // INC
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "inc " + mem;
					}

					case 1: { // DEC
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "dec " + mem;
					}

					case 2: { // CALL
						String mem = m_indirect.getMem16();
						/*if (mem.charAt(0) == '[') {
						mem = "word " + mem;
						}*/
						return "call near " + mem;
					}

					case 3: { // CALL far
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) != '[') {
							mem = "word " + mem;
						}
						return "call far " + mem;
					}

					case 4: { // JMP near
						// FIXME: JMP SP bug ?
						String mem = m_indirect.getMem16();
						/*if (mem.charAt(0) == '[') {
						mem = "word " + mem;
						}*/
						return "jmp near " + mem;
					}

					case 5: { // JMP far
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) != '[') {
							mem = "word " + mem;
						}
						return "jmp far " + mem;
					}

					case 6: { // PUSH
						String mem = m_indirect.getMem16();
						if (mem.charAt(0) == '[') {
							mem = "word " + mem;
						}
						return "push " + mem;
					}

					case 7: // invalid opcode
						prevByte();
						return null;

					default:
						throw new RuntimeException();
				}
			default:
				throw new RuntimeException();
		}
	}
}
