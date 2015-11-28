/*
 * MemoryException.java
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

package seksen.hardware.memory;

/**
 * Base class for all Exceptions thrown by the RealModeMemory classes.
 *
 * @author DL
 * @author Erdem Guven
 */
public class MemoryException extends Exception {

	public MemoryException() {
		super();
	}

	public MemoryException(String string) {
		super(string);
	}
}
