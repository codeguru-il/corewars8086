package il.co.codeguru.corewars8086.cpu;

import il.co.codeguru.corewars8086.utils.Unsigned;

/**
 * Wrapper class for CPU state (registers & flags). 
 * 
 * @author DL
 */
public class CpuState {

    /** Accessors for the 16bit registers */
    public short getAX() {
        return m_ax;
    }
    public short getBX() {
        return m_bx;
    }
    public short getCX() {
        return m_cx;
    }
    public short getDX() {
        return m_dx;
    }
    public short getDS() {
        return m_ds;
    }
    public short getES() {
        return m_es;
    }
    public short getSI() {
        return m_si;
    }
    public short getDI() {
        return m_di;
    }
    public short getSS() {
        return m_ss;
    }
    public short getBP() {
        return m_bp;
    }
    public short getSP() {
        return m_sp;
    }
    public short getCS() {
        return m_cs;
    }
    public short getIP() {
        return m_ip;
    }
    public short getFlags() {
        return m_flags;
    }	

    public void setAX(short value) {
        m_ax = value;
    }
    public void setBX(short value) {
        m_bx = value;
    }
    public void setCX(short value) {
        m_cx = value;
    }
    public void setDX(short value) {
        m_dx = value;
    }
    public void setDS(short value) {
        m_ds = value;
    }
    public void setES(short value) {
        m_es = value;
    }
    public void setSI(short value) {
        m_si = value;
    }
    public void setDI(short value) {
        m_di = value;
    }
    public void setSS(short value) {
        m_ss = value;
    }
    public void setBP(short value) {
        m_bp = value;
    }
    public void setSP(short value) {
        m_sp = value;
    }
    public void setCS(short value) {
        m_cs = value;
    }
    public void setIP(short value) {
        m_ip = value;
    }
    public void setFlags(short value) {
        m_flags = value;
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

    /** Accessors for the virtual Energy register. */
    public short getEnergy() {
        return m_energy;
    }
    public void setEnergy(short value) {
        m_energy = value;
    }

    /** Accessors for the virtual bomb count registers. */
    public byte getBomb1Count() {
        return m_bomb1count;
    }
    public void setBomb1Count(byte value) {
        m_bomb1count = value;
    }
    public byte getBomb2Count() {
        return m_bomb2count;
    }
    public byte getBomb3Count() {
        return m_bomb3count;
    }
    public void setBomb2Count(byte value) {
        m_bomb2count = value;
    }
    public void setBomb3Count(byte value) {
        m_bomb3count = value;
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
    public void setCarryFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_CARRY;
        } else {
            m_flags &= (~FLAGS_MASK_CARRY);
        }
    }
    public void setParityFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_PARITY;
        } else {
            m_flags &= (~FLAGS_MASK_PARITY);
        }
    }
    public void setAuxFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_AUX;
        } else {
            m_flags &= (~FLAGS_MASK_AUX);
        }
    }
    public void setZeroFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_ZERO;
        } else {
            m_flags &= (~FLAGS_MASK_ZERO);
        }
    }
    public void setSignFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_SIGN;
        } else {
            m_flags &= (~FLAGS_MASK_SIGN);
        }
    }
    public void setTrapFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_TRAP;
        } else {
            m_flags &= (~FLAGS_MASK_TRAP);
        }
    }
    public void setInterruptFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_INTERRUPT;
        } else {
            m_flags &= (~FLAGS_MASK_INTERRUPT);
        }
    }
    public void setDirectionFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_DIRECTION;
        } else {
            m_flags &= (~FLAGS_MASK_DIRECTION);
        }
    }
    public void setOverflowFlag(boolean newValue) {
        if (newValue) {
            m_flags |= FLAGS_MASK_OVERFLOW;
        } else {
            m_flags &= (~FLAGS_MASK_OVERFLOW);
        }
    }

    /** CPU registers */
    private short m_ax;
    private short m_bx;
    private short m_cx;
    private short m_dx;

    private short m_ds;
    private short m_es;
    private short m_si;
    private short m_di;

    private short m_ss;
    private short m_bp;
    private short m_sp;

    private short m_cs;
    private short m_ip;
    private short m_flags;

    /** The virtual Energy register (used to calculate the warrior's speed). */
    private short m_energy;

    /** The virtual bomb count registers (used for INT 0x86, INT 0x87 and INT 0x88 opcodes ). */
    private byte m_bomb1count;
    private byte m_bomb2count;
    private byte m_bomb3count;

    /**
     * Masks for the various 'flags' fields.
     */
    private static final short FLAGS_MASK_CARRY = 0x0001;
    private static final short FLAGS_MASK_PARITY = 0x0004;
    private static final short FLAGS_MASK_AUX = 0x0010;
    private static final short FLAGS_MASK_ZERO = 0x0040;
    private static final short FLAGS_MASK_SIGN = 0x0080;
    private static final short FLAGS_MASK_TRAP = 0x0100;
    private static final short FLAGS_MASK_INTERRUPT = 0x0200;
    private static final short FLAGS_MASK_DIRECTION = 0x0400;
    private static final short FLAGS_MASK_OVERFLOW = 0x0800;	
}