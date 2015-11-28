/*
 * OpcodeFetcher.java
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

import seksen.hardware.Address;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.RealModeMemory;

/**
 * Wraps opcode fetching from CS:IP.
 *
 * @author DL
 * @author Erdem Guven
 */
public class OpcodeFetcher {

	/**
	 * Constructor.
	 * @param state   Used to read & update CS:IP.
	 * @param memory  Used to actually read the fetched bytes.
	 */
	public OpcodeFetcher(CpuState state, RealModeMemory memory) {
		m_state = state;
		m_memory = memory;
	}

	/**
	 * @return the next byte pointed by CS:IP (and advances IP).
	 * @throws MemoryException  on any error.
	 */
	public byte nextByte() throws MemoryException {
		Address address = m_memory.newAddress(
				m_state.getCS(), m_state.getIP());
		m_state.setIP((short)(m_state.getIP() + 1));
		return m_memory.readExecuteByte(address);
	}

	/**
	 * @return the next word pointed by CS:IP (and advances IP).
	 * @throws MemoryException  on any error.
	 */
	public short nextWord() throws MemoryException {
		Address address = m_memory.newAddress(
				m_state.getCS(), m_state.getIP());
		m_state.setIP((short)(m_state.getIP() + 2));
		return m_memory.readExecuteWord(address);
	}

        public Address getAddress() {
            return m_memory.newAddress(m_state.getCS(), m_state.getIP());
        }

	/** Used to read & update CS:IP. */
	private final CpuState m_state;

	/** Used to actually read the fetched bytes. */
	private final RealModeMemory m_memory;
}
