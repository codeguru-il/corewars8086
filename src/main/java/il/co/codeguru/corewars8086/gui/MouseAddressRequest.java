package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.hardware.memory.MemoryException;

import java.util.EventListener;

public interface MouseAddressRequest extends EventListener { 
	
  public void addressAtMouseLocationRequested(int address) throws MemoryException;
  
}
