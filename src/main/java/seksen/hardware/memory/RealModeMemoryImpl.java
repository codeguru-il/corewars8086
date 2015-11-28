/*
 * RealModeMemoryImpl.java
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import seksen.hardware.Address;
import seksen.hardware.Machine;
import seksen.hardware.Storable;


/**
 * Implements the RealModeMemory interface using a buffer.
 *
 * @author DL
 * @author Erdem Guven
 */
public class RealModeMemoryImpl extends AbstractRealModeMemory implements Storable{

	/** Actual memory data */
	protected byte[] m_data;

	protected Machine m_machine;

	public void setMachine(Machine machine) {
		m_machine = machine;
		m_data = new byte[m_machine.address.getMaxAddr()];
	}

	public void reset() {
		Arrays.fill(m_data, (byte)0);
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
		notifyListeners(READ_ACCESS,address,1);
		return m_data[address.getLinearAddress()];
	}

	public short readWord(Address address) throws MemoryException {
		notifyListeners(READ_ACCESS,address,2);
		int adr = address.getLinearAddress();
		return (short)((m_data[adr]&0xff) | (m_data[adr+1]<<8));
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
		notifyListeners(WRITE_ACCESS,address,1);
		m_data[address.getLinearAddress()] = value;
	}

	public void writeWord(Address address, short value) throws MemoryException {
		notifyListeners(WRITE_ACCESS,address,2);
		int adr = address.getLinearAddress();
		m_data[adr] = (byte)value;
		m_data[adr+1] = (byte)(value>>8);
	}

	/**
	 * Reads a single byte from the specified address, in order to execute it.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read byte.
	 * @throws MemoryException
	 *
	 * @throws MemoryException  on any error.
	 */
	public byte readExecuteByte(Address address) throws MemoryException {
		notifyListeners(EXEC_ACCESS,address,1);
		return m_data[address.getLinearAddress()];
	}

	public short readExecuteWord(Address address) throws MemoryException {
		notifyListeners(EXEC_ACCESS,address,2);
		int adr = address.getLinearAddress();
		return (short)((m_data[adr]&0xff) | (m_data[adr+1]<<8));
	}

	/**
	 * @param offset		Offset in memory
	 * @param input			Input stream
	 * @throws IOException	If an io error occurs
	 */
	public void writeMemory(Address offset, InputStream input, int size) throws IOException {
		int size2 = m_data.length - offset.getLinearAddress();
		if(size>size2){
			size = size2;
		}
		input.read(m_data,offset.getLinearAddress(),size);
	}

	public void writeMemory(Address adr, byte[] data, int offset, int size) {
		System.arraycopy(data, offset, m_data, adr.getLinearAddress(), size);
	}

	public void fillMemory(Address address, byte val, int size) {
		int i = address.getLinearAddress();
		Arrays.fill(m_data, i, i+size, val);
	}

	public byte[] readMemory(Address linearAddr, int size) {
		int left = m_data.length - linearAddr.getLinearAddress();
		if( left < size ){
			size = left;
		}
		if( size < 0 ){
			return new byte[0];
		}
		byte[] data = new byte[size];
		System.arraycopy(m_data, linearAddr.getLinearAddress(), data, 0, size);
		return data;
	}

	public int getMaxAddr() {
		return m_data.length;
	}

	public Address newAddress(int seg, int off) {
		return m_machine.newAddress(seg,off);
	}

	public void write2file(Address addr, int size, OutputStream output) throws IOException {
		output.write(m_data, addr.getLinearAddress(), size);
	}

	public void setMemory(Address addr, int size, byte value) {
		int from = addr.getLinearAddress();
		Arrays.fill(m_data, from, from+size, value);
	}

	public void save(OutputStream output) throws IOException {
		DataOutputStream objout = new DataOutputStream(output);
		int max = m_data.length;
		int spaces = 8;
		int start = 0;

		// simple compression
		// skip spaces longer than or equal to 8 bytes
		for(int index = 0; index < max; index++) {
			if(spaces < 8){
				if(m_data[index] == 0){
					spaces++;
					if(spaces == 8){
						int len = index - start - 8 + 1;
						objout.writeInt(start);
						objout.writeInt(len);
						objout.write(m_data, start, len);
					}
				} else {
					spaces = 0;
				}
			} else {
				if(m_data[index] != 0){
					start = index;
					spaces = 0;
				}
			}
		}
		if(spaces < 8){
			int len = max - start - spaces;
			objout.writeInt(start);
			objout.writeInt(len);
			objout.write(m_data, start, len);
		}
		objout.writeInt(-1);
		objout.flush();
	}

	public void load(InputStream input) throws IOException {
		Arrays.fill(m_data, (byte)0);

		DataInputStream objin = new DataInputStream(input);
		int index = 0;

		while(true){
			int nindex = objin.readInt();
			if(nindex < index){
				break;
			}
			index = nindex;
			int len = objin.readInt();
			objin.read(m_data, index, len);
		}
	}
}
