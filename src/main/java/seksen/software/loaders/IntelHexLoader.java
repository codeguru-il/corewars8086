/*
 * IntelHexLoader.java
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
package seksen.software.loaders;

import java.io.File;

import seksen.hardware.Machine;
import seksen.hardware.cpu.CpuState;
import seksen.hardware.memory.MemoryException;
import seksen.util.IntelHexParseListener;
import seksen.util.IntelHexParser;

public class IntelHexLoader implements IntelHexParseListener {
	private final Machine machine;

	public IntelHexLoader(Machine machine) {
		this.machine = machine;
	}

	public void load(File file) throws Exception {
		IntelHexParser.parse(file, this);
	}

	public void data(int address, byte[] data) {
		try {
			machine.memory.writeMemory(
				machine.newAddress(address), data, 0, data.length);
		} catch (MemoryException e) {
		}
	}

	public void startSegment(int segment, int offset) {
        CpuState state = machine.state;
        state.setCS(segment);
        state.setIP(offset);
	}

}
