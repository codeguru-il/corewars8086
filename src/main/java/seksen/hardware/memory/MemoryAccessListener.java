/*
 * MemoryAccessListener.java
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
package seksen.hardware.memory;

import seksen.hardware.Address;

public interface MemoryAccessListener {
	void readMemory(Address address, int size) throws MemoryException;

	void writeMemory(Address address, int size) throws MemoryException;

	void readExecuteMemory(Address address, int size) throws MemoryException;
}
