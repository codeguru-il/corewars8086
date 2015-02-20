/*
 * Cpu8086.java
 *
 * Copyright (C) 2005 - 2006 Danny Leshem <dleshem@users.sourceforge.net>
 * Copyright (C) 2006 - 2008 Erdem Güven <zuencap@users.sourceforge.net>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package seksen.hardware.cpu;

import seksen.hardware.Address;
import seksen.hardware.IOHandler;
import seksen.hardware.InterruptException;
import seksen.hardware.InterruptHandler;
import seksen.hardware.Machine;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.RealModeMemory;
import seksen.util.Unsigned;

/**
 * Implements a 8086 CPU.
 *
 * @author DL
 * @author Erdem Guven
 */
public class Cpu8086 extends Cpu {

	protected Machine m_machine;
	protected CpuState prevState;
	protected IOHandler ioHandler;
	protected InterruptHandler intHandler;

	/**
	 * Constructor.
	 *
	 * @param state     Startup state of CPU registers & flags.
	 * @param memory    Real-Mode memory to use.
	 */
	public void setMachine(Machine machine) {
		this.m_machine = machine;

		m_state = machine.state;
		m_memory = machine.memory;
		ioHandler = (IOHandler) machine.getDevice(IOHandler.class);
		intHandler = (InterruptHandler) m_machine.getDevice(InterruptHandler.class);

		m_fetcher = new OpcodeFetcher(m_state, m_memory);
		m_regs = new RegisterIndexingDecoder(m_state);
		m_indirect = new IndirectAddressingDecoder(m_machine, m_fetcher);
	}

	/* (non-Javadoc)
	 * @see corewars.cpu.Cpu#nextOpcode()
	 */
	public synchronized void nextOpcode() throws CpuException, MemoryException {
		m_memory.setAccessListenersEnabled(true);

		prevState = (CpuState) m_state.clone();

		try{
			byte opcode = m_fetcher.nextByte();
			processOpcode(opcode);
		} catch(CpuException e) {
			gotoPrevState();
			throw e;
		} catch(MemoryException e) {
			gotoPrevState();
			throw e;
		} finally {
			instructionCounter++;

			m_indirect.forceSegReg(-1);

			m_memory.setAccessListenersEnabled(false);
		}
	}

	protected void gotoPrevState() {
		m_state.set(prevState);
	}

	protected void processOpcode(byte opcode) throws CpuException, MemoryException {
		switch(opcode & 0xF0) {
			case 0x00:
				opcode0X(opcode);
				break;
			case 0x10:
				opcode1X(opcode);
				break;
			case 0x20:
				opcode2X(opcode);
				break;
			case 0x30:
				opcode3X(opcode);
				break;
			case 0x40:
				opcode4X(opcode);
				break;
			case 0x50:
				opcode5X(opcode);
				break;
			case 0x60:
				opcode6X(opcode);
				break;
			case 0x70:
				opcode7X(opcode);
				break;
			case 0x80:
				opcode8X(opcode);
				break;
			case 0x90:
				opcode9X(opcode);
				break;
			case 0xA0:
				opcodeAX(opcode);
				break;
			case 0xB0:
				opcodeBX(opcode);
				break;
			case 0xC0:
				opcodeCX(opcode);
				break;
			case 0xD0:
				opcodeDX(opcode);
				break;
			case 0xE0:
				opcodeEX(opcode);
				break;
			case 0xF0:
				opcodeFX(opcode);
				break;
		}
	}

	/* (non-Javadoc)
	 * @see corewars.cpu.Cpu#interrupt(int)
	 */
	public void interrupt( int intnum ) throws InterruptException, MemoryException {
		if( intHandler != null ) {
			intHandler.handleInt( intnum );
		} else {
			push(m_state.getFlags());
			push(m_state.getCS());
			push(m_state.getIP());

			Address address = m_machine.newAddress(0,(short)(intnum*4));
			int offset = m_memory.readWord(address);
			int segment = m_memory.readWord(address.addOffset(2));
			m_state.setCS(segment);
			m_state.setIP(offset);
		}
	}

	protected void out(int port, short data) {
	    if( ioHandler != null ) {
		ioHandler.outw(port,data);
	    }
	}

	protected int in(int port) {
	    if( ioHandler != null ) {
		return ioHandler.inw(port);
	    }
	    return 0;
	}

	protected void opcode0X(byte opcode) throws CpuException, MemoryException {
		switch (opcode) {
			case (byte)0x00: // ADD [X], reg8
				m_indirect.reset();
				m_indirect.setMem8(add8(m_indirect.getMem8(), m_indirect.getReg8()));
				break;
			case (byte)0x01: // ADD [X], reg16
				m_indirect.reset();
				m_indirect.setMem16(add16(m_indirect.getMem16(), m_indirect.getReg16()));
				break;
			case (byte)0x02: // ADD reg8, [X]
				m_indirect.reset();
				m_indirect.setReg8(add8(m_indirect.getReg8(), m_indirect.getMem8()));
				break;
			case (byte)0x03: // ADD reg16, [X]
				m_indirect.reset();
				m_indirect.setReg16(add16(m_indirect.getReg16(), m_indirect.getMem16()));
				break;
			case (byte)0x04: // ADD AL, imm8
				m_state.setAL(add8(m_state.getAL(), m_fetcher.nextByte()));
				break;
			case (byte)0x05: // ADD AX, imm16
				m_state.setAX(add16(m_state.getAX(), m_fetcher.nextWord()));
				break;
			case (byte)0x06: // PUSH ES
				push(m_state.getES());
				break;
			case (byte)0x07: // POP ES
				m_state.setES(pop());
				break;
			case (byte)0x08: // OR [X], reg8
				m_indirect.reset();
				m_indirect.setMem8(or8(m_indirect.getMem8(), m_indirect.getReg8()));
				break;
			case (byte)0x09: // OR [X], reg16
				m_indirect.reset();
				m_indirect.setMem16(or16(m_indirect.getMem16(), m_indirect.getReg16()));
				break;
			case (byte)0x0A: // OR reg8, [X]
				m_indirect.reset();
				m_indirect.setReg8(or8(m_indirect.getReg8(), m_indirect.getMem8()));
				break;
			case (byte)0x0B: // OR reg16, [X]
				m_indirect.reset();
				m_indirect.setReg16(or16(m_indirect.getReg16(), m_indirect.getMem16()));
				break;
			case (byte)0x0C: // OR AL, imm8
				m_state.setAL(or8(m_state.getAL(), m_fetcher.nextByte()));
				break;
			case (byte)0x0D: // OR AX, imm16
				m_state.setAX(or16(m_state.getAX(), m_fetcher.nextWord()));
				break;
			case (byte)0x0E: // PUSH CS
				push(m_state.getCS());
				break;
			case (byte)0x0F:
				// 0x0F - invalid opcode
				throw new InvalidOpcodeException();
			default:
				throw new RuntimeException();
		}
	}

	protected void opcode1X(byte opcode) throws MemoryException {
		switch (opcode) {
			case (byte)0x10: // ADC [X], reg8
				m_indirect.reset();
				m_indirect.setMem8(adc8(m_indirect.getMem8(), m_indirect.getReg8()));
				break;
			case (byte)0x11: // ADC [X], reg16
				m_indirect.reset();
				m_indirect.setMem16(adc16(m_indirect.getMem16(), m_indirect.getReg16()));
				break;
			case (byte)0x12: // ADC reg8, [X]
				m_indirect.reset();
				m_indirect.setReg8(adc8(m_indirect.getReg8(), m_indirect.getMem8()));
				break;
			case (byte)0x13: // ADC reg16, [X]
				m_indirect.reset();
				m_indirect.setReg16(adc16(m_indirect.getReg16(), m_indirect.getMem16()));
				break;
			case (byte)0x14: // ADC AL, imm8
				m_state.setAL(adc8(m_state.getAL(), m_fetcher.nextByte()));
				break;
			case (byte)0x15: // ADC AX, imm16
				m_state.setAX(adc16(m_state.getAX(), m_fetcher.nextWord()));
				break;
			case (byte)0x16: // PUSH SS
				push(m_state.getSS());
				break;
			case (byte)0x17: // POP SS
				m_state.setSS(pop());
				break;
			case (byte)0x18: // SBB [X], reg8
				m_indirect.reset();
				m_indirect.setMem8(sbb8(m_indirect.getMem8(), m_indirect.getReg8()));
				break;
			case (byte)0x19: // SBB [X], reg16
				m_indirect.reset();
				m_indirect.setMem16(sbb16(m_indirect.getMem16(), m_indirect.getReg16()));
				break;
			case (byte)0x1A: // SBB reg8, [X]
				m_indirect.reset();
				m_indirect.setReg8(sbb8(m_indirect.getReg8(), m_indirect.getMem8()));
				break;
			case (byte)0x1B: // SBB reg16, [X]
				m_indirect.reset();
				m_indirect.setReg16(sbb16(m_indirect.getReg16(), m_indirect.getMem16()));
				break;
			case (byte)0x1C: // SBB AL, imm8
				m_state.setAL(sbb8(m_state.getAL(), m_fetcher.nextByte()));
				break;
			case (byte)0x1D: // SBB AX, imm16
				m_state.setAX(sbb16(m_state.getAX(), m_fetcher.nextWord()));
				break;
			case (byte)0x1E: // PUSH DS
				push(m_state.getDS());
				break;
			case (byte)0x1F: // POP DS
				m_state.setDS(pop());
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcode2X(byte opcode) throws CpuException, MemoryException {
		switch (opcode) {
			case (byte)0x20: // AND [X], reg8
				m_indirect.reset();
				m_indirect.setMem8(and8(m_indirect.getMem8(), m_indirect.getReg8()));
				break;
			case (byte)0x21: // AND [X], reg16
				m_indirect.reset();
				m_indirect.setMem16(and16(m_indirect.getMem16(), m_indirect.getReg16()));
				break;
			case (byte)0x22: // AND reg8, [X]
				m_indirect.reset();
				m_indirect.setReg8(and8(m_indirect.getReg8(), m_indirect.getMem8()));
				break;
			case (byte)0x23: // AND reg16, [X]
				m_indirect.reset();
				m_indirect.setReg16(and16(m_indirect.getReg16(), m_indirect.getMem16()));
				break;
			case (byte)0x24: // AND AL, imm8
				m_state.setAL(and8(m_state.getAL(), m_fetcher.nextByte()));
				break;
			case (byte)0x25: // AND AX, imm16
				m_state.setAX(and16(m_state.getAX(), m_fetcher.nextWord()));
				break;
			case (byte)0x26: // ES prefix
				m_indirect.forceSegReg( RegisterIndexingDecoder.ES_INDEX );
				processOpcode(m_fetcher.nextByte());
				break;
			case (byte)0x27: // TODO: DAA
				throw new UnimplementedOpcodeException();
			case (byte)0x28: // SUB [X], reg8
				m_indirect.reset();
				m_indirect.setMem8(sub8(m_indirect.getMem8(), m_indirect.getReg8()));
				break;
			case (byte)0x29: // SUB [X], reg16
				m_indirect.reset();
				m_indirect.setMem16(sub16(m_indirect.getMem16(), m_indirect.getReg16()));
				break;
			case (byte)0x2A: // SUB reg8, [X]
				m_indirect.reset();
				m_indirect.setReg8(sub8(m_indirect.getReg8(), m_indirect.getMem8()));
				break;
			case (byte)0x2B: // SUB reg16, [X]
				m_indirect.reset();
				m_indirect.setReg16(sub16(m_indirect.getReg16(), m_indirect.getMem16()));
				break;
			case (byte)0x2C: // SUB AL, imm8
				m_state.setAL(sub8(m_state.getAL(), m_fetcher.nextByte()));
				break;
			case (byte)0x2D: // SUB AX, imm16
				m_state.setAX(sub16(m_state.getAX(), m_fetcher.nextWord()));
				break;
			case (byte)0x2E: // 'CS:' prefix
				m_indirect.forceSegReg( RegisterIndexingDecoder.CS_INDEX );
				processOpcode(m_fetcher.nextByte());
				break;
			case (byte)0x2F: // TODO: DAS
				throw new UnimplementedOpcodeException();
			default:
				throw new RuntimeException();
		}
	}

	protected void opcode3X(byte opcode) throws CpuException, MemoryException {
		switch (opcode) {
			case (byte)0x30: // XOR [X], reg8
				m_indirect.reset();
				m_indirect.setMem8(xor8(m_indirect.getMem8(), m_indirect.getReg8()));
				break;
			case (byte)0x31: // XOR [X], reg16
				m_indirect.reset();
				m_indirect.setMem16(xor16(m_indirect.getMem16(), m_indirect.getReg16()));
				break;
			case (byte)0x32: // XOR reg8, [X]
				m_indirect.reset();
				m_indirect.setReg8(xor8(m_indirect.getReg8(), m_indirect.getMem8()));
				break;
			case (byte)0x33: // XOR reg16, [X]
				m_indirect.reset();
				m_indirect.setReg16(xor16(m_indirect.getReg16(), m_indirect.getMem16()));
				break;
			case (byte)0x34: // XOR AL, imm8
				m_state.setAL(xor8(m_state.getAL(), m_fetcher.nextByte()));
				break;
			case (byte)0x35: // XOR AX, imm16
				m_state.setAX(xor16(m_state.getAX(), m_fetcher.nextWord()));
				break;
			case (byte)0x36: // 'SS:' prefix
				m_indirect.forceSegReg( RegisterIndexingDecoder.SS_INDEX );
				processOpcode(m_fetcher.nextByte());
				break;
			case (byte)0x37: // TODO: AAA
				throw new UnimplementedOpcodeException();
			case (byte)0x38: // CMP [X], reg8
				m_indirect.reset();
				sub8(m_indirect.getMem8(), m_indirect.getReg8());
				break;
			case (byte)0x39: // CMP [X], reg16
				m_indirect.reset();
				sub16(m_indirect.getMem16(), m_indirect.getReg16());
				break;
			case (byte)0x3A: // CMP reg8, [X]
				m_indirect.reset();
				sub8(m_indirect.getReg8(), m_indirect.getMem8());
				break;
			case (byte)0x3B: // CMP reg16, [X]
				m_indirect.reset();
				sub16(m_indirect.getReg16(), m_indirect.getMem16());
				break;
			case (byte)0x3C: // CMP AL, imm8
				sub8(m_state.getAL(), m_fetcher.nextByte());
				break;
			case (byte)0x3D: // CMP AX, imm16
				sub16(m_state.getAX(), m_fetcher.nextWord());
				break;
			case (byte)0x3E: // 'DS:' prefix
				m_indirect.forceSegReg( RegisterIndexingDecoder.DS_INDEX );
				processOpcode(m_fetcher.nextByte());
				break;
			case (byte)0x3F: // TODO: AAS
				throw new UnimplementedOpcodeException();
			default:
				throw new RuntimeException();
		}
	}

	protected void opcode4X(byte opcode) {
		byte index = (byte)(opcode & 0x07);
		switch (opcode) {
			case (byte)0x40: // INC reg16
			case (byte)0x41:
			case (byte)0x42:
			case (byte)0x43:
			case (byte)0x44:
			case (byte)0x45:
			case (byte)0x46:
			case (byte)0x47:
				m_regs.setReg16(index, inc16(m_regs.getReg16(index)));
				break;
			case (byte)0x48: // DEC reg16
			case (byte)0x49:
			case (byte)0x4A:
			case (byte)0x4B:
			case (byte)0x4C:
			case (byte)0x4D:
			case (byte)0x4E:
			case (byte)0x4F:
				m_regs.setReg16(index, dec16(m_regs.getReg16(index)));
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcode5X(byte opcode) throws MemoryException {
		byte index = (byte)(opcode & 0x07);
		switch (opcode) {
			case (byte)0x50: // PUSH reg16
			case (byte)0x51:
			case (byte)0x52:
			case (byte)0x53:
			case (byte)0x54:
			case (byte)0x55:
			case (byte)0x56:
			case (byte)0x57:
				push(m_regs.getReg16(index));
				break;
			case (byte)0x58: // POP reg16
			case (byte)0x59:
			case (byte)0x5A:
			case (byte)0x5B:
			case (byte)0x5C:
			case (byte)0x5D:
			case (byte)0x5E:
			case (byte)0x5F:
				m_regs.setReg16(index, pop());
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcode6X(byte opcode) throws CpuException {
		// 0x60.. 0x6F - invalid opcodes
		throw new InvalidOpcodeException();
	}

	protected void opcode7X(byte opcode) throws MemoryException {
		boolean branch = false;
		switch(opcode) {
			case (byte)0x70: // JO
				branch = m_state.getOverflowFlag();
				break;
			case (byte)0x71: // JNO
				branch = !m_state.getOverflowFlag();
				break;
			case (byte)0x72: // JC,JB,JNAE
				branch = m_state.getCarryFlag();
				break;
			case (byte)0x73: // JNC,JNC,JAE
				branch = !m_state.getCarryFlag();
				break;
			case (byte)0x74: // JE,JZ
				branch = m_state.getZeroFlag();
				break;
			case (byte)0x75: // JNE,JNZ
				branch = !m_state.getZeroFlag();
				break;
			case (byte)0x76: // JBE,JNA
				branch = (m_state.getCarryFlag() || m_state.getZeroFlag());
				break;
			case (byte)0x77: // JNBE,JA
				branch = (!m_state.getCarryFlag() && !m_state.getZeroFlag());
				break;
			case (byte)0x78: // JS
				branch = m_state.getSignFlag();
				break;
			case (byte)0x79: // JNS
				branch = !m_state.getSignFlag();
				break;
			case (byte)0x7A: // JP,JPE
				branch = m_state.getParityFlag();
				break;
			case (byte)0x7B: // JNP,JPO
				branch = !m_state.getParityFlag();
				break;
			case (byte)0x7C: // JL,JNGE
				branch = (m_state.getSignFlag() != m_state.getOverflowFlag());
				break;
			case (byte)0x7D: // JNL,JGE
				branch = (m_state.getSignFlag() == m_state.getOverflowFlag());
				break;
			case (byte)0x7E: // JLE,JNG
				branch = m_state.getZeroFlag() ||
					(m_state.getSignFlag() != m_state.getOverflowFlag());
				break;
			case (byte)0x7F: // JNLE,JG
				branch = !m_state.getZeroFlag() &&
					(m_state.getSignFlag() == m_state.getOverflowFlag());
				break;
		}
		byte offset = m_fetcher.nextByte();
		if (branch) {
			m_state.setIP((short)(m_state.getIP() + offset));
		}
	}

	protected void opcode8X(byte opcode) throws CpuException, MemoryException {
		switch (opcode) {
			case (byte)0x80: // <?> byte ptr [X], imm8
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: // ADD
						m_indirect.setMem8(
							add8(m_indirect.getMem8(), m_fetcher.nextByte()));
						break;
					case 1: // OR
						m_indirect.setMem8(
							or8(m_indirect.getMem8(), m_fetcher.nextByte()));
						break;
					case 2: // ADC
						m_indirect.setMem8(
							adc8(m_indirect.getMem8(), m_fetcher.nextByte()));
						break;
					case 3: // SBB
						m_indirect.setMem8(
							sbb8(m_indirect.getMem8(), m_fetcher.nextByte()));
						break;
					case 4: // AND
						m_indirect.setMem8(
							and8(m_indirect.getMem8(), m_fetcher.nextByte()));
						break;
					case 5: // SUB
						m_indirect.setMem8(
							sub8(m_indirect.getMem8(), m_fetcher.nextByte()));
						break;
					case 6: // XOR
						m_indirect.setMem8(
							xor8(m_indirect.getMem8(), m_fetcher.nextByte()));
						break;
					case 7: // CMP
						sub8(m_indirect.getMem8(), m_fetcher.nextByte());
						break;
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0x81: // <?> word ptr [X], imm16
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: // ADD
						m_indirect.setMem16(
							add16(m_indirect.getMem16(), m_fetcher.nextWord()));
						break;
					case 1: // OR
						m_indirect.setMem16(
							or16(m_indirect.getMem16(), m_fetcher.nextWord()));
						break;
					case 2: // ADC
						m_indirect.setMem16(
							adc16(m_indirect.getMem16(), m_fetcher.nextWord()));
						break;
					case 3: // SBB
						m_indirect.setMem16(
							sbb16(m_indirect.getMem16(), m_fetcher.nextWord()));
						break;
					case 4: // AND
						m_indirect.setMem16(
							and16(m_indirect.getMem16(), m_fetcher.nextWord()));
						break;
					case 5: // SUB
						m_indirect.setMem16(
							sub16(m_indirect.getMem16(), m_fetcher.nextWord()));
						break;
					case 6: // XOR
						m_indirect.setMem16(
							xor16(m_indirect.getMem16(), m_fetcher.nextWord()));
						break;
					case 7: // CMP
						sub16(m_indirect.getMem16(), m_fetcher.nextWord());
						break;
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0x82:
				throw new InvalidOpcodeException();
			case (byte)0x83: // <?> word ptr [X], sign-extended imm8
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: // ADD
						m_indirect.setMem16(
							add16(m_indirect.getMem16(), m_fetcher.nextByte()));
						break;
					case 1: // OR
						m_indirect.setMem16(
							or16(m_indirect.getMem16(), m_fetcher.nextByte()));
						break;
					case 2: // ADC
						m_indirect.setMem16(
							adc16(m_indirect.getMem16(), m_fetcher.nextByte()));
						break;
					case 3: // SBB
						m_indirect.setMem16(
							sbb16(m_indirect.getMem16(), m_fetcher.nextByte()));
						break;
					case 4: // AND
						m_indirect.setMem16(
							and16(m_indirect.getMem16(), m_fetcher.nextByte()));
						break;
					case 5: // SUB
						m_indirect.setMem16(
							sub16(m_indirect.getMem16(), m_fetcher.nextByte()));
						break;
					case 6: // XOR
						m_indirect.setMem16(
							xor16(m_indirect.getMem16(), m_fetcher.nextByte()));
						break;
					case 7: // CMP
						sub16(m_indirect.getMem16(), m_fetcher.nextByte());
						break;
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0x84: // TEST reg8, [X]
				m_indirect.reset();
				and8(m_indirect.getReg8(), m_indirect.getMem8());
				break;
			case (byte)0x85: // TEST reg16, [X]
				m_indirect.reset();
				and16(m_indirect.getReg16(), m_indirect.getMem16());
				break;
			case (byte)0x86: // XCHG reg8, [X]
				m_indirect.reset();
				byte tmpByte = m_indirect.getReg8();
				m_indirect.setReg8(m_indirect.getMem8());
				m_indirect.setMem8(tmpByte);
				break;
			case (byte)0x87: // XCHG reg16, [X]
				m_indirect.reset();
				short tmpWord = (short)m_indirect.getReg16();
				m_indirect.setReg16(m_indirect.getMem16());
				m_indirect.setMem16(tmpWord);
				break;
			case (byte)0x88: // MOV [X], reg8
				m_indirect.reset();
				m_indirect.setMem8(m_indirect.getReg8());
				break;
			case (byte)0x89: // MOV [X], reg16
				m_indirect.reset();
				m_indirect.setMem16((short)m_indirect.getReg16());
				break;
			case (byte)0x8A: // MOV reg8, [X]
				m_indirect.reset();
				m_indirect.setReg8(m_indirect.getMem8());
				break;
			case (byte)0x8B: // MOV reg16, [X]
				m_indirect.reset();
				m_indirect.setReg16(m_indirect.getMem16());
				break;
			case (byte)0x8C: // MOV [X], seg
				m_indirect.reset();
				m_indirect.setMem16((short)m_indirect.getSeg());
				break;
			case (byte)0x8D: // LEA reg16, [X]
				m_indirect.reset();
				Address address = m_indirect.getMemAddress();
				if (address == null) {
					// "LEA reg16, reg16" is an invalid opcode
					throw new InvalidOpcodeException();
				}
				m_indirect.setReg16((short)address.getOffset());
				break;
			case (byte)0x8E: // MOV seg, [X]
				m_indirect.reset();
				m_indirect.setSeg(m_indirect.getMem16());
				m_state.setUndefinedFlags(
						CpuState.FLAGS_MASK_OVERFLOW|
						CpuState.FLAGS_MASK_SIGN|
						CpuState.FLAGS_MASK_ZERO|
						CpuState.FLAGS_MASK_AUX|
						CpuState.FLAGS_MASK_PARITY|
						CpuState.FLAGS_MASK_CARRY);
				break;
			case (byte)0x8F: // POP [X]
				// Note: since Reg index bits are ignored, there are 8 different
				// machine-code representations for this opcode :-)
				m_indirect.reset();
				m_indirect.setMem16(pop());
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcode9X(byte opcode) throws CpuException, MemoryException {
		switch (opcode) {
			case (byte)0x90: // XCHG reg16, AX
			case (byte)0x91:
			case (byte)0x92:
			case (byte)0x93:
			case (byte)0x94:
			case (byte)0x95:
			case (byte)0x96:
			case (byte)0x97:
				byte index = (byte)(opcode & 0x07);
				int tmp = m_regs.getReg16(index);
				m_regs.setReg16(index, m_state.getAX());
				m_state.setAX(tmp);
				break;
			case (byte)0x98: // CBW
				if (m_state.getAL() < 0) {
					m_state.setAH((byte)0xFF);
				} else {
					m_state.setAH((byte)0);
				}
				break;
			case (byte)0x99: // CWD
				if ((m_state.getAX()&0x8000) == 0x8000) {
					m_state.setDX((short)0xFFFF);
				} else {
					m_state.setDX((short)0);
				}
				break;
			case (byte)0x9A: // CALL far imm16:imm16
				short newIP = m_fetcher.nextWord();
				short newCS = m_fetcher.nextWord();
				callFar(newCS, newIP);
				break;
			case (byte)0x9B: // WAIT
				//Nothing to do
				break;
			case (byte)0x9C: // PUSHF
				push(m_state.getFlags());
				break;
			case (byte)0x9D: // POPF
				m_state.setFlags(pop());
				break;
			case (byte)0x9E:
				// TODO: handle reserved bits (1,3,5)
				int flags = m_state.getFlags();
				flags &= 0xFF00;
				flags |= m_state.getAH();
				m_state.setFlags(flags);
				break;
			case (byte)0x9F: // LAHF
				m_state.setAH((byte)m_state.getFlags());
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcodeAX(byte opcode) throws MemoryException {
		Address address = null;
		switch (opcode) {
			case (byte)0xA0: // MOV AL, [imm16]
				address = m_indirect.newAddress(RegisterIndexingDecoder.DS_INDEX, m_fetcher.nextWord());
				m_state.setAL(m_memory.readByte(address));
				break;
			case (byte)0xA1: // MOV AX, [imm16]
				address = m_indirect.newAddress(RegisterIndexingDecoder.DS_INDEX, m_fetcher.nextWord());
				m_state.setAX(m_memory.readWord(address));
				break;
			case (byte)0xA2: // MOV [imm16], AL
				address = m_indirect.newAddress(RegisterIndexingDecoder.DS_INDEX, m_fetcher.nextWord());
				m_memory.writeByte(address, m_state.getAL());
				break;
			case (byte)0xA3:{ // MOV [imm16], AX
				address = m_indirect.newAddress(RegisterIndexingDecoder.DS_INDEX, m_fetcher.nextWord());
				m_memory.writeWord(address, (short)m_state.getAX());
			}break;
			case (byte)0xA4: // MOVSB
				movsb();
				break;
			case (byte)0xA5: // MOVSW
				movsw();
				break;
			case (byte)0xA6: // CMPSB
				cmpsb();
				break;
			case (byte)0xA7: // CMPSW
				cmpsw();
				break;
			case (byte)0xA8: // TEST AL, imm8
				and8(m_state.getAL(), m_fetcher.nextByte());
				break;
			case (byte)0xA9: // TEST AX, imm16
				and16(m_state.getAX(), m_fetcher.nextWord());
				break;
			case (byte)0xAA: // STOSB
				stosb();
				break;
			case (byte)0xAB: // STOSW
				stosw();
				break;
			case (byte)0xAC: // LODSB
				lodsb();
				break;
			case (byte)0xAD: // LODSW
				lodsw();
				break;
			case (byte)0xAE: // SCASB
				scasb();
				break;
			case (byte)0xAF: // SCASW
				scasw();
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcodeBX(byte opcode) throws MemoryException {
		byte index = (byte)(opcode & 0x07);
		switch (opcode) {
			case (byte)0xB0: // MOV reg8, imm8
			case (byte)0xB1:
			case (byte)0xB2:
			case (byte)0xB3:
			case (byte)0xB4:
			case (byte)0xB5:
			case (byte)0xB6:
			case (byte)0xB7:
				m_regs.setReg8(index, m_fetcher.nextByte());
				break;
			case (byte)0xB8: // MOV reg16, imm16
			case (byte)0xB9:
			case (byte)0xBA:
			case (byte)0xBB:
			case (byte)0xBC:
			case (byte)0xBD:
			case (byte)0xBE:
			case (byte)0xBF:
				m_regs.setReg16(index, m_fetcher.nextWord());
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcodeCX(byte opcode) throws CpuException, MemoryException {
		short sizeToPop;
		Address address1 = null;
		Address address2 = null;
		switch (opcode) {
			case (byte)0xC0:
			case (byte)0xC1:
				// 0xC0.. 0xC1 - invalid opcodes
				throw new InvalidOpcodeException();
			case (byte)0xC2: // RETN [imm16]
				sizeToPop = m_fetcher.nextWord();
				m_state.setIP(pop());
				m_state.setSP((short)(m_state.getSP() + sizeToPop));
				break;
			case (byte)0xC3: // RETN
				m_state.setIP(pop());
				break;
			case (byte)0xC4: // LES reg16, [X]
				m_indirect.reset();
				address1 = m_indirect.getMemAddress();
				if (address1 == null) {
					// "LES reg16, reg16" is an invalid opcode
					throw new InvalidOpcodeException();
				}
				address2 = m_machine.newAddress(
					address1.getSegment(), (short)(address1.getOffset() + 2));

				m_indirect.setReg16(m_memory.readWord(address1));
				m_state.setES(m_memory.readWord(address2));
				break;
			case (byte)0xC5: // LDS reg16, [X]
				m_indirect.reset();
				address1 = m_indirect.getMemAddress();
				if (address1 == null) {
					// "LDS reg16, reg16" is an invalid opcode
					throw new InvalidOpcodeException();
				}
				address2 = m_machine.newAddress(
					address1.getSegment(), (short)(address1.getOffset() + 2));

				m_indirect.setReg16(m_memory.readWord(address1));
				m_state.setDS(m_memory.readWord(address2));
				break;
			case (byte)0xC6: // MOV [X], imm8
				// Note: since Reg index bits are ignored, there are 8 different
				// machine-code representations for this opcode :-)
				m_indirect.reset();
				m_indirect.setMem8(m_fetcher.nextByte());
				break;
			case (byte)0xC7: // MOV [X], imm16
				// Note: since Reg index bits are ignored, there are 8 different
				// machine-code representations for this opcode :-)
				m_indirect.reset();
				m_indirect.setMem16(m_fetcher.nextWord());
				break;
			case (byte)0xC8:
			case (byte)0xC9:
				// 0xC8.. 0xC9 - invalid opcodes
				throw new InvalidOpcodeException();
			case (byte)0xCA: // RETF [imm16]
				sizeToPop = m_fetcher.nextWord();
				m_state.setIP(pop());
				m_state.setCS(pop());
				m_state.setSP((short)(m_state.getSP() + sizeToPop));
				break;
			case (byte)0xCB: // RETF
				m_state.setIP(pop());
				m_state.setCS(pop());
				break;
			case (byte)0xCC: // INT3
				interrupt( 3 );
				break;
			case (byte)0xCD: // INT [imm8]
				interrupt( m_fetcher.nextByte() & 0xff );
				break;
			case (byte)0xCE: // INTO
				interrupt( 0 );
				break;
			case (byte)0xCF: // IRET
				m_state.setIP(pop());
				m_state.setCS(pop());
				m_state.setFlags(pop());
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcodeDX(byte opcode) throws CpuException, MemoryException {
		switch (opcode) {
			case (byte)0xD0: // <?> byte ptr [X], 1
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case (byte)0x00: // ROL
						rol8(1);
						break;
					case (byte)0x01: // ROR
						ror8(1);
						break;
					case (byte)0x02: // RCL
						rcl8(1);
						break;
					case (byte)0x03: // RCR
						rcr8(1);
						break;
					case (byte)0x04: // SHL
						shl8(1);
						break;
					case (byte)0x05: // SHR
						shr8(1);
						break;
					case (byte)0x06: // invalid opcode
						throw new InvalidOpcodeException();
					case (byte)0x07: // SAR
						sar8(1);
						break;
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0xD1: // <?> word ptr [X], 1
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case (byte)0x00: // ROL
						rol16(1);
						break;
					case (byte)0x01: // ROR
						ror16(1);
						break;
					case (byte)0x02: // RCL
						rcl16(1);
						break;
					case (byte)0x03: // RCR
						rcr16(1);
						break;
					case (byte)0x04: // SHL
						shl16(1);
						break;
					case (byte)0x05: // SHR
						shr16(1);
						break;
					case (byte)0x06: // invalid opcode
						throw new InvalidOpcodeException();
					case (byte)0x07: // SAR
						sar16(1);
						break;
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0xD2: // <?> byte ptr [X], CL
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case (byte)0x00: // ROL
						rol8(m_state.getCL());
						break;
					case (byte)0x01: // ROR
						ror8(m_state.getCL());
						break;
					case (byte)0x02: // RCL
						rcl8(m_state.getCL());
						break;
					case (byte)0x03: // RCR
						rcr8(m_state.getCL());
						break;
					case (byte)0x04: // SHL
						shl8(m_state.getCL());
						break;
					case (byte)0x05: // SHR
						shr8(m_state.getCL());
						break;
					case (byte)0x06: // invalid opcode
						throw new InvalidOpcodeException();
					case (byte)0x07: // SAR
						sar8(m_state.getCL());
						break;
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0xD3: // <?> word ptr [x], CL
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case (byte)0x00: // ROL
						rol16(m_state.getCL());
						break;
					case (byte)0x01: // ROR
						ror16(m_state.getCL());
						break;
					case (byte)0x02: // RCL
						rcl16(m_state.getCL());
						break;
					case (byte)0x03: // RCR
						rcr16(m_state.getCL());
						break;
					case (byte)0x04: // SHL
						shl16(m_state.getCL());
						break;
					case (byte)0x05: // SHR
						shr16(m_state.getCL());
						break;
					case (byte)0x06: // invalid opcode
						throw new InvalidOpcodeException();
					case (byte)0x07: // SAR
						sar16(m_state.getCL());
						break;
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0xD4: // TODO: AAM
			case (byte)0xD5: // TODO: AAD
				throw new UnimplementedOpcodeException();
			case (byte)0xD6:
				// 0xD6 - invalid opcode
				throw new InvalidOpcodeException();
			case (byte)0xD7: // XLAT, XLATB
				Address address = m_machine.newAddress(m_state.getDS(),
					(short)(m_state.getBX() + Unsigned.unsignedByte(m_state.getAL())));
				m_state.setAL(m_memory.readByte(address));
				break;
			case (byte)0xD8: // FADD dword
			case (byte)0xD9: // FLD dword
			case (byte)0xDA: // FIADD dword
				throw new UnsupportedOpcodeException();
			case (byte)0xDB: // FILD dword
				if(m_fetcher.nextByte() == (byte)0xE2){
					break;
				}
			case (byte)0xDC: // FADD qword
			case (byte)0xDD: // FLD qword
			case (byte)0xDE: // FIADD word
			case (byte)0xDF: // FILD word
				throw new UnsupportedOpcodeException();
			default:
				throw new RuntimeException();
		}
	}

	protected void opcodeEX(byte opcode) throws CpuException, MemoryException {
		byte offset8;
		short offset16;
		short newCX;
		short newCS;
		short newIP;
		switch (opcode) {
			case (byte)0xE0: // LOOPNZ, LOOPNE
				offset8 = m_fetcher.nextByte();
				newCX = (short)(m_state.getCX() - 1);
				m_state.setCX(newCX);
				if ((newCX != 0) && (!m_state.getZeroFlag())) {
					m_state.setIP((short)(m_state.getIP() + offset8));
				}
				break;
			case (byte)0xE1: // LOOPZ, LOOPE
				offset8 = m_fetcher.nextByte();
				newCX = (short)(m_state.getCX() - 1);
				m_state.setCX(newCX);
				if ((newCX != 0) && (m_state.getZeroFlag())) {
					m_state.setIP((short)(m_state.getIP() + offset8));
				}
				break;
			case (byte)0xE2: // LOOP
				offset8 = m_fetcher.nextByte();
				newCX = (short)(m_state.getCX() - 1);
				m_state.setCX(newCX);
				if (newCX != 0) {
					m_state.setIP((short)(m_state.getIP() + offset8));
				}
				break;
			case (byte)0xE3: // JCXZ
				offset8 = m_fetcher.nextByte();
				if (m_state.getCX() == 0) {
					m_state.setIP((short)(m_state.getIP() + offset8));
				}
				break;
			case (byte)0xE4: // IN AL, imm8
				m_state.setAL((byte)in(m_fetcher.nextByte()));
				break;
			case (byte)0xE5: // IN AX, imm8
				m_state.setAX(in(m_fetcher.nextByte()));
				break;
			case (byte)0xE6: // OUT imm8, AL
				out(m_fetcher.nextByte(),m_state.getAL());
				break;
			case (byte)0xE7: // OUT imm8, AX
				out(m_fetcher.nextByte(),(short) m_state.getAX());
				break;
			case (byte)0xE8: // CALL near imm16
				offset16 = m_fetcher.nextWord();
				callNear((short)(m_state.getIP() + offset16));
				break;
			case (byte)0xE9: // JMP near imm16
				offset16 = m_fetcher.nextWord();
				m_state.setIP((short)(m_state.getIP() + offset16));
				break;
			case (byte)0xEA: // JMP far imm16:imm16
				newIP = m_fetcher.nextWord();
				newCS = m_fetcher.nextWord();
				m_state.setIP(newIP);
				m_state.setCS(newCS);
				break;
			case (byte)0xEB: // JMP short imm8
				offset8 = m_fetcher.nextByte();
				m_state.setIP((short)(m_state.getIP() + offset8));
				break;
			case (byte)0xEC: // IN AL, DX
				m_state.setAL((byte)in(m_state.getDX()));
				break;
			case (byte)0xED: // IN AX, DX
				m_state.setAX(in(m_state.getDX()));
				break;
			case (byte)0xEE: // OUT DX, AL
				out(m_state.getDX(),m_state.getAL());
				break;
			case (byte)0xEF: // OUT DX, AX
				out(m_state.getDX(),(short) m_state.getAX());
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void opcodeFX(byte opcode) throws CpuException, MemoryException {
		switch (opcode) {
			case (byte)0xF0: // LOCK
				throw new UnsupportedOpcodeException();
			case (byte)0xF1:
				// 0xF1 - invalid opcode
				throw new InvalidOpcodeException();
			case (byte)0xF2:{ // REPNZ
				byte nextOpcode = m_fetcher.nextByte();

				if(m_state.getCX()>0){
					switch (nextOpcode) {
					case (byte)0xA6: // REPNZ CMPSB
						cmpsb();
					break;
					case (byte)0xA7: // REPNZ CMPSW
						cmpsw();
					break;
					case (byte)0xAE: // REPNZ SCASB
						scasb();
					break;
					case (byte)0xAF: // REPNZ SCASW
						scasw();
					break;
					default:
						throw new InvalidOpcodeException();
					}
					if( !m_state.getZeroFlag() ){
						int cx = m_state.getCX()-1;
						m_state.setCX(cx);
						if(cx>0){
							m_state.setIP(m_state.getIP()-2);
						}
					}
				}
			}break;
			case (byte)0xF3:{ // REP, REPZ
				byte nextOpcode = m_fetcher.nextByte();
				boolean loopdone = false;

				if(m_state.getCX()>0){
					switch (nextOpcode) {
					case (byte)0xA4: // REP MOVSB
						movsb();
					break;
					case (byte)0xA5: // REP MOVSW
						movsw();
					break;
					case (byte)0xA6: // REPZ CMPSB
						cmpsb();
					if( !m_state.getZeroFlag() ){
						loopdone = true;
					}
					break;
					case (byte)0xA7: // REPZ CMPSW
						cmpsw();
					if( !m_state.getZeroFlag() ){
						loopdone = true;
					}
					break;
					case (byte)0xAA: // REP STOSB
						stosb();
					break;
					case (byte)0xAB: // REP STOSW
						stosw();
					break;
					case (byte)0xAC: // REP LODSB
						lodsb();
					break;
					case (byte)0xAD: // REP LODSW
						lodsw();
					break;
					case (byte)0xAE: // REPZ SCASB
						scasb();
					if( !m_state.getZeroFlag() ){
						loopdone = true;
					}
					break;
					case (byte)0xAF: // REPZ SCASW
						scasw();
					if( !m_state.getZeroFlag() ){
						loopdone = true;
					}
					break;
					default:
						throw new InvalidOpcodeException();
					}

					if(!loopdone){
						int cx = m_state.getCX()-1;
						m_state.setCX(cx);
						if(cx>0){
							m_state.setIP(m_state.getIP()-2);
						}
					}
				}
			}break;
			case (byte)0xF4: // HLT
				throw new UnsupportedOpcodeException();
			case (byte)0xF5: // CMC
				m_state.setCarryFlag(!m_state.getCarryFlag());
				break;
			case (byte)0xF6: // <?> byte ptr [X]
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: // TEST imm8
						and8(m_indirect.getMem8(), m_fetcher.nextByte());
						break;
					case 1:
						throw new InvalidOpcodeException();
					case 2: // NOT
						m_indirect.setMem8((byte)(m_indirect.getMem8() ^ 0xFF));
						break;
					case 3: // NEG
						m_indirect.setMem8(sub8((byte)0, m_indirect.getMem8()));
						break;
					case 4: // MUL
						// multiply
						short result = (short)(
							Unsigned.unsignedByte(m_state.getAL()) *
							Unsigned.unsignedByte(m_indirect.getMem8()));
						m_state.setAH((byte)(result >> 8));
						m_state.setAL((byte)result);

						// update flags
						if (m_state.getAH() == 0) {
							m_state.setOverflowFlag(false);
							m_state.setCarryFlag(false);
						} else {
							m_state.setOverflowFlag(true);
							m_state.setCarryFlag(true);
						}
						break;
					case 5: // TODO: IMUL
						throw new UnimplementedOpcodeException();
					case 6: // DIV
						int tmp = Unsigned.unsignedShort(m_state.getAX());
						short divisor = Unsigned.unsignedByte(m_indirect.getMem8());
						if (divisor == 0) { // divide by zero ?
							throw new DivisionException();
						}
						short quotient = (short)(tmp / divisor);
						if (quotient > 0xFF) { // divide overflow ?
							throw new DivisionException();
						}
						m_state.setAL((byte)quotient);
						m_state.setAH((byte)(tmp % divisor));
						break;
					case 7: // TODO: IDIV
						throw new UnimplementedOpcodeException();
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0xF7: // <?> word ptr [X]
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: // TEST imm16
						and16(m_indirect.getMem16(), m_fetcher.nextWord());
						break;
					case 1:
						throw new InvalidOpcodeException();
					case 2: // NOT
						m_indirect.setMem16((short)(m_indirect.getMem16() ^ 0xFFFF));
						break;
					case 3: // NEG
						m_indirect.setMem16(sub16((short)0, m_indirect.getMem16()));
						break;
					case 4: {// MUL
						// multiply
						int result =
							Unsigned.unsignedShort(m_state.getAX()) *
							Unsigned.unsignedShort(m_indirect.getMem16());
						m_state.setDX((short)(result >> 16));
						m_state.setAX((short)result);

						// update flags
						if (m_state.getDX() == 0) {
							m_state.setOverflowFlag(false);
							m_state.setCarryFlag(false);
						} else {
							m_state.setOverflowFlag(true);
							m_state.setCarryFlag(true);
						}
					}break;
					case 5: {// TODO: check IMUL
						int result = m_state.getAX() * m_indirect.getMem16();
						m_state.setDX((short)(result >> 16));
						m_state.setAX((short)result);

						// update flags
						if (m_state.getDX() == 0) {
							m_state.setOverflowFlag(false);
							m_state.setCarryFlag(false);
						} else {
							m_state.setOverflowFlag(true);
							m_state.setCarryFlag(true);
						}
					}break;
					case 6: {// DIV
						long tmp = Unsigned.unsignedInt(
								(Unsigned.unsignedShort(m_state.getDX()) << 16) +
								Unsigned.unsignedShort(m_state.getAX())
								);
						int divisor = Unsigned.unsignedShort(m_indirect.getMem16());
						if (divisor == 0) { // divide by zero ?
							throw new DivisionException();
						}
						int quotient = (int)(tmp / divisor);
						if (quotient > 0xFFFF) { // divide overflow ?
							throw new DivisionException();
						}
						m_state.setAX((short)quotient);
						m_state.setDX((short)(tmp % divisor));
						m_state.setUndefinedFlags(
								CpuState.FLAGS_MASK_OVERFLOW|
								CpuState.FLAGS_MASK_SIGN|
								CpuState.FLAGS_MASK_ZERO|
								CpuState.FLAGS_MASK_AUX|
								CpuState.FLAGS_MASK_PARITY|
								CpuState.FLAGS_MASK_CARRY);
					}break;
					case 7: {// TODO: check IDIV
						int tmp = (Unsigned.unsignedShort(m_state.getDX()) << 16) +
								Unsigned.unsignedShort(m_state.getAX());
						int divisor = Unsigned.unsignedShort(m_indirect.getMem16());
						if (divisor == 0) { // divide by zero ?
							throw new DivisionException();
						}
						int quotient = (int)(tmp / divisor);
						if (quotient > 0xFFFF) { // divide overflow ?
							throw new DivisionException();
						}
						m_state.setAX((short)quotient);
						m_state.setDX((short)(tmp % divisor));
						m_state.setUndefinedFlags(
								CpuState.FLAGS_MASK_OVERFLOW|
								CpuState.FLAGS_MASK_SIGN|
								CpuState.FLAGS_MASK_ZERO|
								CpuState.FLAGS_MASK_AUX|
								CpuState.FLAGS_MASK_PARITY|
								CpuState.FLAGS_MASK_CARRY);
					}break;
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0xF8: // CLC
				m_state.setCarryFlag(false);
				break;
			case (byte)0xF9: // STC
				m_state.setCarryFlag(true);
				break;
			case (byte)0xFA: // CLI
				m_state.setInterruptFlag(false);
				break;
			case (byte)0xFB: // STI
				m_state.setInterruptFlag(true);
				break;
			case (byte)0xFC: // CLD
				m_state.setDirectionFlag(false);
				break;
			case (byte)0xFD: // STD
				m_state.setDirectionFlag(true);
				break;
			case (byte)0xFE: // <?> byte ptr [X]
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: // INC
						m_indirect.setMem8(inc8(m_indirect.getMem8()));
						break;
					case 1: // DEC
						m_indirect.setMem8(dec8(m_indirect.getMem8()));
						break;
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						// invalid opcodes
						throw new InvalidOpcodeException();
					default:
						throw new RuntimeException();
				}
				break;
			case (byte)0xFF: // <?> word ptr [X]
				m_indirect.reset();
				switch (m_indirect.getRegIndex()) {
					case 0: // INC
						m_indirect.setMem16(inc16(m_indirect.getMem16()));
						break;
					case 1: // DEC
						m_indirect.setMem16(dec16(m_indirect.getMem16()));
						break;
					case 2: // CALL near
						callNear(m_indirect.getMem16());
						break;
					case 3: // CALL far
					{
						Address address = m_indirect.getMemAddress();
						if (address == null) {
							throw new InvalidOpcodeException();
						}

						short newIP = m_memory.readWord(address);
						address = m_machine.newAddress(address.getSegment(),
								(short)(address.getOffset() + 2));
						short newCS = m_memory.readWord(address);
						callFar(newCS, newIP);
					}
						break;
					case 4: // JMP near
						// FIXME: JMP SP bug ?
						m_state.setIP(m_indirect.getMem16());
						break;
					case 5: // JMP far
					{
						Address address = m_indirect.getMemAddress();
						if (address == null) {
							throw new InvalidOpcodeException();
						}

						short newIP = m_memory.readWord(address);
						address = m_machine.newAddress(address.getSegment(),
								(short)(address.getOffset() + 2));
						short newCS = m_memory.readWord(address);
						m_state.setCS(newCS);
						m_state.setIP(newIP);
					}
						break;
					case 6: // PUSH
						push(m_indirect.getMem16());
						break;
					case 7: // invalid opcode
						throw new InvalidOpcodeException();
					default:
						throw new RuntimeException();
				}
				break;
			default:
				throw new RuntimeException();
		}
	}

	protected void push(int value) throws MemoryException {
		m_state.setSP((short)(m_state.getSP() - 2));
		Address stackPtr = m_machine.newAddress(
				m_state.getSS(), m_state.getSP());
		m_memory.writeWord(stackPtr, (short)value);
	}

	protected short pop() throws MemoryException {
		Address stackPtr = m_machine.newAddress(
				m_state.getSS(), m_state.getSP());
		short value = m_memory.readWord(stackPtr);
		m_state.setSP((short)(m_state.getSP() + 2));
		return value;
	}

	/**
	 * Updates the CPU flags register after an 8bit operation.
	 * @param val  Result of an 8bit operation.
	 */
	protected void updateFlags8(int val) {
		m_state.setCarryFlag((val & 0xFF00) != 0);
		// TODO: update overflow flag
		updateFlagsNoCarryOverflow8((byte)val);
	}

	/**
	 * Updates the CPU flags register after a 16bit operation.
	 * @param value  Result of a 16bit operation.
	 */
	protected void updateFlags16(int value) {
		m_state.setCarryFlag((value & 0xFFFF0000) != 0);
		// TODO: update overflow flag
		updateFlagsNoCarryOverflow16((short)value);
	}

	/**
	 * Updates the CPU flags register after an 8bit operation, except for the
	 * Carry and Overflow flags.
	 * @param value  Result of an 8bit operation.
	 */
	protected void updateFlagsNoCarryOverflow8(byte value) {
		// TODO: update aux flag
		m_state.setParityFlag(getParity(value));
		m_state.setSignFlag((value & 0x80) != 0);
		m_state.setZeroFlag(value == 0);
	}

	/**
	 * Updates the CPU flags register after a 16bit operation, except for the
	 * Carry and Overflow flags.
	 * @param val  Result of a 16bit operation.
	 */
	protected void updateFlagsNoCarryOverflow16(short val) {
		byte byteValue = (byte)val;
		// TODO: update aux flag
		m_state.setParityFlag(getParity(byteValue));
		m_state.setSignFlag((val & 0x8000) != 0);
		m_state.setZeroFlag(val == 0);
	}

	/**
	 * Adds two 8bit values, updates the flags and returns the result.
	 */
	protected byte add8(byte value1, byte value2) {
		short result16 =
			(short)(Unsigned.unsignedByte(value1) + Unsigned.unsignedByte(value2));
		byte result8 = (byte)result16;
		updateFlags8(result16);
		m_state.setAuxFlag((value1&0xf)+(value2&0xf)>0xf);
		return result8;
	}

	/**
	 * Adds two 16bit values, updates the flags and returns the result.
	 */
	protected short add16(int value1, int value2) {
		updateFlags16(Unsigned.unsignedShort(value1) + Unsigned.unsignedShort(value2));
		int result32=(short)value1+(short)value2;
		m_state.setOverflowFlag(result32>0x7fff||result32<-0x8000);
		m_state.setAuxFlag((value1&0xf)+(value2&0xf)>0xf);
		return (short)result32;
	}

	/**
	 * Increments an 8bit value, updates the flags and returns the result.
	 * Note: does not modify the carry flag.
	 */
	protected byte inc8(byte value) {
		boolean oldCarry = m_state.getCarryFlag();
		byte result = add8(value, (byte)1);
		m_state.setCarryFlag(oldCarry);
		return result;
	}

	/**
	 * Increments a 16bit value, updates the flags and returns the result.
	 * Note: does not modify the carry flag.
	 */
	protected short inc16(int value) {
		boolean oldCarry = m_state.getCarryFlag();
		short result = add16(value, (short)1);
		m_state.setCarryFlag(oldCarry);
		return result;
	}

	/**
	 * Decrements an 8bit value, updates the flags and returns the result.
	 * Note: does not modify the carry flag.
	 */
	protected byte dec8(byte value) {
		boolean oldCarry = m_state.getCarryFlag();
		byte result = sub8(value, (byte)1);
		m_state.setCarryFlag(oldCarry);
		return result;
	}

	/**
	 * Decrements a 16bit value, updates the flags and returns the result.
	 * Note: does not modify the carry flag.
	 */
	protected short dec16(int value) {
		boolean oldCarry = m_state.getCarryFlag();
		short result = sub16(value, (short)1);
		m_state.setCarryFlag(oldCarry);
		return result;
	}

	/**
	 * ORs two 8bit values, updates the flags and returns the result.
	 */
	protected byte or8(byte value1, byte value2) {
		short result16 =
			(short)(Unsigned.unsignedByte(value1) | Unsigned.unsignedByte(value2));
		byte result8 = (byte)result16;
		updateFlags8(result16);
		m_state.setOverflowFlag(false);
		m_state.setCarryFlag(false);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
		return result8;
	}

	/**
	 * ORs two 16bit values, updates the flags and returns the result.
	 */
	protected short or16(int value1, int value2) {
		int result32 =
			Unsigned.unsignedShort(value1) | Unsigned.unsignedShort(value2);
		short result16 = (short)result32;
		updateFlags16(result32);
		m_state.setOverflowFlag(false);
		m_state.setCarryFlag(false);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
		return result16;
	}

	/**
	 * Adds two 8bit values with the carry flag, updates the flags and returns
	 * the result.
	 */
	protected byte adc8(byte value1, byte value2) {
		int carry = m_state.getCarryFlag()?1:0;
		short result16 =
			(short)(Unsigned.unsignedByte(value1) + Unsigned.unsignedByte(value2)+carry);
		updateFlags8(result16);
		result16 = (short)(value1+value2+carry);
		m_state.setOverflowFlag(result16>0x7f||result16<-0x80);
		m_state.setAuxFlag((value1&0xf)+(value2&0xf)+carry>0xf);
		return (byte)result16;
	}

	/**
	 * Adds two 16bit values with the carry flag, updates the flags and returns
	 * the result.
	 */
	protected short adc16(int value1, int value2) {
		int carry = m_state.getCarryFlag()?1:0;
		int result32 = carry +
			Unsigned.unsignedShort(value1) + Unsigned.unsignedShort(value2);
		updateFlags16(result32);
		result32=(short)value1+(short)value2+carry;
		m_state.setOverflowFlag(result32>0x7fff||result32<-0x8000);
		m_state.setAuxFlag((value1&0xf)+(value2&0xf)+carry>0xf);
		return (short)result32;
	}

	/**
	 * Subtracts a given 8bit value with the sum of a second 8bit value and the
	 * carry flag, updates the flags and returns the result.
	 */
	protected byte sbb8(byte value1, byte value2) {
		int borrow = m_state.getCarryFlag()?1:0;
		updateFlags8((short)(Unsigned.unsignedByte(value1)-Unsigned.unsignedByte(value2))-borrow);
		short result16=(short)((byte)value1-(byte)value2-borrow);
		m_state.setOverflowFlag(result16>0x7f||result16<-0x80);
		m_state.setAuxFlag((value1&0xf)<(value2&0xf)+borrow);
		return (byte)result16;
	}

	/**
	 * Subtracts a given 16bit value with the sum of a second 16bit value and the
	 * carry flag, updates the flags and returns the result.
	 */
	protected short sbb16(int value1, int value2) {
		int borrow = m_state.getCarryFlag()?1:0;
		updateFlags16(Unsigned.unsignedShort(value1)-Unsigned.unsignedShort(value2)-borrow);
		int result32=(short)value1-(short)value2-borrow;
		m_state.setOverflowFlag(result32>0x7fff||result32<-0x8000);
		m_state.setAuxFlag((value1&0xf)<(value2&0xf)+borrow);
		return (short)result32;
	}

	/**
	 * ANDs two 8bit values, updates the flags and returns the result.
	 */
	protected byte and8(byte value1, byte value2) {
		short result16 =
			(short)(Unsigned.unsignedByte(value1) & Unsigned.unsignedByte(value2));
		byte result8 = (byte)result16;
		updateFlags8(result16);
		m_state.setOverflowFlag(false);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
		return result8;
	}

	/**
	 * ANDs two 16bit values, updates the flags and returns the result.
	 */
	protected short and16(int value1, int value2) {
		int result32 =
			Unsigned.unsignedShort(value1) & Unsigned.unsignedShort(value2);
		short result16 = (short)result32;
		updateFlags16(result32);
		m_state.setOverflowFlag(false);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
		return result16;
	}

	/**
	 * Subtracts two 8bit values, updates the flags and returns the result.
	 */
	protected byte sub8(byte value1, byte value2) {
		updateFlags8((short)(Unsigned.unsignedByte(value1)-Unsigned.unsignedByte(value2)));
		short result16=(short)((byte)value1-(byte)value2);
		m_state.setOverflowFlag(result16>0x7f||result16<-0x80);
		m_state.setAuxFlag((value1&0xf)<(value2&0xf));
		return (byte)result16;
	}

	/**
	 * Subtracts two 16bit values, updates the flags and returns the result.
	 */
	protected short sub16(int value1, int value2) {
		updateFlags16(Unsigned.unsignedShort(value1) - Unsigned.unsignedShort(value2));
		int result32=(short)value1-(short)value2;
		m_state.setOverflowFlag(result32>0x7fff||result32<-0x8000);
		m_state.setAuxFlag((value1&0xf)<(value2&0xf));
		return (short)result32;
	}

	/**
	 * XORs two 8bit values, updates the flags and returns the result.
	 */
	protected byte xor8(byte value1, byte value2) {
		short result16 =
			(short)(Unsigned.unsignedByte(value1) ^ Unsigned.unsignedByte(value2));
		byte result8 = (byte)result16;
		updateFlags8(result16);
		m_state.setOverflowFlag(false);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
		return result8;
	}

	/**
	 * XORs two 16bit values, updates the flags and returns the result.
	 */
	protected short xor16(int value1, int value2) {
		int result32 =
			Unsigned.unsignedShort(value1) ^ Unsigned.unsignedShort(value2);
		short result16 = (short)result32;
		updateFlags16(result32);
		m_state.setOverflowFlag(false);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
		return result16;
	}

	/**
	 * Implements a near call opcode.
	 * @param offset    New value for IP (CS stays the same).
	 * @throws MemoryException
	 */
	protected void callNear(short offset) throws MemoryException {
		Address from = m_machine.newAddress(prevState.getCS(), prevState.getIP());
		Address to = m_machine.newAddress(m_state.getCS(), offset);
		notifyCallListeners(from,to);
		push(m_state.getIP());
		m_state.setIP(offset);
	}

	/**
	 * Implements a far call opcode.
	 * @param segment   New value for CS.
	 * @param offset    New value for IP.
	 * @throws MemoryException
	 */
	protected void callFar(short segment, short offset) throws MemoryException {
		push(m_state.getCS());
		m_state.setCS(segment);
		callNear(offset);
	}

	/**
	 * Implements the 'movsb' opcode.
	 * @throws MemoryException
	 */
	protected void movsb() throws MemoryException {
		Address src =
			m_machine.newAddress(m_state.getDS(), m_state.getSI());
		Address dst =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		m_memory.writeByte(dst, m_memory.readByte(src));

		byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1);
		m_state.setSI((short)(m_state.getSI() + diff));
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	/**
	 * Implements the 'movsw' opcode.
	 * @throws MemoryException
	 */
	protected void movsw() throws MemoryException {
		Address src =
			m_machine.newAddress(m_state.getDS(), m_state.getSI());
		Address dst =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		m_memory.writeWord(dst, m_memory.readWord(src));

		int diff = (m_state.getDirectionFlag() ? -2 : 2);
		m_state.setSI((short)(m_state.getSI() + diff));
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	/**
	 * Implements the 'cmpsb' opcode.
	 * @throws MemoryException
	 */
	protected void cmpsb() throws MemoryException {
		Address address1 =
			m_machine.newAddress(m_state.getDS(), m_state.getSI());
		Address address2 =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		sub8(m_memory.readByte(address1), m_memory.readByte(address2));

		byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1);
		m_state.setSI((short)(m_state.getSI() + diff));
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	/**
	 * Implements the 'cmpsw' opcode.
	 * @throws MemoryException
	 */
	protected void cmpsw() throws MemoryException {
		Address address1 =
			m_machine.newAddress(m_state.getDS(), m_state.getSI());
		Address address2 =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		sub16(m_memory.readWord(address1), m_memory.readWord(address2));

		byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2);
		m_state.setSI((short)(m_state.getSI() + diff));
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	/**
	 * Implements the 'stosb' opcode.
	 * @throws MemoryException
	 */
	protected void stosb() throws MemoryException {
		Address address =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		m_memory.writeByte(address, m_state.getAL());
		byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1);
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	/**
	 * Implements the 'stosw' opcode.
	 * @throws MemoryException
	 */
	protected void stosw() throws MemoryException {
		Address address =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		m_memory.writeWord(address, (short) m_state.getAX());
		byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2);
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	/**
	 * Implements the virtual 'stosdw' opcode.
	 * @throws MemoryException
	 */
	protected void stosdw() throws MemoryException {
		Address address1 =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		m_memory.writeWord(address1, (short) m_state.getAX());

		Address address2 =
			m_machine.newAddress(m_state.getES(), (short)(m_state.getDI() + 2));
		m_memory.writeWord(address2, (short) m_state.getDX());

		byte diff = (m_state.getDirectionFlag() ? (byte)-4 : (byte)4);
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	/**
	 * Implements the 'lodsb' opcode.
	 * @throws MemoryException
	 */
	protected void lodsb() throws MemoryException {
		Address address =
			m_machine.newAddress(m_state.getDS(), m_state.getSI());
		m_state.setAL(m_memory.readByte(address));
		byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1);
		m_state.setSI((short)(m_state.getSI() + diff));
	}

	/**
	 * Implements the 'lodsw' opcode.
	 * @throws MemoryException
	 */
	protected void lodsw() throws MemoryException {
		Address address =
			m_machine.newAddress(m_state.getDS(), m_state.getSI());
		m_state.setAX(m_memory.readWord(address));
		byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2);
		m_state.setSI((short)(m_state.getSI() + diff));
	}

	/**
	 * Implements the 'scasb' opcode.
	 * @throws MemoryException
	 */
	protected void scasb() throws MemoryException {
		Address address =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		sub8(m_state.getAL(), m_memory.readByte(address));
		byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1);
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	/**
	 * Implements the 'scasw' opcode.
	 * @throws MemoryException
	 */
	protected void scasw() throws MemoryException {
		Address address =
			m_machine.newAddress(m_state.getES(), m_state.getDI());
		sub16(m_state.getAX(), m_memory.readWord(address));
		byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2);
		m_state.setDI((short)(m_state.getDI() + diff));
	}

	protected void rol8(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31

		for (int i = 0; i < count; ++i) {
			byte val = m_indirect.getMem8();
			byte msb1 = (byte)((val >> 7) & 0x01);
			byte msb2 = (byte)((val >> 6) & 0x01);

			val = (byte)((val << 1) | msb1);
			m_indirect.setMem8(val);

			m_state.setCarryFlag(msb1 != 0);
			m_state.setOverflowFlag(msb1 != msb2);
		}
	}

	protected void ror8(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31

		for (int i = 0; i < count; ++i) {
			byte val = m_indirect.getMem8();
			byte lsb = (byte)(val & 0x01);

			val = (byte)((val >>> 1) | (lsb << 7));
			m_indirect.setMem8(val);

			byte msb1 = (byte)((val >> 7) & 0x01);
			byte msb2 = (byte)((val >> 6) & 0x01);

			m_state.setCarryFlag(lsb != 0);
			m_state.setOverflowFlag(msb1 != msb2);
		}
	}

	protected void rcl8(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem8() & 0xFF | (m_state.getCarryFlag()?0x100:0);
		val = (val<<count)|(val>>(9-count));

		m_indirect.setMem8((byte)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>7)&0x1)!=((val>>8)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		m_state.setCarryFlag((val&0x100)==0x100);
	}

	protected void rcr8(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem8() & 0xFF | (m_state.getCarryFlag()?0x100:0);
		val = (val>>count)|(val<<(9-count));

		m_indirect.setMem8((byte)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>7)&0x1)!=((val>>6)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		m_state.setCarryFlag((val&0x100)==0x100);
	}

	protected void shl8(int count) throws MemoryException {
		count &= 0x1f; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem8()&0xff;
		val <<= count;

		m_indirect.setMem8((byte)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>7)&0x1)!=((val>>8)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		updateFlags8(val);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
	}

	protected void shr8(int count) throws MemoryException {
		count &= 0x1f; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem8()&0xff;
		val >>= (count-1);
		val = ((val&1)<<8)|(val>>1);//move last bit to carry

		m_indirect.setMem8((byte)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>7)&0x1)!=((val>>6)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		updateFlags8(val);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
	}

	protected void sar8(int count) throws MemoryException {
		count &= 0x1f; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem8();
		val = (val >> (count-1)) & 0x1ff;
		val = ((val&1)<<8)|(val>>1);//move last bit to carry

		m_indirect.setMem8((byte)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>7)&0x1)!=((val>>6)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		updateFlags8(val);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
	}

	protected void rol16(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem16() & 0xFFFF;
		val = (val<<count)|(val>>(16-count));

		if(count==1){
			m_state.setOverflowFlag(((val>>16)&0x1)!=(val&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		m_state.setCarryFlag((val&0x1)==1);

		m_indirect.setMem16((short)val);
	}

	protected void ror16(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem16() & 0xFFFF;
		val = (val>>count)|(val<<(16-count));

		if(count==1){
			m_state.setOverflowFlag(((val>>16)&0x1)!=(val&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}

		m_state.setCarryFlag((val&0x8000)==0x8000);

		m_indirect.setMem16((short)val);
	}

	protected void rcl16(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem16() & 0xFFFF | (m_state.getCarryFlag()?0x10000:0);
		val = (val<<count)|(val>>(17-count));

		m_indirect.setMem16((short)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>15)&0x1)!=((val>>16)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		m_state.setCarryFlag((val&0x10000)==0x10000);
	}

	protected void rcr16(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem16() & 0xFFFF | (m_state.getCarryFlag()?0x10000:0);
		val = (val>>count)|(val<<(17-count));

		m_indirect.setMem16((short)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>15)&0x1)!=((val>>14)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		m_state.setCarryFlag((val&0x10000)==0x10000);
	}

	protected void shl16(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem16();
		val <<= count;

		m_indirect.setMem16((short)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>16)&0x1)!=((val>>15)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		m_state.setCarryFlag((val&0x10000)==0x10000);
		updateFlagsNoCarryOverflow16((short)val);
		m_state.setAuxFlag(true);
	}

	protected void shr16(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem16()&0xffff;
		val >>= (count-1);
		val = ((val&1)<<16)|(val>>1);//move last bit to carry

		m_indirect.setMem16((short)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>15)&0x1)!=((val>>14)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		m_state.setCarryFlag((val&0x10000)==0x10000);
		updateFlagsNoCarryOverflow16((short)val);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
	}

	protected void sar16(int count) throws MemoryException {
		count &= 0x1F; // restrict count to 0-31
		if(count==0){
			return;
		}

		int val = m_indirect.getMem16();
		val = (val >> (count-1)) & 0x1ffff;
		val = ((val&1)<<16)|(val>>1);//move last bit to carry

		m_indirect.setMem16((short)val);

		if(count==1){
			m_state.setOverflowFlag(((val>>15)&0x1)!=((val>>14)&0x1));
		} else {
			m_state.setUndefinedFlags(CpuState.FLAGS_MASK_OVERFLOW);
		}
		m_state.setCarryFlag((val&0x10000)==0x10000);
		updateFlagsNoCarryOverflow16((short)val);
		m_state.setUndefinedFlags(CpuState.FLAGS_MASK_AUX);
	}

	/**
	 * Returns true iff the given byte's bit parity is EVEN.
	 * @param value  Value for which bit parity will be tested.
	 * @return true iff the given byte's bit parity is EVEN.
	 */
	protected boolean getParity(byte value) {
		int p = value & 0xff;
		p = p ^ (p>>4);
		p = p ^ (p>>2);
		p = (p ^ (p>>1)) & 1;
		if( (p == 0) != PARITY_TABLE[Unsigned.unsignedByte(value)])
			System.out.println("Olmadi");
		return PARITY_TABLE[Unsigned.unsignedByte(value)];
	}

	/**
	 * Parity table implementation.
	 * An array memeber of 'true' means the bit parity for the given index is EVEN.
	 */
	protected static final boolean PARITY_TABLE[] = {
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true,
			true, false, false, true, false, true, true, false,
			true, false, false, true, false, true, true, false,
			false, true, true, false, true, false, false, true};

	/** Current state of registers & flags */
	protected CpuState m_state;

	/** Real-Mode memory used by this class */
	protected RealModeMemory m_memory;

	/** Used to fetch next instruction bytes */
	protected OpcodeFetcher m_fetcher;

	/** Used to decode register indexing in opcodes */
	protected RegisterIndexingDecoder m_regs;

	/** Used to decode indirect-addressing opcodes */
	protected IndirectAddressingDecoder m_indirect;

}
