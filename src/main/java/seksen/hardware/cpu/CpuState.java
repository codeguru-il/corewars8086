/*
 * CpuState.java
 *
 * Copyright (C) 2005 - 2006 Danny Leshem <dleshem@users.sourceforge.net>
 * Copyright (C) 2006 - 2008 Erdem Güven <zuencap@users.sourceforge.net>.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package seksen.hardware.cpu;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import seksen.hardware.Address;
import seksen.hardware.Device;
import seksen.hardware.Machine;
import seksen.hardware.Storable;
import seksen.util.Hex;
import seksen.util.Unsigned;

/**
 * Wrapper class for CPU state (registers & flags).
 *
 * @author DL
 * @author Erdem Guven
 */
public class CpuState implements Storable, Device {

	public void setMachine(Machine mac) {
	}

    public void reset() {
	this.m_ax = 0;
	this.m_bx = 0;
	this.m_cx = 0;
	this.m_dx = 0;

	this.m_ds = 0;
	this.m_es = 0;
	this.m_si = 0;
	this.m_di = 0;

	this.m_ss = 0;
	this.m_bp = 0;
	this.m_sp = 0;

	this.m_cs = 0;
	this.m_ip = 0;

	this.m_flags = 0;
	this.m_defined_flags = 0;
    }

	/** Accessors for the 16bit registers */
	public int getAX() {
		return m_ax;
	}
	public int getBX() {
		return m_bx;
	}
	public int getCX() {
		return m_cx;
	}
	public int getDX() {
		return m_dx;
	}
	public int getDS() {
		return m_ds;
	}
	public int getES() {
		return m_es;
	}
	public int getSI() {
		return m_si;
	}
	public int getDI() {
		return m_di;
	}
	public int getSS() {
		return m_ss;
	}
	public int getBP() {
		return m_bp;
	}
	public int getSP() {
		return m_sp;
	}
	public int getCS() {
		return m_cs;
	}
	public int getIP() {
		return m_ip;
	}
	public int getFlags() {
		return m_flags;
	}
	public Address getCSIP(Address address) {
		return address.newAddress(m_cs, m_ip);
	}
	public Address getSSSP(Address address) {
		return address.newAddress(m_ss, m_sp);
	}

	public void setAX(int value) {
		m_ax = value & 0xffff;
	}
	public void setBX(int value) {
		m_bx = value & 0xffff;
	}
	public void setCX(int value) {
		m_cx = value & 0xffff;
	}
	public void setDX(int value) {
		m_dx = value & 0xffff;
	}
	public void setDS(int value) {
		m_ds = value & 0xffff;
	}
	public void setES(int value) {
		m_es = value & 0xffff;
	}
	public void setSI(int value) {
		m_si = value & 0xffff;
	}
	public void setDI(int value) {
		m_di = value & 0xffff;
	}
	public void setSS(int value) {
		m_ss = value & 0xffff;
	}
	public void setBP(int value) {
		m_bp = value & 0xffff;
	}
	public void setSP(int value) {
		m_sp = value & 0xffff;
	}
	public void setCS(int value) {
		m_cs = value & 0xffff;
	}
	public void setIP(int value) {
		m_ip = value & 0xffff;
	}
	public void setFlags(int value) {
		m_flags = value & 0xffff;
	}
	public void setUndefinedFlags(int value) {
		m_defined_flags &= value ^ 0xffff; //for CpuVMCompare
	}
	public void setCSIP(Address address) {
		setCS(address.getSegment());
		setIP(address.getOffset());
	}
	public void setSSSP(Address address) {
		setSS(address.getSegment());
		setSP(address.getOffset());
	}

	/** Accessors for the 8bit registers */
	public byte getAL() {
		return (byte)m_ax;
	}
	public byte getBL() {
		return (byte)m_bx;
	}
	public byte getCL() {
		return (byte)m_cx;
	}
	public byte getDL() {
		return (byte)m_dx;
	}
	public byte getAH() {
		return (byte)(m_ax >> 8);
	}
	public byte getBH() {
		return (byte)(m_bx >> 8);
	}
	public byte getCH() {
		return (byte)(m_cx >> 8);
	}
	public byte getDH() {
		return (byte)(m_dx >> 8);
	}

	public void setAL(byte value) {
		m_ax &= 0xFF00;
		m_ax |= Unsigned.unsignedByte(value);
	}
	public void setBL(byte value) {
		m_bx &= 0xFF00;
		m_bx |= Unsigned.unsignedByte(value);
	}
	public void setCL(byte value) {
		m_cx &= 0xFF00;
		m_cx |= Unsigned.unsignedByte(value);
	}
	public void setDL(byte value) {
		m_dx &= 0xFF00;
		m_dx |= Unsigned.unsignedByte(value);
	}
	public void setAH(byte value) {
		m_ax &= 0x00FF;
		m_ax |= (Unsigned.unsignedByte(value) << 8);
	}
	public void setBH(byte value) {
		m_bx &= 0x00FF;
		m_bx |= (Unsigned.unsignedByte(value) << 8);
	}
	public void setCH(byte value) {
		m_cx &= 0x00FF;
		m_cx |= (Unsigned.unsignedByte(value) << 8);
	}
	public void setDH(byte value) {
		m_dx &= 0x00FF;
		m_dx |= (Unsigned.unsignedByte(value) << 8);
	}

	/**
	 * 'get' accessor methods for the various fields of the flags register.
	 * @return whether or not the requested flags field is set.
	 */
	public boolean getCarryFlag() {
		return ((m_flags & FLAGS_MASK_CARRY) == FLAGS_MASK_CARRY);
	}
	public boolean getParityFlag() {
		return ((m_flags & FLAGS_MASK_PARITY) == FLAGS_MASK_PARITY);
	}
	public boolean getAuxFlag() {
		return ((m_flags & FLAGS_MASK_AUX) == FLAGS_MASK_AUX);
	}
	public boolean getZeroFlag() {
		return ((m_flags & FLAGS_MASK_ZERO) == FLAGS_MASK_ZERO);
	}
	public boolean getSignFlag() {
		return ((m_flags & FLAGS_MASK_SIGN) == FLAGS_MASK_SIGN);
	}
	public boolean getTrapFlag() {
		return ((m_flags & FLAGS_MASK_TRAP) == FLAGS_MASK_TRAP);
	}
	public boolean getInterruptFlag() {
		return ((m_flags & FLAGS_MASK_INTERRUPT) == FLAGS_MASK_INTERRUPT);
	}
	public boolean getDirectionFlag() {
		return ((m_flags & FLAGS_MASK_DIRECTION) == FLAGS_MASK_DIRECTION);
	}
	public boolean getOverflowFlag() {
		return ((m_flags & FLAGS_MASK_OVERFLOW) == FLAGS_MASK_OVERFLOW);
	}

	/**
	 * 'set' accessor methods for the various fields of the flags register.
	 * @param newValue whether or not the requested flags field should be set.
	 */
	public void setFlag(int flag,boolean newValue){
		if (newValue) {
			m_flags |= flag;
		} else {
			m_flags &= (~flag);
		}
		m_defined_flags |= flag;
	}
	public void setCarryFlag(boolean newValue) {
		setFlag(FLAGS_MASK_CARRY,newValue);
	}
	public void setParityFlag(boolean newValue) {
		setFlag(FLAGS_MASK_PARITY,newValue);
	}
	public void setAuxFlag(boolean newValue) {
		setFlag(FLAGS_MASK_AUX,newValue);
	}
	public void setZeroFlag(boolean newValue) {
		setFlag(FLAGS_MASK_ZERO,newValue);
	}
	public void setSignFlag(boolean newValue) {
		setFlag(FLAGS_MASK_SIGN,newValue);
	}
	public void setTrapFlag(boolean newValue) {
		setFlag(FLAGS_MASK_TRAP,newValue);
	}
	public void setInterruptFlag(boolean newValue) {
		setFlag(FLAGS_MASK_INTERRUPT,newValue);
	}
	public void setDirectionFlag(boolean newValue) {
		setFlag(FLAGS_MASK_DIRECTION,newValue);
	}
	public void setOverflowFlag(boolean newValue) {
		setFlag(FLAGS_MASK_OVERFLOW,newValue);
	}

	public void set(CpuState prevState) {
		this.m_ax = prevState.m_ax;
		this.m_bx = prevState.m_bx;
		this.m_cx = prevState.m_cx;
		this.m_dx = prevState.m_dx;

		this.m_ds = prevState.m_ds;
		this.m_es = prevState.m_es;
		this.m_si = prevState.m_si;
		this.m_di = prevState.m_di;

		this.m_ss = prevState.m_ss;
		this.m_bp = prevState.m_bp;
		this.m_sp = prevState.m_sp;

		this.m_cs = prevState.m_cs;
		this.m_ip = prevState.m_ip;

		this.m_flags = prevState.m_flags;
		this.m_defined_flags = prevState.m_defined_flags;
	}

	public Object clone() {
		CpuState state = new CpuState();
		state.m_ax = m_ax;
		state.m_bx = m_bx;
		state.m_cx = m_cx;
		state.m_dx = m_dx;

		state.m_ds = m_ds;
		state.m_es = m_es;
		state.m_si = m_si;
		state.m_di = m_di;

		state.m_ss = m_ss;
		state.m_bp = m_bp;
		state.m_sp = m_sp;

		state.m_cs = m_cs;
		state.m_ip = m_ip;

		state.m_flags = m_flags;
		state.m_defined_flags = m_defined_flags;

		return state;
	}

	public String toString() {
		return
			"CpuState[AX:"+Hex.toHexString(m_ax, 4)+
			",BX:"+Hex.toHexString(m_bx, 4)+
			",CX:"+Hex.toHexString(m_cx, 4)+
			",DX:"+Hex.toHexString(m_dx, 4)+

			",DS:"+Hex.toHexString(m_ds, 4)+
			",ES:"+Hex.toHexString(m_es, 4)+
			",SI:"+Hex.toHexString(m_si, 4)+
			",DI:"+Hex.toHexString(m_di, 4)+

			",SS:"+Hex.toHexString(m_ss, 4)+
			",BP:"+Hex.toHexString(m_bp, 4)+
			",SP:"+Hex.toHexString(m_sp, 4)+

			",CS:"+Hex.toHexString(m_cs, 4)+
			",IP:"+Hex.toHexString(m_ip, 4)+
			",Flags:"+Hex.toHexString(m_flags, 4)+"]";

	}

	public boolean equals(Object obj) {
		if( !(obj instanceof CpuState )){
			return false;
		}
		CpuState state = (CpuState)obj;

		return
		state.m_ax == m_ax &&
		state.m_bx == m_bx &&
		state.m_cx == m_cx &&
		state.m_dx == m_dx &&

		state.m_ds == m_ds &&
		state.m_es == m_es &&
		state.m_si == m_si &&
		state.m_di == m_di &&

		state.m_ss == m_ss &&
		state.m_bp == m_bp &&
		state.m_sp == m_sp &&

		state.m_cs == m_cs &&
		state.m_ip == m_ip &&
		(state.m_flags&m_defined_flags) == (m_flags&m_defined_flags);
	}

	public void save(OutputStream output) throws IOException {
		DataOutputStream objout = new DataOutputStream(output);
		objout.writeShort(m_ax);
		objout.writeShort(m_bx);
		objout.writeShort(m_cx);
		objout.writeShort(m_dx);

		objout.writeShort(m_ds);
		objout.writeShort(m_es);
		objout.writeShort(m_si);
		objout.writeShort(m_di);

		objout.writeShort(m_ss);
		objout.writeShort(m_bp);
		objout.writeShort(m_sp);

		objout.writeShort(m_cs);
		objout.writeShort(m_ip);

		objout.writeShort(m_flags);
		objout.writeShort(m_defined_flags);
		objout.flush();
	}

	public void load(InputStream input) throws IOException {
		DataInputStream objin = new DataInputStream(input);
		m_ax = objin.readShort();
		m_bx = objin.readShort();
		m_cx = objin.readShort();
		m_dx = objin.readShort();

		m_ds = objin.readShort();
		m_es = objin.readShort();
		m_si = objin.readShort();
		m_di = objin.readShort();

		m_ss = objin.readShort();
		m_bp = objin.readShort();
		m_sp = objin.readShort();

		m_cs = objin.readShort();
		m_ip = objin.readShort();

		m_flags = objin.readShort();
		m_defined_flags = objin.readShort();
	}

	/** CPU registers */
	private int m_ax;
	private int m_bx;
	private int m_cx;
	private int m_dx;

	private int m_ds;
	private int m_es;
	private int m_si;
	private int m_di;

	private int m_ss;
	private int m_bp;
	private int m_sp;

	private int m_cs;
	private int m_ip;

	private int m_flags;
	private int m_defined_flags;

	/**
	 * Masks for the various 'flags' fields.
	 * Carry Flag (CF) - this flag is set to 1 when there is an unsigned overflow.
	 * 		For example when you add bytes 255 + 1 (result is not in range 0...255).
	 * 		When there is no overflow this flag is set to 0.
	 * Zero Flag (ZF) - set to 1 when result is zero. For none zero result this flag is set to 0.
	 * Sign Flag (SF) - set to 1 when result is negative. When result is positive it is set to 0.
	 * 		Actually this flag take the value of the most significant bit.
	 * Overflow Flag (OF) - set to 1 when there is a signed overflow. For example,
	 * 		when you add bytes 100 + 50 (result is not in range -128...127).
	 * Parity Flag (PF) - this flag is set to 1 when there is even number of one
	 * 		bits in result, and to 0 when there is odd number of one bits.
	 * 		Even if result is a word only 8 low bits are analyzed!
	 * Auxiliary Flag (AF) - set to 1 when there is an unsigned overflow for low nibble (4 bits).
	 * Interrupt enable Flag (IF) - when this flag is set to 1 CPU reacts to interrupts from external devices.
	 * Direction Flag (DF) - this flag is used by some instructions to process
	 *  	data chains, when this flag is set to 0 - the processing is done forward,
	 *  	when this flag is set to 1 the processing is done backward.
	 */
	public static final int FLAGS_MASK_CARRY = 0x0001;
	public static final int FLAGS_MASK_PARITY = 0x0004;
	public static final int FLAGS_MASK_AUX = 0x0010;
	public static final int FLAGS_MASK_ZERO = 0x0040;
	public static final int FLAGS_MASK_SIGN = 0x0080;
	public static final int FLAGS_MASK_TRAP = 0x0100;
	public static final int FLAGS_MASK_INTERRUPT = 0x0200;
	public static final int FLAGS_MASK_DIRECTION = 0x0400;
	public static final int FLAGS_MASK_OVERFLOW = 0x0800;
}
