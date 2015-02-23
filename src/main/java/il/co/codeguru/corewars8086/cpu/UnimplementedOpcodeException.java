package il.co.codeguru.corewars8086.cpu;

/**
 * Thrown when attempting to execute an unimplemented opcode.
 * 
 * @author DL
 */
public class UnimplementedOpcodeException extends CpuException {
	private static final long serialVersionUID = 1L;
	
	public UnimplementedOpcodeException(String opcode)
	{
		super(opcode + " is not implemented yet");
	}
}
