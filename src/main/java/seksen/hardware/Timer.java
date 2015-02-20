/*
 * Timer.java
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import seksen.hardware.memory.MemoryException;

public class Timer implements Storable, Device {
	private static final int TIMER_INT_NUMBER = 19;
	private static final int COUNTER_RESET = 1000; /* ~1ms */
	private static final int COUNTER_STOP = -1;

	private Machine machine;
	private int counter;

	public void setMachine(Machine mac) {
		machine = mac;
		reset();
	}

	public void reset() {
		stop_counter();
	}

	public void io_writew(int address, short value) {
		switch( address ) {
		case 0xA062: break; // clock rate
		case 0xA066:
			if( value == (short)0xE001 ) {
				reset_counter();
			}
			break;
		}
	}

	public void do_cycle()
	{
		if( count() ) {
			try {
				machine.cpu.interrupt(TIMER_INT_NUMBER);
			} catch (InterruptException e) {
				e.printStackTrace();
			} catch (MemoryException e) {
				e.printStackTrace();
			}
		}
	}

	private void stop_counter() {
		counter = COUNTER_STOP;
	}

	private void reset_counter() {
		counter = COUNTER_RESET;
	}

	private boolean is_counting() {
		return counter >= 0;
	}

	private boolean count() {
		if( !is_counting() ) {
			return false;
		}

		if( counter == 0 ) {
			reset_counter();
		} else {
			counter--;
		}

		return counter == 0;
	}

	public void load(InputStream input) throws IOException {
		DataInputStream datain = new DataInputStream(input);
		try {
			counter = datain.readInt();
		} catch (EOFException e) {
		}
	}

	public void save(OutputStream output) throws IOException {
		DataOutputStream dataout = new DataOutputStream(output);
		dataout.writeInt(counter);
	}
}
