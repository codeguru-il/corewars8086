package il.co.codeguru.corewars8086.hardware;

import il.co.codeguru.corewars8086.hardware.cpu.Cpu;
import il.co.codeguru.corewars8086.hardware.cpu.CpuException;
import il.co.codeguru.corewars8086.hardware.cpu.CpuState;
import il.co.codeguru.corewars8086.hardware.memory.IOOverMemory;
import il.co.codeguru.corewars8086.hardware.memory.MemoryAccessProtection;
import il.co.codeguru.corewars8086.hardware.memory.MemoryException;
import il.co.codeguru.corewars8086.hardware.memory.RealModeMemory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author Erdem Güven
 *
 */
public class Machine {
	public final Cpu cpu;
	public final CpuState state;
	public final RealModeMemory memory;
	public final AbstractAddress address;

	public final MemoryAccessProtection memoryProtection;
	public final IOHandler ioHandler;
	public final IOPort serialPort;

	private final Device devices[];

	private Writer logWriter;
	private Writer outWriter;

	public Machine()
	{
		this.address =  new Address(0,0);

		//this.memory = new RealModeMemoryImpl();
		this.memory = new IOOverMemory();
		this.state = new CpuState();

		this.cpu = new Cpu();

		this.ioHandler = new IOHandler();

		this.memoryProtection = new MemoryAccessProtection();
		memory.addAccessListener(memoryProtection);

		serialPort = new IOPort(256, 256);

		devices = new Device[]{cpu, state, memory, memoryProtection, ioHandler, serialPort};

		for(Device dev:devices) {
			if( dev != null ) {
				dev.setMachine(this);
			}
		}
	}

	public Device getDevice(Class cls) {
		for(int a=0; a<devices.length; a++){
			if(cls.isInstance(devices[a])){
				return devices[a];
			}
		}
		return null;
	}

	public AbstractAddress newAddress(int seg, int off) {
		return address.newAddress(seg,off);
	}

	public AbstractAddress newAddress(int i) {
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
		for(int a=0; a<devices.length; a++){
			if(devices[a] instanceof Storable){
				((Storable)devices[a]).save(output);
			}
		}
	}

	public void load(InputStream input) throws IOException {
		reset();

		for(int a=0; a<devices.length; a++){
			if(devices[a] instanceof Storable){
				((Storable)devices[a]).load(input);
			}
		}
	}

	static public Machine loadNewMachine(InputStream input) throws IOException {
		Machine mac = new Machine();
		mac.load(input);

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

	public void do_cycle() throws CpuException, MemoryException, InterruptException {
		cpu.nextOpcode();
	}
}
