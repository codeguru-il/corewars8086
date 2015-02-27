package il.co.codeguru.corewars8086.cpu;

/**
 * Thrown by DIV/IDIV opcodes when the division overflows, or when dividing by zero.
 * 
 * @author DL
 */
public class DivisionException extends CpuException {
	private static final long serialVersionUID = 1L;
	
	public DivisionException(String message)
	{
		super(message);
	}
}
