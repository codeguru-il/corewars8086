package il.co.codeguru.corewars8086.gui;

import java.util.EventListener;

public interface MouseAddressRequest extends EventListener { 
	
  public void addressAtMouseLocationRequested(int address);
  
}
