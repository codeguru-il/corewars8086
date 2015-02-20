package il.co.codeguru.corewars8086.hardware.memory;

/**
 * Memory region (start address, end address)
 * 
 * @author DL
 */
public class RealModeMemoryRegion {
	
    /**
     * Constructor.
     * 
     * @param start  Region's start address.
     * @param end    Region's end address.
     */
    public RealModeMemoryRegion(RealModeAddress start, RealModeAddress end) {
        m_start = start;
        m_end = end;		
    }

    /**
     * Returns whether or not a given address is within the region.
     * 
     * @param address  Address to check.
     * @return whether or not the given address is within the region.
     */
    public boolean isInRegion(RealModeAddress address) {
        final int start = m_start.getLinearAddress();
        final int end = m_end.getLinearAddress();
        final int asked = address.getLinearAddress();

        return ((asked >= start) && (asked <= end));		
    }

    /** Region's start address */
    private final RealModeAddress m_start;
    /** Region's end address */
    private final RealModeAddress m_end;
}