package il.co.codeguru.corewars8086.hardware;

import java.util.Iterator;

class NullIterator implements Iterator {
	public boolean hasNext() {
		return false;
	}

	public Object next() {
		return null;
	}

	public void remove() {
	}
}