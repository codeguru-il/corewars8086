package il.co.codeguru.corewars8086.hardware.memory;

import il.co.codeguru.corewars8086.hardware.AbstractAddress;

import java.util.EventListener;

public interface MemoryAccessListener extends EventListener {
	void readMemory(AbstractAddress address, int size) throws MemoryException;

	void writeMemory(AbstractAddress address, int size) throws MemoryException;

	void readExecuteMemory(AbstractAddress address, int size) throws MemoryException;
}
