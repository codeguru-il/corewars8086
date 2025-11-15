package memory;

/**
 * Implements the RealModeMemory interface using a buffer.
 *
 * @author DL
 */
public class RealModeMemoryImpl extends AbstractRealModeMemory {

    /** Listener to memory events */
    private MemoryEventListener listener;

    /** Actual memory data */
    private byte[] m_data;

    /**
     * Constructor.
     */
    public RealModeMemoryImpl() {
        m_data = new byte[RealModeAddress.MEMORY_SIZE];
    }

    /**
     * Reads a single byte from the specified address.
     *
     * @param address    Real-mode address to read from.
     * @return the read byte.
     * 
     * @throws MemoryException  on any error. 
     */
    public byte readByte(RealModeAddress address) {
        return m_data[address.getLinearAddress()];		
    }

    /**
     * Writes a single byte to the specified address.
     *
     * @param address    Real-mode address to write to.
     * @param value      Data to write.
     * 
     * @throws MemoryException  on any error. 
     */
    public void writeByte(RealModeAddress address, byte value) {
        m_data[address.getLinearAddress()] = value;
        if (listener != null) {
            listener.onMemoryWrite(address);
        }
    }

    /**
     * Reads a single byte from the specified address, in order to execute it.
     *
     * @param address    Real-mode address to read from.
     * @return the read byte.
     * 
     * @throws MemoryException  on any error. 
     */
    public byte readExecuteByte(RealModeAddress address) {
        return m_data[address.getLinearAddress()];		
    }	

    /**
     * @return Returns the listener.
     */
    public MemoryEventListener getListener() {
        return listener;
    }
    /**
     * @param listener The listener to set.
     */
    public void setListener(MemoryEventListener listener) {
        this.listener = listener;
    }
}
