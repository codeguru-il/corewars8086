package memory;

import java.util.EventListener;

/**
 * Defines an interface for memory listeners
 * 
 * @author BS
 */
public interface MemoryEventListener extends EventListener {
    /**
     * Called when a byte is written to memory
     * @param address
     */
    void onMemoryWrite(RealModeAddress address);
}
