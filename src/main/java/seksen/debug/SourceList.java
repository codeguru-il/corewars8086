/*
 * SourceList.java
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

public class SourceList {
	Source sources[];

	public SourceList() {
		sources = new Source[0];
	}


	public SourceLine getLine( Address address ) {
		int line = -1;
		for( int a=0; a<sources.length; a++) {
			line = sources[a].getLine(address);
			if( line >= 0 ) {
				System.out.println("Source: "+sources[a].path+
						":"+line);
				return new SourceLine(sources[a].path, line);
			}
		}
		return null;
	}

	public void add(Source[] source_array) {
		Source[] nsources = new Source[sources.length + source_array.length];
		System.arraycopy(sources, 0, nsources, 0, sources.length);
		System.arraycopy(source_array, 0, nsources, sources.length,
				source_array.length);
		sources = nsources;
	}
}
