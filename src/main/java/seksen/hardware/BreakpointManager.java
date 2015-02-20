/*
 * BreakpointManager.java
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

import java.util.ArrayList;
import java.util.Iterator;

import seksen.hardware.memory.MemoryAccessListener;
import seksen.hardware.memory.MemoryException;

/**
 * @author Erdem Guven
 */
public class BreakpointManager implements MemoryAccessListener {
	private final static int BP_EXEC = 0;
	private final static int BP_READ = 1;
	private final static int BP_WRITE = 2;
	private ArrayList bpLists[] = new ArrayList[3];

	public boolean toggleExecBP(Address address) {
		return toggleBP(BP_EXEC,address);
	}

	public boolean toggleReadBP(Address address) {
		return toggleBP(BP_READ,address);
	}

	public boolean toggleWriteBP(Address address) {
		return toggleBP(BP_WRITE,address);
	}

	private synchronized boolean toggleBP(int list, Address address) {
		if(bpLists[list] == null){
			bpLists[list] = new ArrayList();
		}
		if(bpLists[list].contains(address)){
			bpLists[list].remove(address);
			if(bpLists[list].size() == 0){
				bpLists[list] = null;
			}
			return false;
		} else {
			bpLists[list].add(address);
			return true;
		}
	}

	public boolean isExecBP(Address address) {
		return isBP(BP_EXEC,address);
	}

	public boolean isReadBP(Address address) {
		return isBP(BP_READ,address);
	}

	public boolean isWriteBP(Address address) {
		return isBP(BP_WRITE,address);
	}

	private synchronized boolean isBP(int list, Address address) {
		if(bpLists[list] == null){
			return false;
		}
		return bpLists[list].contains(address);
	}

	public Iterator getExecBPsIterator() {
		return getBPsIterator(BP_EXEC);
	}

	public Iterator getReadBPsIterator() {
		return getBPsIterator(BP_READ);
	}

	public Iterator getWriteBPsIterator() {
		return getBPsIterator(BP_WRITE);
	}

	private synchronized Iterator getBPsIterator(int list) {
		if(bpLists[list] == null){
			return new NullIterator();
		}
		return bpLists[list].iterator();
	}

	public void readExecuteMemory(Address address, int size) throws MemoryException {
		if (isBP(BP_EXEC,address)) {
			throw new MemoryException("Exec breakpoint at "+address);
		}
	}

	public void readMemory(Address address, int size) throws MemoryException {
		if (isBP(BP_READ,address)) {
			throw new MemoryException("Read breakpoint at "+address);
		}
	}

	public void writeMemory(Address address, int size) throws MemoryException {
		if (isBP(BP_WRITE,address)) {
			throw new MemoryException("Write breakpoint at "+address);
		}
	}
}

class NullIterator implements Iterator {
	public boolean hasNext() {
		return false;
	}

	public Object next() {
		return null;
	}

	public void remove() {
	}
}