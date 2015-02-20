/*
 * Address24.java
 *
 * Copyright (C) 2006 - 2008 Erdem Güven <zuencap@users.sourceforge.net>.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package seksen.hardware;

import seksen.util.Hex;
import seksen.util.Unsigned;

public class Address24 extends Address {

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
	protected Address24(int segment, int offset) {
		m_segment = segment & 0xffff;
		m_offset = offset & 0xffff;
	}

	/**
	 * Constructor from linear address.
	 *
	 * The 'segment' part will be the highest possible, e.g.:
	 * 12345h -> 1234:0005h
	 *
	 * @param linearAddress    32bit linear address.
	 */
	public Address24(int linearAddress) {
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

	public Address addOffset(int off){
		return new Address24(m_segment,m_offset+off);
	}

	public Address addAddress(int off){
		int adr = (getLinearAddress()+off);
		off = (off+m_offset)&0xffff;
		int seg;
		if(off<adr) {
			seg = (adr-off)/PARAGRAPH_SIZE;
		} else {
			seg = 0;
			off = adr;
		}
		return new Address24(seg,off);
	}

	/** Various real-mode memory constants. */
	public static final int NUM_PARAGRAPHS = 64 * 1024;
	public static final int PARAGRAPH_SIZE = 0x100;
	public static final int MEMORY_SIZE = NUM_PARAGRAPHS * PARAGRAPH_SIZE;

	public String toString() {
		return Hex.toHexString(getSegment(),4)+":"+Hex.toHexString(getOffset(),4);
	}

	public int getMaxAddr() {
		return MEMORY_SIZE;
	}

	public Address newAddress(int seg, int off) {
		return new Address24(seg,off);
	}

	public Address newAddress(int i) {
		return new Address24(i);
	}

	public int getSegmentSize() {
		return 0x10000;
	}

	public Address parseAddress(String address) {
		return parseAddress(ADR24, address);
	}
}
