package il.co.codeguru.corewars8086.cpu;

/**
 * Base class for all Exceptions thrown by the Cpu class.
 * 
 * @author DL
 */
public abstract class CpuException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public CpuException(String message) {
		super(message);
	}
}
