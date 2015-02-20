/*
 * Cpu.java
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

import java.util.Vector;

import seksen.hardware.Address;
import seksen.hardware.Device;
import seksen.hardware.InterruptException;
import seksen.hardware.memory.MemoryException;

public abstract class Cpu implements Device {

	protected int instructionCounter = 0;

	protected Vector listeners;

	public void reset(){
		instructionCounter = 0;
	}

	/**
	 * Performs the next single opcode.
	 *
	 * @throws CpuException    on any CPU error.
	 * @throws MemoryException on any Memory error.
	 */
	public abstract void nextOpcode() throws CpuException, MemoryException;

	public abstract void interrupt(int intnum)
		throws InterruptException, MemoryException;

	public void addCallListener(CallListener listener){
		if(listeners == null){
			listeners = new Vector();
		}
		listeners.add(listener);
	}

	public void removeCallListener(CallListener listener){
		if(listeners != null){
			listeners.remove(listener);
		}
	}

	protected void notifyCallListeners(Address from, Address to) {
		if(listeners == null){
			return;
		}
		int size = listeners.size();
		for(int a=0; a<size; a++){
			((CallListener)listeners.elementAt(a)).callInst(from,to);
		}
	}

	public int getInstructionCounter(){
		return instructionCounter;
	}
}