/*
 * MemoryVmCompare.java
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

import java.io.IOException;
import java.io.InputStream;

import seksen.hardware.Address;
import seksen.jni.VM86;

public class MemoryVmCompare extends RealModeMemoryImpl {
	boolean copyWrite = true;

	public void setCompareWrite(){
		copyWrite = false;
	}

	public void setCopyWrite(){
		copyWrite = true;
	}

	public void writeByte(Address address, byte value) throws MemoryException {
		byte[] data = new byte[1];
		if(copyWrite){
			data[0] = value;
			VM86.writeMemory(address.getLinearAddress(), data, 0, 1);
		} else {
			VM86.readMemory(address.getLinearAddress(), data, 0, 1);
			if( data[0] != value ){
				byte vmvalue = data[0];
				data[0] = readByte(address);
				VM86.writeMemory(address.getLinearAddress(), data, 0, 1); // Reset value
				throw new MemoryException("VM86 memory differs at "+address+" "+vmvalue+"!="+value);
			}
		}

		super.writeByte(address, value);
	}

	public void writeMemory(Address address, byte[] data, int offset, int size) {
		super.writeMemory(address, data, offset, size);
		if(copyWrite){
			VM86.writeMemory(address.getLinearAddress(), data, offset, size);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public void writeMemory(Address address, InputStream input, int size) throws IOException {
		super.writeMemory(address, input, size);
		if(copyWrite){
			byte[] data = readMemory(address, size);
			VM86.writeMemory(address.getLinearAddress(), data, 0, size);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public void fillMemory(Address address, byte val, int size) {
		super.fillMemory(address, val, size);
		if(copyWrite){
			VM86.fillMemory(address.getLinearAddress(), val, size);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
