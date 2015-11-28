package il.co.codeguru.corewars8086.hardware.cpu;

import il.co.codeguru.corewars8086.hardware.AbstractAddress;
import il.co.codeguru.corewars8086.hardware.Device;
import il.co.codeguru.corewars8086.hardware.InterruptException;
import il.co.codeguru.corewars8086.hardware.memory.MemoryException;

import java.util.Vector;

public abstract class AbstractCpu implements Device {

	protected int instructionCounter = 0;

	protected Vector listeners;

	public void reset(){
		instructionCounter = 0;
	}

	/**
	 * Performs the next single opcode.
	 *
	 * @throws seksen.hardware.cpu.CpuException    on any CPU error.
	 * @throws seksen.hardware.memory.MemoryException on any Memory error.
	 */
	public abstract void nextOpcode() throws CpuException, MemoryException, InterruptException;

	public abstract void interrupt(int intnum)
        throws InterruptException, MemoryException, IntOpcodeException;

	public void addCallListener(CallListener listener){
		if(listeners == null){
			listeners = new Vector();
		}
		listeners.add(listener);
	}

	public void removeCallListener(CallListener listener){
		if(listeners != null){
			listeners.remove(listener);
		}
	}

	protected void notifyCallListeners(AbstractAddress from, AbstractAddress to) {
		if(listeners == null){
			return;
		}
		int size = listeners.size();
		for(int a=0; a<size; a++){
			((CallListener)listeners.elementAt(a)).callInst(from,to);
		}
	}

	public int getInstructionCounter(){
		return instructionCounter;
	}
}
