/*
 * Machine.java
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import seksen.hardware.cpu.Cpu;
import seksen.hardware.cpu.Cpu8086;
import seksen.hardware.cpu.CpuException;
import seksen.hardware.cpu.CpuState;
import seksen.hardware.memory.IOOverMemory;
import seksen.hardware.memory.MemoryAccessProtection;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.RealModeMemory;

/**
 * @author Erdem Güven
 *
 */
public class Machine {
	/**Normal addressing*/
	public static final int ADDR_20 		= 0;
	/**Extended addressing*/
	public static final int ADDR_24 		= 2;

	public static final int ACCESS_CHECK	= 4;
	public static final int TEST_INTERPRETER= 8;

	private final int type;

	public final Cpu cpu;
	public final CpuState state;
	public final RealModeMemory memory;
	public final Address address;
	public final int addressType;

	private final InterruptHandler intHandler;
	private final MemoryAccessProtection memoryProtection;
	private final IOHandler ioHandler;
	private final Timer timer;
	private final IOPort serialPort;

	private final Device devices[];

	private Writer logWriter;
	private Writer outWriter;

	public Machine( int type )
	{
		this.type = type;

		if( !isExtended() ) {
			this.addressType = Address.ADR20;
			this.address =  new Address20(0,0);
		} else {
			this.addressType = Address.ADR24;
			this.address = new Address24(0,0);
		}

		//this.memory = new RealModeMemoryImpl();
		this.memory = new IOOverMemory();
		this.state = new CpuState();

		this.cpu = new Cpu8086();

		this.ioHandler = new IOHandler();

		this.intHandler = null;


		if( isAccessCheckEnabled() ) {
			this.memoryProtection = new MemoryAccessProtection();
			memory.addAccessListener(memoryProtection);
		} else {
			this.memoryProtection = null;
		}

		timer = new Timer();
		serialPort = new IOPort(256, 256);

		devices = new Device[]{cpu, state, memory, intHandler, memoryProtection,
			ioHandler, timer, serialPort};

		for(Device dev:devices) {
			if( dev != null ) {
				dev.setMachine(this);
			}
		}
	}

	private boolean isExtended() {
		return (type & ADDR_24) == ADDR_24;
	}

	private boolean isTestEnabled() {
		return (type & TEST_INTERPRETER) == TEST_INTERPRETER;
	}

	private boolean isAccessCheckEnabled() {
		return (type & ACCESS_CHECK) == ACCESS_CHECK;
	}

	public Device getDevice(Class cls) {
		for(int a=0; a<devices.length; a++){
			if(cls.isInstance(devices[a])){
				return devices[a];
			}
		}
		return null;
	}

	public Address newAddress(int seg, int off) {
		return address.newAddress(seg,off);
	}

	public Address newAddress(int i) {
		return address.newAddress(i);
	}

	public void reset(){
		for( int a=0; a<devices.length; a++ ) {
			if( devices[a] != null ) {
				devices[a].reset();
			}
		}
	}

	public void save(OutputStream output) throws IOException {
		output.write(type);

		for(int a=0; a<devices.length; a++){
			if(devices[a] instanceof Storable){
				((Storable)devices[a]).save(output);
			}
		}
	}

	public void load(InputStream input) throws IOException {
		if( input.read() != type ){
			throw new IOException("Wrong machine type");
		}

		load2(input);
	}

	protected void load2(InputStream input) throws IOException {
		reset();

		for(int a=0; a<devices.length; a++){
			if(devices[a] instanceof Storable){
				((Storable)devices[a]).load(input);
			}
		}
	}

	static public Machine loadNewMachine(InputStream input) throws IOException {
		int type = input.read();
		Machine mac = new Machine(type);
		mac.load2(input);

		return mac;
	}

	public void setLogWriter(Writer lwriter){
		logWriter = lwriter;
	}

	public void log(String log) {
		if( logWriter != null ) {
			try {
				logWriter.write(log);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setOutWriter(Writer lwriter){
		outWriter = lwriter;
	}

	public void out(String log) {
		if( outWriter != null ) {
			try {
				outWriter.write(log);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void do_cycle() throws CpuException, MemoryException
	{
		timer.do_cycle();
		cpu.nextOpcode();
	}
}
