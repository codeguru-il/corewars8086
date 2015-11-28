/*
 * SymbolList.java
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

public class SymbolList {
	private Symbol[] symbols;

	public SymbolList() {
		symbols = new Symbol[0];
	}

	public SymbolList(Symbol[] symbols) {
		this.symbols = symbols;
	}

	public Symbol getSymbol(Address address){
		int adr = address.getLinearAddress();
		int index;
		for(index=0;index<symbols.length &&
			adr>=symbols[index].address.getLinearAddress();index++);

		if(index==0 ||
				(adr-symbols[index-1].address.getLinearAddress())>0x10000){
			return null;
		}

		return symbols[index-1];
	}


	public Symbol getSymbolAt(Address address){
		int adr = address.getLinearAddress();
		int index;
		for(index=0;index<symbols.length;index++) {
			int adr2 = symbols[index].address.getLinearAddress();
			if( adr == adr2 ) {
				return symbols[index];
			}
			if( adr < adr2 ) {
				break;
			}
		}

		return null;
	}


	public Symbol getSymbol(String symbol){
		int index;
		for(index=0; index<symbols.length &&
			!symbols[index].symbol.equals(symbol);index++);

		if(index==0){
			return null;
		}

		return symbols[index-1];
	}

	public Symbol getSymbol(int index) {
		return symbols[index];
	}

	public int length() {
		return symbols.length;
	}
}
