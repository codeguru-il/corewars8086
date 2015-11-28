package il.co.codeguru.corewars8086.hardware.memory;

import il.co.codeguru.corewars8086.hardware.AbstractAddress;
import il.co.codeguru.corewars8086.hardware.Device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for 16bit Real-Mode memory.
 *
 * @author DL
 * @author Erdem Guven
 */
public abstract class RealModeMemory implements Device {

	/**
	 * Reads a single byte from the specified address.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read byte.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract byte readByte(AbstractAddress address) throws MemoryException;

	/**
	 * Reads a single word from the specified address.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read word.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract short readWord(AbstractAddress address) throws MemoryException;

	public abstract int readDWord(AbstractAddress address) throws MemoryException;

	/**
	 * Writes a single byte to the specified address.
	 *
	 * @param address    Real-mode address to write to.
	 * @param value      Data to write.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract void writeByte(AbstractAddress address, byte value)
			throws MemoryException;

	/**
	 * Writes a single word to the specified address.
	 *
	 * @param address    Real-mode address to write to.
	 * @param value      Data to write.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract void writeWord(AbstractAddress address, int value)
			throws MemoryException;

	public abstract void writeDWord(AbstractAddress address, long value)
			throws MemoryException;
	/**
	 * Reads a single byte from the specified address, in order to execute it.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read byte.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract byte readExecuteByte(AbstractAddress address)
			throws MemoryException;

	/**
	 * Reads a single word from the specified address, in order to execute it.
	 *
	 * @param address    Real-mode address to read from.
	 * @return the read word.
	 *
	 * @throws MemoryException  on any error.
	 */
	public abstract short readExecuteWord(AbstractAddress address)
			throws MemoryException;

	/**
	 * Reads many bytes at once.
	 * @param linearAddr
	 * @param size
	 * @return
	 * @throws MemoryException
	 */
	public abstract byte[] readMemory(AbstractAddress addr, int size)
			throws MemoryException;

	/**
	 * Writes many bytes at once.
	 * @param linearAddr
	 * @param size
	 * @return
	 * @throws MemoryException
	 */
	public abstract void writeMemory(AbstractAddress addr, InputStream input, int size)
			throws IOException, MemoryException;

	public abstract void writeMemory(AbstractAddress adr, byte[] data, int offset, int size)
			throws MemoryException;

	public abstract void fillMemory(AbstractAddress address, byte data, int size) throws MemoryException;


	/**
	 * Writes some part of memory to a output stream.
	 * @param addr	Start address of part.
	 * @param size	Size of part.
	 * @param output Output stream
	 * @throws IOException
	 */
	public abstract void write2file(AbstractAddress addr, int size, OutputStream output) throws IOException;

	public abstract void setMemory(AbstractAddress addr, int size, byte value);

	/**
	 * @return Returns
	 */
	public abstract int getMaxAddr();

	public abstract AbstractAddress newAddress(int seg, int off);

	public abstract void addAccessListener(MemoryAccessListener listener);
	public abstract void removeAccessListener(MemoryAccessListener listener);
	public abstract void setAccessListenersEnabled(boolean enable);
}
