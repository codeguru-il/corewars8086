/*
 * Source.java
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
package seksen.debug;

import seksen.hardware.Address;


public class Source {
	public final String path;
	public final int segment;
	public final short[] offsets;
	public final int[] lines;

	public Source(final String path, final int segment, final short[] offsets, final int[] lines) {
		super();
		this.path = path;
		this.segment = segment;
		this.offsets = offsets;
		this.lines = lines;
	}

	public int getLine( Address address ) {
		if( offsets.length <= 0 ) {
			return -1;
		}

		Address addr = address.newAddress(segment, 0);
		int offset = address.getLinearAddress() - addr.getLinearAddress();

		if( offset < (offsets[0]&0xffff) ||
				offset > (offsets[offsets.length-1]&0xffff) )
		{
			return -1;
		}

		int s = 0;
		int e = offsets.length;

		while(s<e) {
			int i = (e+s) / 2;
			int off = (offsets[i]&0xffff);

			if( offset > off) {
				s = i+1;
			} else if(offset < off) {
				e = i;
			} else {
				return lines[i];
			}
		}

		return -1;
	}
}
