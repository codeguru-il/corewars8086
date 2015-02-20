/*
 * Symbol.java
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

public class Symbol implements Comparable{
	public final Address address;
	public final String symbol;

	public Symbol(Address address, String symbol) {
		super();
		this.address = address;
		this.symbol = symbol;
	}

	public String toString() {
		return symbol+' '+address;
	}

	public int compareTo(Object arg0) {
		return symbol.compareTo(((Symbol)arg0).symbol);
	}
}
