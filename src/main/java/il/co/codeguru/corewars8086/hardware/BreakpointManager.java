package il.co.codeguru.corewars8086.hardware;

import il.co.codeguru.corewars8086.hardware.memory.MemoryAccessListener;
import il.co.codeguru.corewars8086.hardware.memory.MemoryException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Erdem Guven
 */
public class BreakpointManager implements MemoryAccessListener {
	private final static int BP_EXEC = 0;
	private final static int BP_READ = 1;
	private final static int BP_WRITE = 2;
	private ArrayList bpLists[] = new ArrayList[3];

	public boolean toggleExecBP(AbstractAddress address) {
		return toggleBP(BP_EXEC,address);
	}

	public boolean toggleReadBP(AbstractAddress address) {
		return toggleBP(BP_READ,address);
	}

	public boolean toggleWriteBP(AbstractAddress address) {
		return toggleBP(BP_WRITE,address);
	}

	private synchronized boolean toggleBP(int list, AbstractAddress address) {
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

	public boolean isExecBP(AbstractAddress address) {
		return isBP(BP_EXEC,address);
	}

	public boolean isReadBP(AbstractAddress address) {
		return isBP(BP_READ,address);
	}

	public boolean isWriteBP(AbstractAddress address) {
		return isBP(BP_WRITE,address);
	}

	private synchronized boolean isBP(int list, AbstractAddress address) {
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

	public void readExecuteMemory(AbstractAddress address, int size) throws MemoryException {
		if (isBP(BP_EXEC,address)) {
			throw new MemoryException("Exec breakpoint at "+address);
		}
	}

	public void readMemory(AbstractAddress address, int size) throws MemoryException {
		if (isBP(BP_READ,address)) {
			throw new MemoryException("Read breakpoint at "+address);
		}
	}

	public void writeMemory(AbstractAddress address, int size) throws MemoryException {
		if (isBP(BP_WRITE,address)) {
			throw new MemoryException("Write breakpoint at "+address);
		}
	}
}