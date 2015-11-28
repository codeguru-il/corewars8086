package il.co.codeguru.corewars8086.hardware;

/**
 * Wrapper class for a Real-Mode segment:offset address.
 * 
 * @author DL
 */
public abstract class AbstractAddress implements Comparable {
	public static final int ADR20 = 0;

	/**
	 * Creats a new normalized address
	 * @param i	Linear address
	 * @return
	 */
	public abstract AbstractAddress newAddress(int i);

	public abstract int getLinearAddress();

	/**
	 * @return segment.
	 */
	public abstract int getSegment();

	/**
	 * @return offset.
	 */
	public abstract int getOffset();

	public abstract AbstractAddress addOffset(int off);

	public abstract AbstractAddress addAddress(int i);

	public abstract AbstractAddress newAddress(int seg, int off);

	public abstract int getMaxAddr();

	public abstract int getSegmentSize();

	public AbstractAddress normalize() {
		return newAddress(getLinearAddress());
	}

	public boolean isValid() {
		return getLinearAddress() < getMaxAddr();
	}

	public boolean equals(Object obj) {
		return (obj instanceof AbstractAddress) &&
				((AbstractAddress)obj).getLinearAddress() == getLinearAddress();
	}

	public int compareTo(Object o){
		return getLinearAddress() - ((AbstractAddress)o).getLinearAddress();
	}

	public AbstractAddress parseAddress(String str) {
		int index = str.indexOf(':');
		AbstractAddress address = null;
		if( index == -1 ){
			int base = 10;
			if(str.startsWith("0x")){
				base = 16;
			}

			int addr = Integer.parseInt(str, base);
			address = new Address(0,addr);
		} else {
			int seg = Integer.parseInt(str.substring(0, index),16);
			int off = Integer.parseInt(str.substring(index+1),16);
			address = new Address(seg,off);
		}
		return address;
	}
}