/*
 * CpuVmCompare.java
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

import seksen.hardware.InterruptException;
import seksen.hardware.Machine;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.MemoryVmCompare;
import seksen.hardware.memory.RealModeMemory;
import seksen.jni.VM86;
import seksen.util.Hex;


public class CpuVmCompare extends Cpu80186 implements CpuStateListener{

	private int vmint = -1;
	private MemoryVmCompare vmmem = null;
	private CpuState vmstate = null;
	private boolean sschanged = false;

	public void setMachine(Machine machine) {
		super.setMachine(machine);
		if( VM86.mapMemory(0, 0x100000) != 0 ){
    		System.out.println("mmap failed");
    		System.exit(1);
    	}

		RealModeMemory mem = machine.memory;
		if (mem instanceof MemoryVmCompare) {
			vmmem = (MemoryVmCompare) mem;
		}

		((CpuStateWatcher)machine.state).setListener(this);
	}

	public void nextOpcode() throws CpuException, MemoryException {
		vmstate = (CpuState) m_machine.state.clone();
		boolean tflag = vmstate.getTrapFlag();
		vmstate.setTrapFlag(true);

		int err = VM86.runVM80186(vmstate);
		int errtype = (err & VM86.VM86_TYPE_MASK);
		int errarg = (err & VM86.VM86_ARG_MASK) >> 8;

		vmint = -1;
		if(errtype == VM86.VM86_INTx){
			vmint = errarg;
		} else if(errtype != VM86.VM86_TRAP){
			throw new InvalidOpcodeException("VM86 returned "+Hex.toHexString(err, 4));
		}

		if(vmmem!=null){
			vmmem.setCompareWrite();
		}

		sschanged = false;
		super.nextOpcode();
		//When ss changed cpu doesn't process interrupts (and trap) for one instruction
		//So vm86 must be processed next instruction too.
		if(sschanged){
			super.nextOpcode();
		}

		if(vmmem!=null){
			vmmem.setCopyWrite();
		}

		CpuState state = m_machine.state;
		vmstate.setTrapFlag(tflag);
		//In every step vm86 set int flag and some other one.
		//We ignore these flags in comparation.
		vmstate.setFlags((vmstate.getFlags()&~0x0202)|(state.getFlags()&0x0202));

		if(!state.equals(vmstate)){
			String msg = "VM86 registers differ : \n"+vmstate+'\n'+state;
			gotoPrevState();
			throw new InvalidOpcodeException(msg);
		}

		if( vmint != -1 ){
			gotoPrevState();
			throw new InterruptException("VM86 signaled an interrupt but not Cpu8086 : "+vmint);
		}
	}

	public void processOpcode(byte opcode) throws CpuException, MemoryException {
		int opc = opcode&0xff;

		if( opc == 0x9c ){ // PUSHF
			//vm86 had pushed trap flag on so we will
			int flags = m_state.getFlags();
			m_state.setFlag(0x3102,true);
			m_state.setInterruptFlag(false);
			super.processOpcode(opcode);
			m_state.setFlags(flags);
		} else if( opc == 0x9d ){ // POPF
			super.processOpcode(opcode);
			m_state.setTrapFlag(false);
		} else {
			super.processOpcode(opcode);
		}
	}

	public void interrupt(int intnum) throws InterruptException, MemoryException {
		if( vmint == -1 ){
			throw new InterruptException("VM86 didn't signal an interrupt but Cpu8086 : "+intnum);
		}
		if( vmint == -1 ){
			throw new InterruptException("VM86 signaled another interrupt : "+vmint+"!="+intnum);
		}

		if(vmmem!=null){
			vmmem.setCopyWrite();
		}
		super.interrupt(intnum);
		if(vmmem!=null){
			vmmem.setCompareWrite();
		}

		vmstate = (CpuState) m_machine.state.clone();
		vmint = -1;
	}

	public void registerChanged(int regIndex) {
		if(regIndex == CpuStateListener.INDEX_SS){
			sschanged = true;
		}
	}
}
