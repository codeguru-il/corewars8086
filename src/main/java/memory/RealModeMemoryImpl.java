package memory;

import utils.EventMulticaster;

public class RealModeMemoryImpl extends AbstractRealModeMemory {

    private final byte[] m_data;
    private final EventMulticaster eventCaster;
    private final MemoryEventListener memoryEventListener;

    public RealModeMemoryImpl() {
        m_data = new byte[RealModeAddress.MEMORY_SIZE];
        eventCaster = new EventMulticaster(MemoryEventListener.class);
        memoryEventListener = (MemoryEventListener) eventCaster.getProxy();
    }

    @Override
    public byte readByte(RealModeAddress address) {
        return m_data[address.getLinearAddress()];		
    }

    @Override
    public void writeByte(RealModeAddress address, byte value) {
        m_data[address.getLinearAddress()] = value;
        // This will now notify ALL registered listeners
        memoryEventListener.onMemoryWrite(address);
    }

    @Override
    public byte readExecuteByte(RealModeAddress address) {
        return m_data[address.getLinearAddress()];		
    }	

    /**
     * The original listener setter, used by War.java
     */
    public void setListener(MemoryEventListener listener) {
        if (listener != null) {
            eventCaster.add(listener);
        }
    }

    /**
     * The new listener setter for external listeners like the GUI or ReplayRecorder.
     */
    public void setExternalListener(MemoryEventListener listener) {
        if (listener != null) {
            eventCaster.add(listener);
        }
    }
}