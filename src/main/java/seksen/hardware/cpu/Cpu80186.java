/*
 * Cpu80186.java
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
package seksen.hardware.cpu;

import seksen.hardware.Machine;
import seksen.hardware.memory.MemoryException;

public class Cpu80186 extends Cpu8086 {

	public void setMachine(Machine machine) {
		super.setMachine(machine);
	}

	public void processOpcode(byte opcode) throws CpuException, MemoryException {
		int opc = opcode&0xff;

		switch( opc ) {
		case 0x60:{ // PUSHA
			int sp = m_state.getSP();
			push(m_state.getAX());
			push(m_state.getCX());
			push(m_state.getDX());
			push(m_state.getBX());
			push(sp);
			push(m_state.getBP());
			push(m_state.getSI());
			push(m_state.getDI());
		}break;
		case 0x61:{ // POPA
			m_state.setDI(pop());
			m_state.setSI(pop());
			m_state.setBP(pop());
			pop(); // ignore sp
			m_state.setBX(pop());
			m_state.setDX(pop());
			m_state.setCX(pop());
			m_state.setAX(pop());
		}break;
		case 0x68: // PUSH iw
			push(m_fetcher.nextWord());
			break;
		case 0x69:{ // IMUL reg,reg,word
			m_indirect.reset();
			int product = ((short)m_indirect.getReg16())*m_fetcher.nextWord();
			m_indirect.setMem16((short)(product));
			m_state.setFlag(CpuState.FLAGS_MASK_CARRY|CpuState.FLAGS_MASK_OVERFLOW,
					product>0x7fff||product<-0x8000);
		}break;
		case 0x6A: // PUSH ib
			push(m_fetcher.nextByte());
			break;
		case 0x6B:{ // IMUL reg,reg,byte
			m_indirect.reset();
			int product = ((short)m_indirect.getReg16())*m_fetcher.nextByte();
			m_indirect.setMem16((short)(product));
			m_state.setFlag(CpuState.FLAGS_MASK_CARRY|CpuState.FLAGS_MASK_OVERFLOW,
					product>0x7fff||product<-0x8000);
		}break;
		case 0xC0:
			m_indirect.reset();
			switch (m_indirect.getRegIndex()) {
				case (byte)0x00: // ROL
					rol8(m_fetcher.nextByte());
					break;
				case (byte)0x01: // ROR
					ror8(m_fetcher.nextByte());
					break;
				case (byte)0x02: // RCL
					rcl8(m_fetcher.nextByte());
					break;
				case (byte)0x03: // RCR
					rcr8(m_fetcher.nextByte());
					break;
				case (byte)0x04: // SHL
					shl8(m_fetcher.nextByte());
					break;
				case (byte)0x05: // SHR
					shr8(m_fetcher.nextByte());
					break;
				case (byte)0x06: // invalid opcode
					throw new InvalidOpcodeException();
				case (byte)0x07: // SAR
					sar8(m_fetcher.nextByte());
					break;
				default:
					throw new RuntimeException();
			}
			break;
		case 0xC1:
			m_indirect.reset();
			switch (m_indirect.getRegIndex()) {
				case (byte)0x00: // ROL
					rol16(m_fetcher.nextByte());
					break;
				case (byte)0x01: // ROR
					ror16(m_fetcher.nextByte());
					break;
				case (byte)0x02: // RCL
					rcl16(m_fetcher.nextByte());
					break;
				case (byte)0x03: // RCR
					rcr16(m_fetcher.nextByte());
					break;
				case (byte)0x04: // SHL
					shl16(m_fetcher.nextByte());
					break;
				case (byte)0x05: // SHR
					shr16(m_fetcher.nextByte());
					break;
				case (byte)0x06: // invalid opcode
					throw new InvalidOpcodeException();
				case (byte)0x07: // SAR
					sar16(m_fetcher.nextByte());
					break;
				default:
					throw new RuntimeException();
			}
			break;
		case 0xC8: // ENTER
			push(m_state.getBP());
			m_state.setBP(m_state.getSP());
			m_state.setSP((short)(m_state.getSP() - m_fetcher.nextWord()));
			m_fetcher.nextByte(); //TODO implement nesting
			break;
		case 0xC9: // LEAVE
			m_state.setSP(m_state.getBP());
			m_state.setBP(pop());
			break;
		default:
			super.processOpcode(opcode);
			break;
		}
	}
}
