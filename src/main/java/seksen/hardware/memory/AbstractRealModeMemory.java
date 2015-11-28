/*
 * AbstractRealModeMemory.java
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

package seksen.hardware.memory;

import seksen.hardware.Address;
import seksen.util.Unsigned;

/**
 * Base class for classes implementing the RealModeMemory interface, which
 * provides simple implementation of the 'word' methods using the 'byte' methods.
 *
 * @author DL
 * @author Erdem Guven
 */
public abstract class AbstractRealModeMemory extends RealModeMemory {

	protected MemoryAccessListener[] listeners = null;

	/**
	 * Reads a single byte from the specified address.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read byte.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract byte readByte(Address address) throws MemoryException;

	/**
	 * Reads a single word from the specified address.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read word.
	 *
	 * @throws MemoryException  on any error.
	 */
	public short readWord(Address address) throws MemoryException {
		return (short)(
				(Unsigned.unsignedByte(readByte(address.addOffset(1))) << 8) |
				Unsigned.unsignedByte(readByte(address)));
	}

	public int readDWord(Address address) throws MemoryException {
		return (Unsigned.unsignedShort(readWord(address.addOffset(2))) << 16) |
				Unsigned.unsignedShort(readWord(address));
	}

	/**
	 * Writes a single byte to the specified address.
	 *
	 * @param address    Real-mode address to write to.
	 * @param value      Data to write.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract void writeByte(Address address, byte value)
		throws MemoryException;

	/**
	 * Writes a single word to the specified address.
	 *
	 * @param address    Real-mode address to write to.
	 * @param value      Data to write.
	 *
	 * @throws MemoryException  on any error.
	 */
	public void writeWord(Address address, short value)
		throws MemoryException {
		writeByte(address, (byte)value);
		writeByte(address.addOffset(1), (byte)(value >> 8));
	}

	public void writeDWord(Address address, int value) throws MemoryException {
		writeWord(address, (short)value);
		writeWord(address.addOffset(2), (short)(value >> 16));
	}

	/**
	 * Reads a single byte from the specified address, in order to execute it.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read byte.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract byte readExecuteByte(Address address)
		throws MemoryException;

	/**
	 * Reads a single word from the specified address, in order to execute it.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read word.
	 *
	 * @throws MemoryException  on any error.
	 */
	public short readExecuteWord(Address address) throws MemoryException {
		return (short)(
				(Unsigned.unsignedByte(readExecuteByte(address.addOffset(1))) << 8) |
				Unsigned.unsignedByte(readExecuteByte(address)));
	}

	public void addAccessListener(MemoryAccessListener listener) {
		if(listeners == null){
			listeners = new MemoryAccessListener[1];
			listeners[0] = listener;
		} else {
			MemoryAccessListener[] newlisteners = new MemoryAccessListener[listeners.length+1];
			System.arraycopy(listeners, 0, newlisteners, 0, listeners.length);
			listeners = newlisteners;
			listeners[listeners.length-1] = listener;
		}
	}


	public void removeAccessListener(MemoryAccessListener listener) {
		if(listeners == null){
			return;
		}
		for(int a=0;a<listeners.length;a++){
			if(listeners[a]==listener){
				MemoryAccessListener[] newlisteners = new MemoryAccessListener[listeners.length-1];
				if(a>0){
					System.arraycopy(listeners, 0, newlisteners, 0, a);
				}
				if(listeners.length-a-1>0){
					System.arraycopy(listeners, a+1, newlisteners, a, listeners.length-a-1);
				}
				listeners = newlisteners;
				break;
			}
		}
	}

	public void setAccessListenersEnabled(boolean enable) {
		notifying = !enable;
	}

	protected static final int READ_ACCESS = 0;
	protected static final int WRITE_ACCESS = 1;
	protected static final int EXEC_ACCESS = 2;
	private boolean notifying = true;

	protected void notifyListeners(int accessMode, Address address, int size) throws MemoryException{
		if(notifying || listeners == null ){
			return;
		}
		notifying = true;
		try{
			if(accessMode == READ_ACCESS){
				for(int a=0; a<listeners.length; a++){
					listeners[a].readMemory(address, size);
				}
			} else if(accessMode == WRITE_ACCESS){
				for(int a=0; a<listeners.length; a++){
					listeners[a].writeMemory(address, size);
				}
			} else {
				for(int a=0; a<listeners.length; a++){
					listeners[a].readExecuteMemory(address, size);
				}
			}
		} finally {
			notifying = false;
		}
	}
}
