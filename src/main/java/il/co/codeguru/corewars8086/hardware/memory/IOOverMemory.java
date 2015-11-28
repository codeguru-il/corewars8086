package il.co.codeguru.corewars8086.hardware.memory;

import il.co.codeguru.corewars8086.hardware.AbstractAddress;
import il.co.codeguru.corewars8086.hardware.IOHandler;
import il.co.codeguru.corewars8086.hardware.Machine;

public class IOOverMemory extends RealModeMemoryImpl {

    protected IOHandler ioHandler;

	public void setMachine(Machine machine) {
		super.setMachine(machine);
		ioHandler = (IOHandler) machine.getDevice(IOHandler.class);
	}

	private int getIOaddress(AbstractAddress address) {
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
	 * @throws seksen.hardware.memory.MemoryException
	 *
	 * @throws seksen.hardware.memory.MemoryException  on any error.
	 */
	public byte readByte(AbstractAddress address) throws MemoryException {
		int addr = getIOaddress(address);
		if( addr != -1 ) {
			notifyListeners(READ_ACCESS,address,1);
			return (byte) ioHandler.readw(addr);
		} else {
			return super.readByte(address);
		}
	}

	public short readWord(AbstractAddress address) throws MemoryException {
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
	 * @throws seksen.hardware.memory.MemoryException
	 *
	 * @throws seksen.hardware.memory.MemoryException  on any error.
	 */
	public void writeByte(AbstractAddress address, byte value) throws MemoryException {
		int addr = getIOaddress(address);
		if( addr != -1 ) {
			notifyListeners(WRITE_ACCESS,address,1);
			ioHandler.writew(addr, value);
		} else {
			super.writeByte(address, value);
		}
	}

	public void writeWord(AbstractAddress address, short value)
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
