/*
 * IOPort.java
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
package seksen.hardware;

import java.io.InputStream;
import java.io.OutputStream;

import seksen.util.BufferInputStream;
import seksen.util.BufferOutputStream;

public class IOPort implements Device {

	private BufferOutputStream input_buffer;
	private BufferInputStream output_buffer;

	public IOPort(int input_buffer_size, int output_buffer_size) {
		input_buffer = new BufferOutputStream(input_buffer_size);
		output_buffer = new BufferInputStream(output_buffer_size);
	}

	public void setMachine(Machine mac) {
	}

	public void reset() {
		input_buffer.resetBuffer();
		output_buffer.resetBuffer();
	}

	public byte in() {
		return (byte) input_buffer.read();
	}

	public void out(byte b) {
		output_buffer.write(b);
	}

	public OutputStream getOutputStream() {
		return input_buffer;
	}

	public InputStream getInputStream() {
		return output_buffer;
	}
}
