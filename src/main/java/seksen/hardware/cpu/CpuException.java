/*
 * CpuException.java
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

package seksen.hardware.cpu;

/**
 * Base class for all Exceptions thrown by the Cpu class.
 *
 * @author DL
 * @author Erdem Guven
 */
public abstract class CpuException extends Exception {

	public CpuException() {
		super();
	}

	public CpuException(String string) {
		super(string);
	}

	public CpuException(Exception e) {
		super(e);
	}
}
