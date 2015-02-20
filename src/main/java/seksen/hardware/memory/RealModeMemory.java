/*
 * RealModeMemory.java
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import seksen.hardware.Address;
import seksen.hardware.Device;

/**
 * Interface for 16bit Real-Mode memory.
 *
 * @author DL
 * @author Erdem Guven
 */
public abstract class RealModeMemory implements Device {

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
	public abstract short readWord(Address address) throws MemoryException;

	public abstract int readDWord(Address address) throws MemoryException;

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
	public abstract void writeWord(Address address, short value)
		throws MemoryException;

	public abstract void writeDWord(Address address, int value)
		throws MemoryException;
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
	public abstract short readExecuteWord(Address address)
		throws MemoryException;

	/**
	 * Reads many bytes at once.
	 * @param linearAddr
	 * @param size
	 * @return
	 * @throws MemoryException
	 */
	public abstract byte[] readMemory(Address addr, int size)
		throws MemoryException;

	/**
	 * Writes many bytes at once.
	 * @param linearAddr
	 * @param size
	 * @return
	 * @throws MemoryException
	 */
	public abstract void writeMemory(Address addr, InputStream input, int size)
		throws IOException, MemoryException;

	public abstract void writeMemory(Address adr, byte[] data, int offset, int size)
		throws MemoryException;

	public abstract void fillMemory(Address address, byte data, int size) throws MemoryException;


	/**
	 * Writes some part of memory to a output stream.
	 * @param addr	Start address of part.
	 * @param size	Size of part.
	 * @param output Output stream
	 * @throws IOException
	 */
	public abstract void write2file(Address addr, int size, OutputStream output) throws IOException;

	public abstract void setMemory(Address addr, int size, byte value);

	/**
	 * @return Returns
	 */
	public abstract int getMaxAddr();

	public abstract Address newAddress(int seg, int off);

	public abstract void addAccessListener(MemoryAccessListener listener);
	public abstract void removeAccessListener(MemoryAccessListener listener);
	public abstract void setAccessListenersEnabled(boolean enable);
}
