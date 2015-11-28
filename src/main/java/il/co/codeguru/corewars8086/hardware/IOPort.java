package il.co.codeguru.corewars8086.hardware;

import il.co.codeguru.corewars8086.util.BufferInputStream;
import il.co.codeguru.corewars8086.util.BufferOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

public class IOPort implements Device {

	private BufferOutputStream input_buffer;
	private BufferInputStream output_buffer;

	public IOPort(int input_buffer_size, int output_buffer_size) {
		input_buffer = new BufferOutputStream(input_buffer_size);
		output_buffer = new BufferInputStream(output_buffer_size);
	}

	public void setMachine(Machine mac) {
	}

	public void reset() {
		input_buffer.resetBuffer();
		output_buffer.resetBuffer();
	}

	public byte in() {
		return (byte) input_buffer.read();
	}

	public void out(byte b) {
		output_buffer.write(b);
	}

	public OutputStream getOutputStream() {
		return input_buffer;
	}

	public InputStream getInputStream() {
		return output_buffer;
	}
}
