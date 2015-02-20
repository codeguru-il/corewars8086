package il.co.codeguru.corewars8086.hardware.memory;

/**
 * Interface for 16bit Real-Mode memory.
 * 
 * @author DL
 */
public interface RealModeMemory {

    /**
     * Reads a single byte from the specified address.
     *
     * @param address    Real-mode address to read from.
     * @return the read byte.
     * 
     * @throws MemoryException  on any error. 
     */
    public abstract byte readByte(Address address) throws MemoryException;

    /**
     * Reads a single word from the specified address.
     *
     * @param address    Real-mode address to read from.
     * @return the read word.
     * 
     * @throws MemoryException  on any error. 
     */
    public abstract short readWord(Address address) throws MemoryException;

    /**
     * Writes a single byte to the specified address.
     *
     * @param address    Real-mode address to write to.
     * @param value      Data to write.
     * 
     * @throws MemoryException  on any error. 
     */
    public abstract void writeByte(Address address, byte value)
        throws MemoryException;

    /**
     * Writes a single word to the specified address.
     *
     * @param address    Real-mode address to write to.
     * @param value      Data to write.
     * 
     * @throws MemoryException  on any error. 
     */	
    public abstract void writeWord(Address address, short value)
        throws MemoryException;

    /**
     * Reads a single byte from the specified address, in order to execute it.
     *
     * @param address    Real-mode address to read from.
     * @return the read byte.
     * 
     * @throws MemoryException  on any error. 
     */
    public abstract byte readExecuteByte(Address address)
        throws MemoryException;

    /**
     * Reads a single word from the specified address, in order to execute it.
     *
     * @param address    Real-mode address to read from.
     * @return the read word.
     * 
     * @throws MemoryException  on any error. 
     */
    public abstract short readExecuteWord(Address address)
        throws MemoryException;	
}
