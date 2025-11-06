package il.co.codeguru.corewars8086.memory;

import il.co.codeguru.corewars8086.utils.Unsigned;

/**
 * Base class for classes implementing the RealModeMemory interface, which
 * provides simple implementation of the 'word' methods using the 'byte' methods.  
 * 
 * @author DL
 */
public abstract class AbstractRealModeMemory implements RealModeMemory {
	
    /**
     * Reads a single byte from the specified address.
     *
     * @param address    Real-mode address to read from.
     * @return the read byte.
     * 
     * @throws MemoryException  on any error. 
     */
    public abstract byte readByte(RealModeAddress address) throws MemoryException;

    /**
     * Reads a single word from the specified address.
     *
     * @param address    Real-mode address to read from.
     * @return the read word.
     * 
     * @throws MemoryException  on any error. 
     */
    public short readWord(RealModeAddress address) throws MemoryException {
        // read low word
        byte low = readByte(address);

        // read high word
        RealModeAddress nextAddress = new RealModeAddress(
            address.getSegment(), (short)(address.getOffset() + 1));
        byte high = readByte(nextAddress);

        return (short)((Unsigned.unsignedByte(high) << 8) |
                Unsigned.unsignedByte(low));
    }

    /**
     * Writes a single byte to the specified address.
     *
     * @param address    Real-mode address to write to.
     * @param value      Data to write.
     * 
     * @throws MemoryException  on any error. 
     */
    public abstract void writeByte(RealModeAddress address, byte value)
        throws MemoryException;

    /**
     * Writes a single word to the specified address.
     *
     * @param address    Real-mode address to write to.
     * @param value      Data to write.
     * 
     * @throws MemoryException  on any error. 
     */	
    public void writeWord(RealModeAddress address, short value)
        throws MemoryException {

        byte low = (byte)value;
        byte high = (byte)(value >> 8);

        // write low byte
        writeByte(address, low);

        // write high byte
        RealModeAddress nextAddress = new RealModeAddress(
            address.getSegment(), (short)(address.getOffset() + 1));
        writeByte(nextAddress, high);		
    }

    /**
     * Reads a single byte from the specified address, in order to execute it.
     *
     * @param address    Real-mode address to read from.
     * @return the read byte.
     * 
     * @throws MemoryException  on any error. 
     */
    public abstract byte readExecuteByte(RealModeAddress address)
        throws MemoryException;

    /**
     * Reads a single word from the specified address, in order to execute it.
     *
     * @param address    Real-mode address to read from.
     * @return the read word.
     * 
     * @throws MemoryException  on any error. 
     */
    public short readExecuteWord(RealModeAddress address) throws MemoryException {
        // read low word
        byte low = readExecuteByte(address);

        // read high word
        RealModeAddress nextAddress = new RealModeAddress(
            address.getSegment(), (short)(address.getOffset() + 1));
        byte high = readExecuteByte(nextAddress);

        return (short)((Unsigned.unsignedByte(high) << 8) |
            Unsigned.unsignedByte(low));
    }	
}