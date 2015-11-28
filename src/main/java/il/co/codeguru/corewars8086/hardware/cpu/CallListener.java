package il.co.codeguru.corewars8086.hardware.cpu;

import il.co.codeguru.corewars8086.hardware.AbstractAddress;

public interface CallListener {

	void callInst(AbstractAddress from, AbstractAddress to);

}
