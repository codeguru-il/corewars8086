package il.co.codeguru.corewars8086.util;

public interface IntelHexParseListener {
	void startSegment(int segment, int offset);
	void data(int address, byte[] data);
}