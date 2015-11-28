package il.co.codeguru.corewars8086.hardware.memory;

/**
 * Base class for all Exceptions thrown by the RealModeMemory classes.
 * 
 * @author DL
 */
public class MemoryException extends Exception {

	public MemoryException() {
		super();
	}

	public MemoryException(String string) {
		super(string);
	}
}
