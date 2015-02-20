package il.co.codeguru.corewars8086.util;

import java.io.OutputStream;

public class BufferOutputStream extends OutputStream {

	private byte[] buffer;
	private int index;
	private int size;

	public BufferOutputStream() {
		this(16);
	}

	public BufferOutputStream(int buffer_size) {
		buffer = new byte[buffer_size];
		index = 0;
		size = 0;
	}

	public void resetBuffer() {
		synchronized (buffer) {
			index = 0;
			size = 0;
		}
	}

	public void write(int b) {
		synchronized (buffer) {
			buffer[index] = (byte) b;
			index = (index + 1) % buffer.length;
			if (size < buffer.length) {
				size++;
			}
		}
	}

	public int read() {
		synchronized (buffer) {
			if (size > 0) {
				int i = (index + buffer.length - size) % buffer.length;
				return buffer[i];
			} else {
				return 0;
			}
		}
	}
}