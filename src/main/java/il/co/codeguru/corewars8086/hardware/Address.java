package il.co.codeguru.corewars8086.hardware;

import il.co.codeguru.corewars8086.util.Hex;
import il.co.codeguru.corewars8086.util.Unsigned;

/**
 * Wrapper class for a Real-Mode segment:offset address.
 *
 * @author DL
 * @author Erdem Guven
 */
public class Address extends AbstractAddress {

	/** 16bit Real-Mode segment. */
	protected final int m_segment;
	/** 16bit Real-Mode offset. */
	protected final int m_offset;

	/**
	 * Constructor from segment:offset.
	 *
	 * @param segment    16bit Real-mode segment.
	 * @param offset     16bit Real-mode offset.
	 */
	public Address(int segment, int offset) {
		m_segment = segment;
		m_offset = offset;
	}

	/**
	 * Constructor from linear address.
	 *
	 * The 'segment' part will be the highest possible, e.g.:
	 * 12345h -> 1234:0005h
	 *
	 * @param linearAddress    32bit linear address.
	 */
	public Address(int linearAddress) {
		linearAddress %= MEMORY_SIZE;

		int unsignedSegment = Unsigned.unsignedShort(linearAddress / PARAGRAPH_SIZE);
		int unsignedOffset = linearAddress % PARAGRAPH_SIZE;

		m_segment = unsignedSegment;
		m_offset = unsignedOffset;
	}

	/**
	 * @return 16bit Real-Mode segment.
	 */
	public int getSegment() {
		return m_segment;
	}

	/**
	 * @return 16bit Real-Mode offset.
	 */
	public int getOffset() {
		return m_offset;
	}

	/**
	 * @return 32bit linear address.
	 */
	public int getLinearAddress() {

		int unsignedSegment = Unsigned.unsignedShort(m_segment);
		int unsignedOffset = Unsigned.unsignedShort(m_offset);

		int linearAddress = unsignedSegment*PARAGRAPH_SIZE + unsignedOffset;
		return linearAddress % MEMORY_SIZE;
	}

	public AbstractAddress addOffset(int off){
		return new Address(m_segment,m_offset+off);
	}

	public AbstractAddress addAddress(int off){
		int adr = (getLinearAddress()+off);
		off = (off+m_offset)&0xffff;
		int seg;
		if(off<adr) {
			seg = (adr-off)>>4;
		} else {
			seg = 0;
			off = adr;
		}
		return new Address(seg,off);
	}

	/** Various real-mode memory constants. */
	public static final int NUM_PARAGRAPHS = 64 * 1024;
	public static final int PARAGRAPH_SIZE = 0x10;
	public static final int MEMORY_SIZE = NUM_PARAGRAPHS * PARAGRAPH_SIZE;

	public String toString() {
		return Hex.toHexString(getSegment(), 4)+":"+Hex.toHexString(getOffset(),4);
	}

	public int getMaxAddr() {
		return MEMORY_SIZE;
	}

	public AbstractAddress newAddress(int seg, int off) {
		return new Address(seg,off);
	}

	public AbstractAddress newAddress(int i) {
		return new Address(i);
	}

	public int getSegmentSize() {
		return 0x10000;
	}
}
