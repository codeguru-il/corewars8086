/*
 * IOOverMemory.java
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
package seksen.hardware.memory;

import seksen.hardware.Address;
import seksen.hardware.IOHandler;
import seksen.hardware.Machine;

public class IOOverMemory extends RealModeMemoryImpl {

    protected IOHandler ioHandler;

	public void setMachine(Machine machine) {
		super.setMachine(machine);
		ioHandler = (IOHandler) machine.getDevice(IOHandler.class);
	}

	private int getIOaddress(Address address) {
		int addr = address.getLinearAddress();
		if((addr >= 0x8000) && (addr < 0x10000)) {
			return addr;
		} else {
			return -1;
		}
	}

	/**
	 * Reads a single byte from the specified address.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read byte.
	 * @throws MemoryException
	 *
	 * @throws MemoryException  on any error.
	 */
	public byte readByte(Address address) throws MemoryException {
		int addr = getIOaddress(address);
		if( addr != -1 ) {
			notifyListeners(READ_ACCESS,address,1);
			return (byte) ioHandler.readw(addr);
		} else {
			return super.readByte(address);
		}
	}

	public short readWord(Address address) throws MemoryException {
		int addr = getIOaddress(address);
		if( addr != -1 ) {
			notifyListeners(READ_ACCESS,address,2);
			return ioHandler.readw(addr);
		} else {
			return super.readWord(address);
		}
	}

	/**
	 * Writes a single byte to the specified address.
	 *
	 * @param address    Real-mode address to write to.
	 * @param value      Data to write.
	 * @throws MemoryException
	 *
	 * @throws MemoryException  on any error.
	 */
	public void writeByte(Address address, byte value) throws MemoryException {
		int addr = getIOaddress(address);
		if( addr != -1 ) {
			notifyListeners(WRITE_ACCESS,address,1);
			ioHandler.writew(addr, value);
		} else {
			super.writeByte(address, value);
		}
	}

	public void writeWord(Address address, short value)
		throws MemoryException
	{
		int addr = getIOaddress(address);
		if( addr != -1 ) {
			notifyListeners(WRITE_ACCESS,address,2);
			ioHandler.writew(addr, value);
		} else {
			super.writeWord(address, value);
		}
	}
}
