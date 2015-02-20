/*
 * IOHandler.java
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

import seksen.util.Hex;

public class IOHandler implements Device {
	private Machine machine;
	private Timer timer;
	private IOPort serialPort;

	private short regA080;

	public void setMachine(Machine machine) {
		this.machine = machine;
		timer = (Timer) machine.getDevice(Timer.class);
		serialPort = (IOPort) machine.getDevice(IOPort.class);
	}

    public void reset() {
    }

	public short inw(int port) {
		short value = 0;
		switch(port) {
		case 0xff66: value = 0x20; break;
		}
		machine.log("IN  "+Hex.toHexString(port, 4)+" => 0x"+Hex.toHexString(value, 4)+"\n");
		return value;
	}

	public void outw(int port, short value) {
		machine.log("OUT 0x"+Hex.toHexString(port, 4)+", 0x"+Hex.toHexString(value, 4)+"\n");
	}

	public short readw(int address) {
		int value = 0;
		switch( address ) {
		case 0x8064: value = 0x1; break; // LOW_BW_ADC_STATUS
		case 0xA080: value = regA080; break;
		case 0xA082: value = 0x40; break; // UART status
		case 0xB43A:						// ; X86_AUDIP_MPE_RDATA1, MPE_RDATA1
		case 0xB43E: value = 0x8000; break; // X86_AUDIP_MPE_RDATA3, MPE_RDATA3
		}
		return (short) value;
	}

	public void writew(int address, short value) {
		switch( address ) {
		case 0xA062:
		case 0xA066:
		    if( timer != null ) {
			timer.io_writew(address, value);
		    }
			break;
		case 0xA080: regA080 = value; break;
		case 0xA084:
		    if(serialPort != null) {
			serialPort.out((byte)value);
		    }
		    break;
		}
	}
}
