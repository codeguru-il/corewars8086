/*
 * BufferInputStream.java
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
package seksen.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BufferInputStream extends InputStream {
	private byte[] buffer;
	private int index;
	private int size;

	public BufferInputStream() {
		this(16);
	}

	public BufferInputStream( int buffer_size ) {
		buffer = new byte[buffer_size];
		index = 0;
		size = 0;
	}

	public void resetBuffer() {
		synchronized (buffer) {
			index = 0;
			size = 0;
		}
	}

	public void write(int b) {
		synchronized( buffer ) {
			buffer[index] = (byte) b;
			index = (index+1) % buffer.length;
			if( size < buffer.length ) {
				size++;
			}
		}
	}

	public int read() {
		synchronized( buffer ) {
			if( size > 0 ){
				int i = (index + buffer.length - size) % buffer.length;
				return buffer[i];
			} else {
				return 0;
			}
		}
	}

	@Override
	public int available() throws IOException {
		return size;
	}
}
