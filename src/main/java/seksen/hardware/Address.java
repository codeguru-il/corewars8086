/*
 * Address.java
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

public abstract class Address implements Comparable {
	public static final int ADR20 = 0;

	public static Address newAddress(int type, int seg, int off){
		if(type == ADR20){
			return new Address20(seg,off);
		}
		return null;
	}

	/**
	 * Creats a new normilized address
	 * @param i	Linear address
	 * @return
	 */
	public abstract Address newAddress(int i);

	public abstract int getLinearAddress();

	/**
	 * @return segment.
	 */
	public abstract int getSegment();

	/**
	 * @return offset.
	 */
	public abstract int getOffset();

	public abstract Address addOffset(int off);

	public abstract Address addAddress(int i);

	public abstract Address newAddress(int seg, int off);

	public abstract int getMaxAddr();

	public abstract int getSegmentSize();

	public Address normalize() {
		return newAddress(getLinearAddress());
	}

	public boolean isValid() {
		return getLinearAddress() < getMaxAddr();
	}

	public boolean equals(Object obj) {
		return (obj instanceof Address) &&
			((Address)obj).getLinearAddress() == getLinearAddress();
	}

	public int compareTo(Object o){
		return getLinearAddress() - ((Address)o).getLinearAddress();
	}

	public abstract Address parseAddress(String address);

	public static Address parseAddress(int type,String str) {
		int index = str.indexOf(':');
		Address address = null;
		if( index == -1 ){
			int base = 10;
			if(str.startsWith("0x")){
				base = 16;
			}

			int addr = Integer.parseInt(str, base);
			address = newAddress(type,0,addr);
		} else {
			int seg = Integer.parseInt(str.substring(0, index),16);
			int off = Integer.parseInt(str.substring(index+1),16);
			address = newAddress(type,seg,off);
		}
		return address;
	}
}