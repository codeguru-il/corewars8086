/*
 * Unsigned.java
 *
 * Copyright (C) 2005 - 2006 Danny Leshem <dleshem@users.sourceforge.net>
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

package seksen.util;

/**
 * Unsigned numbers utilities.
 *
 * @author DL
 * @author Erdem Guven
 */
public class Unsigned {

	public static short unsignedByte(byte num) {
		return (short)((short)num & 0xFF);
	}

	public static int unsignedShort(short num) {
		return unsignedShort((int)num);
	}

	public static int unsignedShort(int num) {
		return (num & 0xFFFF);
	}

	/**
	 * Returns the unsigned representation of the number.
	 */
	public static long unsignedInt(int num) {
		return ((long)num & 0xFFFFFFFF);
	}

	/**
	 * Private constructor.
	 */
	private Unsigned() {}
}
