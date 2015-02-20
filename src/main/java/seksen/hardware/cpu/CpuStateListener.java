/*
 * CpuStateListener.java
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
package seksen.hardware.cpu;

public interface CpuStateListener {
	public static final int INDEX_AX = 0;
	public static final int INDEX_BX = 1;
	public static final int INDEX_CX = 2;
	public static final int INDEX_DX = 3;
	public static final int INDEX_DS = 4;
	public static final int INDEX_ES = 5;
	public static final int INDEX_SI = 6;
	public static final int INDEX_DI = 7;
	public static final int INDEX_SS = 8;
	public static final int INDEX_BP = 9;
	public static final int INDEX_SP = 10;
	public static final int INDEX_CS = 11;
	public static final int INDEX_IP = 12;
	public static final int INDEX_FLAGS = 13;

	void registerChanged(int regIndex);
}
