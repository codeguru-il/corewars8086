package il.co.codeguru.corewars8086.hardware.memory;

import il.co.codeguru.corewars8086.utils.Unsigned;

/**
 * Wrapper class for a Real-Mode segment:offset address.
 * 
 * @author DL
 */
public class RealModeAddress {

    /**
     * Constructor from segment:offset.
     * 
     * @param segment    16bit Real-mode segment.
     * @param offset     16bit Real-mode offset.
     */
    public RealModeAddress(short segment, short offset) {
        m_segment = segment;
        m_offset = offset;

		int unsignedSegment = Unsigned.unsignedShort(m_segment);
		int unsignedOffset = Unsigned.unsignedShort(m_offset);

		int linearAddressFull = unsignedSegment * PARAGRAPH_SIZE + unsignedOffset;
		m_linearAddress = linearAddressFull % MEMORY_SIZE;
    }

    /**
     * Constructor from linear address.
     * 
     * The 'segment' part will be the highest possible, e.g.:
     * 12345h -> 1234:0005h
     *  
     * @param linearAddress    32bit linear address.
     */
    public RealModeAddress(int linearAddress) {
        linearAddress %= MEMORY_SIZE;

        int unsignedSegment = Unsigned.unsignedShort(linearAddress / PARAGRAPH_SIZE);
        int unsignedOffset = Unsigned.unsignedShort(
            (linearAddress - (unsignedSegment*PARAGRAPH_SIZE)));

        m_segment = (short)unsignedSegment;
        m_offset = (short)unsignedOffset;

		m_linearAddress = linearAddress;
    }	

    /**
     * @return 16bit Real-Mode segment.
     */
    public short getSegment() {
        return m_segment;
    }

    /**
     * @return 16bit Real-Mode offset.
     */
    public short getOffset() {
        return m_offset;
    }

    /**
     * @return 32bit linear address.
     */
    public int getLinearAddress() {
		return m_linearAddress;
    }

    /** Various real-mode memory constants. */	
    public static final int NUM_PARAGRAPHS = 64 * 1024;	
    public static final int PARAGRAPH_SIZE = 0x10;	
    public static final int PARAGRAPHS_IN_SEGMENT = 0x1000;	
    public static final int MEMORY_SIZE = NUM_PARAGRAPHS * PARAGRAPH_SIZE;

    /** 16bit Real-Mode segment. */	
    private final short m_segment;

    /** 16bit Real-Mode offset. */	
    private final short m_offset;

	/** cached linear representation of segment and offset */
	private int m_linearAddress;
}