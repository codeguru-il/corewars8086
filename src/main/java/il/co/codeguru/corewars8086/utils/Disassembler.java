package il.co.codeguru.corewars8086.utils;

import il.co.codeguru.corewars8086.memory.RealModeAddress;

import java.util.ArrayList;

/**
 * A fast disassembler, similar to Cpu.java
 * @author Yovaayova
 */
public class Disassembler
{
	/**
	 * the bytes to disassemble.
	 */
	private static byte[] bytes;
	/**
	 * the pointer to the next byte to disassemble.
	 */
	private static int pointer;
	
	/**
	 * the current mode or register index or memory index of the indirect address function
	 */
	private static byte mode, regIndex, memIndex;
	
	/**
	 * disassemble the bytes
	 * @param bytes to disassemble
	 * @return string array of the disassembled opcodes
	 */
	public static String[] disassemble(byte[] bytes, RealModeAddress address)
	{
		Disassembler.bytes = bytes;
		pointer = 0;
		
		ArrayList<String> res = new ArrayList<String>();
		while (hasNextByte())
		{
			int lastPointer = pointer;
			try
			{
				String opcode = nextOpcode();
				String assembledBytes = "0x";
				for (int i = lastPointer; i < pointer; i++)
					assembledBytes += Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
				res.add(toString(address.getSegment(), (short)((address.getOffset() + lastPointer) & 0xFFFF)) + "\t"
					+ assembledBytes + "\t" + opcode);
			}
			catch (DisassemblerException e)
			{
				for (int i = lastPointer; i < pointer; i++)
					res.add(toString(address.getSegment(), (short)((address.getOffset() + lastPointer) & 0xFFFF)) + "\t"
							+ toString(bytes[i]) + "\t DB " + toString(bytes[i]));
			}
		}
		
		String[] results = new String[res.size()];
		return res.toArray(results);
	}
	
	/**
	 * 
	 * @return if you have more bytes to disassemble
	 */
	private static boolean hasNextByte()
	{
		return pointer < bytes.length;
	}
	
	private static byte getByte() throws DisassemblerException
	{
		if (!hasNextByte())
			throw new DisassemblerException();
		
		return bytes[pointer];
	}
	
	/**
	 * similar to OpcodeFetcher#nextByte()
	 * @return the next byte to disassemble
	 * @throws DisassemblerException 
	 */
	private static byte nextByte() throws DisassemblerException
	{
		if (!hasNextByte())
			throw new DisassemblerException();
		
		pointer++;
		
		return bytes[pointer -1];
	}
	
	/**
	 * similar to OpcodeFetcher#nextWord()
	 * @return the next word to disassemble
	 * @throws DisassemblerException 
	 */
	public static short nextWord() throws DisassemblerException
	{
		byte low = nextByte();
		byte high = nextByte();
		
		return (short)((Unsigned.unsignedByte(high) << 8) |
                Unsigned.unsignedByte(low));
	}
	
	/**
	 * calculates the mode, register index and the memory index of the next byte, assuming it is an indirect address function.
	 * similar to IndirectAddressingDecoder#reset() 
	 * @throws DisassemblerException 
	 */
	private static void resetIndirect() throws DisassemblerException //
	{
        // read the 'mode' byte (MM RRR III)
        // M - indirect addressing mode mux
        // R - register indexing
        // I - indirect addressing indexing 
        byte modeByte = nextByte();

        mode = (byte)((modeByte >> 6) & 0x03);
        regIndex = (byte)((modeByte >> 3) & 0x07);		
        memIndex = (byte)(modeByte & 0x07);
    }
	
	private static byte getIndirect()
	{
		return (byte) (memIndex | (regIndex << 3) | (mode << 6));
	}
	
	/**
	 * similar to IndirectAddressingDecoder#getMem8()
	 * @return the indirect 8 bit memory description
	 * @throws DisassemblerException 
	 */
	private static String getMem8() throws DisassemblerException
	{
		String extraInfo = null;
		// decode the opcode according to the indirect-addressing mode, and
        // retrieve the address operand
        switch (mode) {
            case 0:
                extraInfo = "]";
                break;
            case 1:
                extraInfo = " + " + toString(nextByte()) + "]";
                break;
            case 2:
            	extraInfo = " + " + toString(nextWord()) + "]";
                break;
            case 3:	
                return getReg8(memIndex);
            default:
                throw new RuntimeException();
        }
        
        switch (memIndex)
        {
	        case 0:
	            return "[BX + SI" + extraInfo;
	        case 1:
	        	return "[BX + DI" + extraInfo;
	        case 2:
	        	return "[BP + SI" + extraInfo;
	        case 3:
	        	return "[BP + DI" + extraInfo;
	        case 4:
	        	return "[SI" + extraInfo;
	        case 5:
	        	return "[DI" + extraInfo;
	        case 6:
	        	if (mode == 0)
	        		return "[" + toString(nextWord()) + "]";
	        	else
	        		return "[BP" + extraInfo;
	        case 7:
	        	return "[BX" + extraInfo;
	        default:
	            throw new RuntimeException();		
        }
	}
	
	/**
	 * similar to IndirectAddressingDecoder#getMem16()
	 * @return the indirect 16 bit memory description
	 * @throws DisassemblerException 
	 */
	private static String getMem16() throws DisassemblerException
	{
		if (mode == 3)
			return getReg16(memIndex);
		else
			return getMem8();
	}
	
	private static String getReg8()
	{
		return getReg8(regIndex);
	}
	
	/**
	 * similar to RegisterIndexingDecoder#getReg8()
	 * @return the indirect 8 bit register description
	 */
	private static String getReg8(byte index)
	{
		switch (index)
		{
	        case 0:
	            return "AL";
	        case 1:
	            return "CL";
	        case 2:
	            return "DL";
	        case 3:
	            return "BL";
	        case 4:
	            return "AH";
	        case 5:
	            return "CH";
	        case 6:
	            return "DH";
	        case 7:
	            return "BH";				
	        default:
	            throw new RuntimeException();				
		}
	}
	
	private static String getReg16()
	{
		return getReg16(regIndex);
	}
	
	/**
	 * similar to RegisterIndexingDecoder#getReg16()
	 * @return the indirect 16 bit register description
	 */
	private static String getReg16(byte index)
	{
		switch (index)
		{
	        case 0:
	            return "AX";
	        case 1:
	            return "CX";
	        case 2:
	            return "DX";
	        case 3:
	            return "BX";
	        case 4:
	            return "SP";
	        case 5:
	            return "BP";
	        case 6:
	            return "SI";
	        case 7:
	            return "DI";				
	        default:
	            throw new RuntimeException();				
		}
	}
	
	private static String getSeg()
	{
		return getSeg(regIndex);
	}
	
	/**
	 * 
	 * similar to RegisterIndexingDecoder#getSeg()
	 * @return the indirect segment register description
	 */
	private static String getSeg(byte index)
	{
        switch (index)
        {
            case 0:
                return "ES";
            case 1:
                return "CS";
            case 2:
                return "SS";
            case 3:
                return "DS";
            case 4:
                return "ES";
            case 5:
                return "CS";
            case 6:
                return "SS";
            case 7:
                return "DS";				
            default:
                throw new RuntimeException();				
        }
    }
	
	private static short toWord(byte b1, byte b2)
	{
		return (short)((Unsigned.unsignedByte(b2) << 8) |
                Unsigned.unsignedByte(b1));
	}
	
	public static String toString(byte b)
	{
		return "0x" + Integer.toHexString(b & 0xFF).toUpperCase();
	}
	
	public static String toString(short b)
	{
		return "0x" + Integer.toHexString(b & 0xFFFF).toUpperCase();
	}
	
	public static String toString(short segment, short offset)
	{
		String segmentS = toString(segment);
		while (segmentS.length() < 6)
			segmentS = "0x0" + segmentS.substring(2);
		
		String offsetS = toString(offset);
		while (offsetS.length() < 6)
			offsetS = "0x0" + offsetS.substring(2);
		return segmentS + ":" + offsetS;
	}
	
	private static String nextOpcode() throws DisassemblerException {
        byte opcode = nextByte();
        switch(opcode & 0xF0) {
            case 0x00:
                return opcode0X(opcode);
            case 0x10:
            	return opcode1X(opcode);
            case 0x20:
            	return opcode2X(opcode);
            case 0x30:
            	return opcode3X(opcode);
            case 0x40:
            	return opcode4X(opcode);
            case 0x50:
            	return opcode5X(opcode);
            case 0x60:
            	return opcode6X(opcode);
            case 0x70:
            	return opcode7X(opcode);
            case 0x80:
            	return opcode8X(opcode);
            case 0x90:
            	return opcode9X(opcode);
            case 0xA0:
            	return opcodeAX(opcode);
            case 0xB0:
            	return opcodeBX(opcode);
            case 0xC0:
            	return opcodeCX(opcode);
            case 0xD0:
            	return opcodeDX(opcode);
            case 0xE0:
            	return opcodeEX(opcode);
            case 0xF0:
            	return opcodeFX(opcode);
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcode0X(byte opcode) throws DisassemblerException{
        switch (opcode) {
            case (byte)0x00: // ADD [X], reg8
                resetIndirect();
            	return "ADD " + getMem8() + ", " + getReg8();
            case (byte)0x01: // ADD [X], reg16
                resetIndirect();
            	return "ADD " + getMem16() + ", " + getReg16();
            case (byte)0x02: // ADD reg8, [X]
                resetIndirect();
                return "ADD " + getReg8() + ", " + getMem8();
            case (byte)0x03: // ADD reg16, [X]
                resetIndirect();
                return "ADD " + getReg16() + ", " + getMem16();
            case (byte)0x04: // ADD AL, imm8
                return "ADD AL, " + toString(nextByte());
            case (byte)0x05: // ADD AX, imm16
            	return "ADD AX, " + toString(nextWord());
            case (byte)0x06: // PUSH ES
                return "PUSH ES";
            case (byte)0x07: // POP ES
                return "POP ES";				
            case (byte)0x08: // OR [X], reg8
                resetIndirect();
                return "OR " + getMem8() + ", " + getReg8();				
            case (byte)0x09: // OR [X], reg16
                resetIndirect();
            	return "OR " + getMem16() + ", " + getReg16();		
            case (byte)0x0A: // OR reg8, [X]
                resetIndirect();
            	return "OR " + getReg8() + ", " + getMem8();		
            case (byte)0x0B: // OR reg16, [X]
                resetIndirect();
            	return "OR " + getReg16() + ", " + getMem16();		
            case (byte)0x0C: // OR AL, imm8
                return "OR AL, " + toString(nextByte());
            case (byte)0x0D: // OR AX, imm16
            	return "OR AX, " + toString(nextWord());			
            case (byte)0x0E: // PUSH CS
                return "PUSH CS";
            case (byte)0x0F: // invalid opcode
            	throw new DisassemblerException();
            default:
                throw new RuntimeException();
        }		
    }

    private static String opcode1X(byte opcode) throws DisassemblerException{
        switch (opcode) {
            case (byte)0x10: // ADC [X], reg8
                resetIndirect();
                return "ADC " + getMem8() + ", " + getReg8();
            case (byte)0x11: // ADC [X], reg16
                resetIndirect();
            	return "ADC " + getMem16() + ", " + getReg16();
            case (byte)0x12: // ADC reg8, [X]
                resetIndirect();
                return "ADC " + getReg8() + ", " + getMem8();
            case (byte)0x13: // ADC reg16, [X]
                resetIndirect();
            	return "ADC " + getReg16() + ", " + getMem16();
            case (byte)0x14: // ADC AL, imm8
                return "ADC AL, " + toString(nextByte());
            case (byte)0x15: // ADC AX, imm16
            	return "ADC AX, " + toString(nextWord());			
            case (byte)0x16: // PUSH SS
                return "PUSH SS";
            case (byte)0x17: // POP SS
                return "POP SS";
            case (byte)0x18: // SBB [X], reg8
                resetIndirect();
                return "SBB " + getMem8() + ", " + getReg8();
            case (byte)0x19: // SBB [X], reg16
                resetIndirect();
            	return "SBB " + getMem16() + ", " + getReg16();
            case (byte)0x1A: // SBB reg8, [X]
                resetIndirect();
            	return "SBB " + getReg8() + ", " + getMem8();
            case (byte)0x1B: // SBB reg16, [X]
                resetIndirect();
            	return "SBB " + getReg16() + ", " + getMem16();
            case (byte)0x1C: // SBB AL, imm8
                return "SBB AL, " + toString(nextByte());
            case (byte)0x1D: // SBB AX, imm16
            	return "SBB AX, " + toString(nextWord());	
            case (byte)0x1E: // PUSH DS
                return "PUSH DS";
            case (byte)0x1F: // POP DS
                return "POP DS";
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcode2X(byte opcode) throws DisassemblerException{
        switch (opcode) {			
            case (byte)0x20: // AND [X], reg8
                resetIndirect();
            	return "AND " + getMem8() + ", " + getReg8();
            case (byte)0x21: // AND [X], reg16
                resetIndirect();
            	return "AND " + getMem16() + ", " + getReg16();
            case (byte)0x22: // AND reg8, [X]
                resetIndirect();
            	return "AND " + getReg8() + ", " + getMem8();
            case (byte)0x23: // AND reg16, [X]
                resetIndirect();
            	return "AND " + getReg16() + ", " + getMem16();
            case (byte)0x24: // AND AL, imm8
            	return "AND AL, " + toString(nextByte());
            case (byte)0x25: // AND AX, imm16
            	return "AND AX, " + toString(nextWord());		
            case (byte)0x28: // SUB [X], reg8
                resetIndirect();
            	return "SUB " + getMem8() + ", " + getReg8();
            case (byte)0x29: // SUB [X], reg16
                resetIndirect();
            	return "SUB " + getMem16() + ", " + getReg16();
            case (byte)0x2A: // SUB reg8, [X]
                resetIndirect();
            	return "SUB " + getReg8() + ", " + getMem8();
            case (byte)0x2B: // SUB reg16, [X]
                resetIndirect();
            	return "SUB " + getReg16() + ", " + getMem16();
            case (byte)0x2C: // SUB AL, imm8
            	return "SUB AL, " + toString(nextByte());
            case (byte)0x2D: // SUB AX, imm16
            	return "SUB AX, " + toString(nextWord());	
            case (byte)0x26: // TODO: 'ES:' prefix
            case (byte)0x27: // TODO: DAA
            case (byte)0x2E: // TODO: 'CS:' prefix
            case (byte)0x2F: // TODO: DAS
            	throw new DisassemblerException();
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcode3X(byte opcode) throws DisassemblerException {
        switch (opcode) {			
            case (byte)0x30: // XOR [X], reg8
                resetIndirect();
            	return "XOR " + getMem8() + ", " + getReg8();
            case (byte)0x31: // XOR [X], reg16
                resetIndirect();
            	return "XOR " + getMem16() + ", " + getReg16();
            case (byte)0x32: // XOR reg8, [X]
                resetIndirect();
            	return "XOR " + getReg8() + "," + getMem8();
            case (byte)0x33: // XOR reg16, [X]
                resetIndirect();
            	return "XOR " + getReg16() + ", " + getMem16();
            case (byte)0x34: // XOR AL, imm8
            	return "XOR AL, " + toString(nextByte());
            case (byte)0x35: // XOR AX, imm16
            	return "XOR AX, " + toString(nextWord());		
            case (byte)0x38: // CMP [X], reg8
                resetIndirect();
            	return "CMP " + getMem8() + ", " + getReg8();
            case (byte)0x39: // CMP [X], reg16
                resetIndirect();
            	return "CMP " + getMem16() + ", " + getReg16();
            case (byte)0x3A: // CMP reg8, [X]
                resetIndirect();
            	return "CMP " + getReg8() + ", " + getMem8();
            case (byte)0x3B: // CMP reg16, [X]
                resetIndirect();
            	return "CMP " + getReg16() + ", " + getMem16();
            case (byte)0x3C: // CMP AL, imm8
            	return "CMP AL, " + toString(nextByte());
            case (byte)0x3D: // CMP AX, imm16
            	return "CMP AX, " + toString(nextWord());
            case (byte)0x36: // TODO: 'SS:' prefix
            case (byte)0x37: // TODO: AAA
            case (byte)0x3E: // TODO: 'DS:' prefix
            case (byte)0x3F: // TODO: AAS
            	throw new DisassemblerException();
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcode4X(byte opcode) {
        byte index = (byte)(opcode & 0x07);
        switch (opcode) {
            case (byte)0x40: // INC reg16
            case (byte)0x41:
            case (byte)0x42:
            case (byte)0x43:
            case (byte)0x44:
            case (byte)0x45:
            case (byte)0x46:
            case (byte)0x47:
                return "INC " + getReg16(index);
            case (byte)0x48: // DEC reg16
            case (byte)0x49:
            case (byte)0x4A:
            case (byte)0x4B:
            case (byte)0x4C:
            case (byte)0x4D:
            case (byte)0x4E:
            case (byte)0x4F:
            	return "DEC " + getReg16(index);
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcode5X(byte opcode) {
        byte index = (byte)(opcode & 0x07);
        switch (opcode) {
            case (byte)0x50: // PUSH reg16
            case (byte)0x51:
            case (byte)0x52:
            case (byte)0x53:
            case (byte)0x54:
            case (byte)0x55:
            case (byte)0x56:
            case (byte)0x57:
            	return "PUSH " + getReg16(index);
            case (byte)0x58: // POP reg16
            case (byte)0x59:
            case (byte)0x5A:
            case (byte)0x5B:
            case (byte)0x5C:
            case (byte)0x5D:
            case (byte)0x5E:
            case (byte)0x5F:
            	return "POP " + getReg16(index);
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcode6X(byte opcode) throws DisassemblerException {
        // 0x60.. 0x6F - invalid opcodes
    	throw new DisassemblerException();	
    }

    private static String opcode7X(byte opcode) throws DisassemblerException {
        String jump = "";
        switch(opcode) {
            case (byte)0x70: // JO
            	jump = "JO";
            	break;
            case (byte)0x71: // JNO
            	jump = "JNO";
        		break;
            case (byte)0x72: // JC,JB,JNAE
            	jump = "JC";
        		break;
            case (byte)0x73: // JNC,JNB,JAE
            	jump = "JNC";
        		break;
            case (byte)0x74: // JE,JZ
            	jump = "JZ";
        		break;
            case (byte)0x75: // JNE,JNZ
            	jump = "JNZ";
        		break;
            case (byte)0x76: // JBE,JNA
            	jump = "JBE";
        		break;
            case (byte)0x77: // JNBE,JA
            	jump = "JNBE";
        		break;
            case (byte)0x78: // JS
            	jump = "JS";
        		break;
            case (byte)0x79: // JNS
            	jump = "JNS";
        		break;
            case (byte)0x7A: // JP,JPE
            	jump = "JP";
        		break;
            case (byte)0x7B: // JNP,JPO
            	jump = "JNP";
        		break;
            case (byte)0x7C: // JL,JNGE
            	jump = "JL";
        		break;
            case (byte)0x7D: // JNL,JGE
            	jump = "JNL";
        		break;
            case (byte)0x7E: // JLE,JNG
            	jump = "JLE";
        		break;
            case (byte)0x7F: // JNLE,JG
            	jump = "JNLE";
            	break;
        }
        
        return jump + " " + toString(nextByte());
    }

    private static String opcode8X(byte opcode) throws DisassemblerException {
        switch (opcode) {
            case (byte)0x80: // <?> byte ptr [X], imm8
            case (byte)0x82: // TODO: opcode 0x82 is identical to opcode 0x80 ?
                resetIndirect();
                switch (regIndex)
                {
                    case 0: // ADD
                        return "ADD " + getMem8() + ", " + toString(nextByte());
                    case 1: // OR
                    	return "OR " + getMem8() + ", " + toString(nextByte());
                    case 2: // ADC
                    	return "ADC " + getMem8() + ", " + toString(nextByte());
                    case 3: // SBB
                    	return "SBB " + getMem8() + ", " + toString(nextByte());
                    case 4: // AND
                    	return "AND " + getMem8() + ", " + toString(nextByte());
                    case 5: // SUB
                    	return "SUB " + getMem8() + ", " + toString(nextByte());
                    case 6: // XOR
                    	return "XOR " + getMem8() + ", " + toString(nextByte());
                    case 7: // CMP
                    	return "CMP " + getMem8() + ", " + toString(nextByte());
                    default:
                        throw new RuntimeException();
                }
            case (byte)0x81: // <?> word ptr [X], imm16
                resetIndirect();
                switch (regIndex)
                {
                    case 0: // ADD
                    	return "ADD " + getMem16() + ", " + toString(nextWord());
                    case 1: // OR
                    	return "OR " + getMem16() + ", " + toString(nextWord());
                    case 2: // ADC
                    	return "ADC " + getMem16() + ", " + toString(nextWord());
                    case 3: // SBB
                    	return "SBB " + getMem16() + ", " + toString(nextWord());
                    case 4: // AND
                    	return "AND " + getMem16() + ", " + toString(nextWord());
                    case 5: // SUB
                    	return "SUB " + getMem16() + ", " + toString(nextWord());
                    case 6: // XOR
                    	return "XOR " + getMem16() + ", " + toString(nextWord());
                    case 7: // CMP
                    	return "CMP " + getMem16() + ", " + toString(nextWord());
                    default:
                        throw new RuntimeException();
                }
            case (byte)0x83: // <?> word ptr [X], sign-extended imm8
                resetIndirect();
                switch (regIndex)
                {
                    case 0: // ADD
                        return "ADD WORD " + getMem16() + ", " + toString(nextByte());
                    case 1: // OR
                    	return "OR WORD " + getMem16() + ", " + toString(nextByte());
                    case 2: // ADC
                    	return "ADC WORD " + getMem16() + ", " + toString(nextByte());
                    case 3: // SBB
                    	return "SBB WORD " + getMem16() + ", " + toString(nextByte());
                    case 4: // AND
                    	return "AND WORD " + getMem16() + ", " + toString(nextByte());
                    case 5: // SUB
                    	return "SUB WORD " + getMem16() + ", " + toString(nextByte());
                    case 6: // XOR
                    	return "XOR WORD " + getMem16() + ", " + toString(nextByte());
                    case 7: // CMP
                    	return "CMP WORD " + getMem16() + ", " + toString(nextByte());
                    default:
                        throw new RuntimeException();
                }
            case (byte)0x84: // TEST reg8, [X]
                resetIndirect();
                return "TEST " + getReg8() + ", " + getMem8();			
            case (byte)0x85: // TEST reg16, [X]
                resetIndirect();
            	return "TEST " + getReg16() + ", " + getMem16();				
            case (byte)0x86: // XCHG reg8, [X]
                resetIndirect();
            	return "XCHG " + getReg8() + ", " + getMem8();
            case (byte)0x87: // XCHG reg16, [X]				
                resetIndirect();
            	return "XCHG " + getReg16() + ", " + getMem16();		
            case (byte)0x88: // MOV [X], reg8
                resetIndirect();
                return "MOV " + getMem8() + ", " + getReg8();
            case (byte)0x89: // MOV [X], reg16
                resetIndirect();
            	return "MOV " + getMem16() + ", " + getReg16();
            case (byte)0x8A: // MOV reg8, [X]
                resetIndirect();
            	return "MOV " + getReg8() + ", " + getMem8();
            case (byte)0x8B: // MOV reg16, [X]
                resetIndirect();
            	return "MOV " + getReg16() + ", " + getMem16();
            case (byte)0x8C: // MOV [X], seg
                resetIndirect();
                return "MOV " + getMem16() + ", " + getSeg();
            case (byte)0x8D: // LEA reg16, [X]
                resetIndirect();
                if (mode == 3) // "LEA reg16, reg16" is an invalid opcode
                    return "DW " + toString(toWord(opcode, getIndirect()));
                else
                	return "LEA " + getReg16() + ", " + getMem16();
            case (byte)0x8E: // MOV seg, [X]
                resetIndirect();
                return "MOV " + getSeg() + ", " + getMem16();
            case (byte)0x8F: // POP [X]
                // Note: since Reg index bits are ignored, there are 8 different
                // machine-code representations for this opcode :-)
                resetIndirect();
                return "POP " + getMem16();
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcode9X(byte opcode) throws DisassemblerException {
        switch (opcode) {
            case (byte)0x90: // XCHG reg16, AX
            case (byte)0x91:
            case (byte)0x92:
            case (byte)0x93:
            case (byte)0x94:
            case (byte)0x95:
            case (byte)0x96:
            case (byte)0x97:				
                byte index = (byte)(opcode & 0x07);
                return "XCHG " + getReg16(index) + ", AX";
            case (byte)0x98: // CBW
                return "CBW";
            case (byte)0x99: // CWD
                return "CWD";				
            case (byte)0x9A: // CALL far imm16:imm16
                return "CALL FAR " + toString(nextWord()) + ":" + toString(nextWord());
            case (byte)0x9B: // original: WAIT, modified: virtual opcode NRG
                // The virtual NRG opcode is made up of 4 consecutive WAIT opcodes
                for (int i = 0; i < 3; ++i)
                {
                    if (getByte() != (byte)0x9B)
                        throw new DisassemblerException();
                    else
                    	nextByte(); //wastes the byte, inorder to make the disassembler work with NRG opcodes
                }
                return "NRG";
            case (byte)0x9C: // PUSHF
                return "PUSHF";
            case (byte)0x9D: // POPF
                return "POPF";
            case (byte)0x9E: // SAHF
                // TODO: handle reserved bits (1,3,5)
                return "SAHF";
            case (byte)0x9F: // LAHF
                return "LAHF";
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcodeAX(byte opcode) throws DisassemblerException {
        switch (opcode) {
            case (byte)0xA0: // MOV AL, [imm16]
                return "MOV AL, [" + toString(nextWord()) + "]";
            case (byte)0xA1: // MOV AX, [imm16]
            	return "MOV AX, [" + toString(nextWord()) + "]";
            case (byte)0xA2: // MOV [imm16], AL
            	return "MOV [" + toString(nextWord()) + "], AL";
            case (byte)0xA3: // MOV [imm16], AX
            	return "MOV [" + toString(nextWord()) + "], AX";
            case (byte)0xA4: // MOVSB
                return "MOVSB";
            case (byte)0xA5: // MOVSW
                return "MOVSW";
            case (byte)0xA6: // CMPSB
                return "CMPSB";
            case (byte)0xA7: // CMPSW
                return "CMPSW";
            case (byte)0xA8: // TEST AL, imm8
                return "TEST AL, " + toString(nextByte());
            case (byte)0xA9: // TEST AX, imm16
            	return "TEST AX, " + toString(nextWord());				
            case (byte)0xAA: // STOSB
                return "STOSB";			
            case (byte)0xAB: // STOSW
                return "STOSW";			
            case (byte)0xAC: // LODSB
                return "LODSB";				
            case (byte)0xAD: // LODSW
                return "LODSW";				
            case (byte)0xAE: // SCASB
                return "SCASB";		
            case (byte)0xAF: // SCASW
                return "SCASW";				
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcodeBX(byte opcode) throws DisassemblerException {
        byte index = (byte)(opcode & 0x07);
        switch (opcode) {
            case (byte)0xB0: // MOV reg8, imm8
            case (byte)0xB1:
            case (byte)0xB2:
            case (byte)0xB3:
            case (byte)0xB4:
            case (byte)0xB5:
            case (byte)0xB6:
            case (byte)0xB7:
                return "MOV " + getReg8(index) + ", " + toString(nextByte());
            case (byte)0xB8: // MOV reg16, imm16
            case (byte)0xB9:
            case (byte)0xBA:
            case (byte)0xBB:
            case (byte)0xBC:
            case (byte)0xBD:
            case (byte)0xBE:
            case (byte)0xBF:
            	return "MOV " + getReg16(index) + ", " + toString(nextWord());
            default:
                throw new RuntimeException();
        }
    }	
	
    private static String opcodeCX(byte opcode) throws DisassemblerException {
        switch (opcode) {
            case (byte)0xC0:
            case (byte)0xC1:
                // 0xC0.. 0xC1 - invalid opcodes
                throw new DisassemblerException();
            case (byte)0xC2: // RETN [imm16]
                return "RET " + toString(nextWord());
            case (byte)0xC3: // RETN
                return "RET";
            case (byte)0xC4: // LES reg16, [X]
                resetIndirect();
                if (mode == 3)
                    // "LES reg16, reg16" is an invalid opcode
                    throw new DisassemblerException();					
                else
                	return "LES " + getReg16() + ", " + getMem8();		
            case (byte)0xC5: // LDS reg16, [X]
                resetIndirect();
                if (mode == 3)
                    // "LDS reg16, reg16" is an invalid opcode
                    throw new DisassemblerException();					
                else
                	return "LDS " + getReg16() + ", " + getMem8();		
            case (byte)0xC6: // MOV [X], imm8
                // Note: since Reg index bits are ignored, there are 8 different
                // machine-code representations for this opcode :-)
                resetIndirect();
            	return "MOV " + getMem8() + ", " + toString(nextByte());
            case (byte)0xC7: // MOV [X], imm16
                // Note: since Reg index bits are ignored, there are 8 different
                // machine-code representations for this opcode :-)
                resetIndirect();
                return "MOV " + getMem16() + ", " + toString(nextWord());
            case (byte)0xC8:
            case (byte)0xC9:
                // 0xC8.. 0xC9 - invalid opcodes
                throw new DisassemblerException();
            case (byte)0xCA: // RETF [imm16]
                return "RETF " + toString(nextWord());
            case (byte)0xCB: // RETF
                return "RETF";
            case (byte)0xCC: // INT3
                throw new DisassemblerException();
            case (byte)0xCD: // INT [imm8]
                byte opcodeId = nextByte();
                if (opcodeId == (byte)0x86)
                    return "INT 0x86";
                else if (opcodeId == (byte)0x87)
                    return "INT 0x87";
                else
                    throw new DisassemblerException();
            case (byte)0xCE: // INTO
                throw new DisassemblerException();
            case (byte)0xCF: // IRET
                return "IRET";
            default:
                throw new RuntimeException();
        }		
    }
	
    private static String opcodeDX(byte opcode) throws DisassemblerException {
        switch (opcode) {
            case (byte)0xD0: // <?> byte ptr [X], 1
                resetIndirect();
                switch (regIndex)
                {
                    case (byte)0x00: // ROL
                        return "ROL " + getMem8() + ", 1";
                    case (byte)0x01: // ROR
                    	return "ROR " + getMem8() + ", 1";
                    case (byte)0x02: // RCL
                    	return "RCL " + getMem8() + ", 1";
                    case (byte)0x03: // RCR
                    	return "RCR " + getMem8() + ", 1";
                    case (byte)0x04: // SHL
                    	return "SHL " + getMem8() + ", 1";
                    case (byte)0x05: // SHR
                    	return "SHR " + getMem8() + ", 1";
                    case (byte)0x06: // invalid opcode
                        throw new DisassemblerException();
                    case (byte)0x07: // SAR
                    	return "SAR " + getMem8() + ", 1";
                    default:
                        throw new RuntimeException();
                }
            case (byte)0xD1: // <?> word ptr [X], 1
                resetIndirect();
	            switch (regIndex)
	            {
	                case (byte)0x00: // ROL
	                    return "ROL " + getMem16() + ", 1";
	                case (byte)0x01: // ROR
	                	return "ROR " + getMem16() + ", 1";
	                case (byte)0x02: // RCL
	                	return "RCL " + getMem16() + ", 1";
	                case (byte)0x03: // RCR
	                	return "RCR " + getMem16() + ", 1";
	                case (byte)0x04: // SHL
	                	return "SHL " + getMem16() + ", 1";
	                case (byte)0x05: // SHR
	                	return "SHR " + getMem16() + ", 1";
	                case (byte)0x06: // invalid opcode
	                    throw new DisassemblerException();
	                case (byte)0x07: // SAR
	                	return "SAR " + getMem16() + ", 1";
	                default:
	                    throw new RuntimeException();
	            }
            case (byte)0xD2: // <?> byte ptr [X], CL
                resetIndirect();
	            switch (regIndex)
	            {
	                case (byte)0x00: // ROL
	                    return "ROL " + getMem8() + ", CL";
	                case (byte)0x01: // ROR
	                	return "ROR " + getMem8() + ", CL";
	                case (byte)0x02: // RCL
	                	return "RCL " + getMem8() + ", CL";
	                case (byte)0x03: // RCR
	                	return "RCR " + getMem8() + ", CL";
	                case (byte)0x04: // SHL
	                	return "SHL " + getMem8() + ", CL";
	                case (byte)0x05: // SHR
	                	return "SHR " + getMem8() + ", CL";
	                case (byte)0x06: // invalid opcode
	                    throw new DisassemblerException();
	                case (byte)0x07: // SAR
	                	return "SAR " + getMem8() + ", CL";
	                default:
	                    throw new RuntimeException();
	            }
            case (byte)0xD3: // <?> word ptr [x], CL
                resetIndirect();
	            switch (regIndex)
	            {
	                case (byte)0x00: // ROL
	                    return "ROL " + getMem16() + ", CL";
	                case (byte)0x01: // ROR
	                	return "ROR " + getMem16() + ", CL";
	                case (byte)0x02: // RCL
	                	return "RCL " + getMem16() + ", CL";
	                case (byte)0x03: // RCR
	                	return "RCR " + getMem16() + ", CL";
	                case (byte)0x04: // SHL
	                	return "SHL " + getMem16() + ", CL";
	                case (byte)0x05: // SHR
	                	return "SHR " + getMem16() + ", CL";
	                case (byte)0x06: // invalid opcode
	                    throw new DisassemblerException();
	                case (byte)0x07: // SAR
	                	return "SAR " + getMem16() + ", CL";
	                default:
	                    throw new RuntimeException();
	            }
            
            case (byte)0xD7: // XLAT, XLATB
                return "XLATB";
            case (byte)0xD4: // TODO: AAM
            case (byte)0xD5: // TODO: AAD
            case (byte)0xD6: // 0xD6 - invalid opcode
            case (byte)0xD8: // FADD dword
            case (byte)0xD9: // FLD dword
            case (byte)0xDA: // FIADD dword
            case (byte)0xDB: // FILD dword
            case (byte)0xDC: // FADD qword
            case (byte)0xDD: // FLD qword
            case (byte)0xDE: // FIADD word
            case (byte)0xDF: // FILD word
                throw new DisassemblerException();
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcodeEX(byte opcode) throws DisassemblerException {
        switch (opcode) {
            case (byte)0xE0: // LOOPNZ, LOOPNE
                return "LOOPNZ " + toString(nextByte());
            case (byte)0xE1: // LOOPZ, LOOPE
            	return "LOOPZ " + toString(nextByte());
            case (byte)0xE2: // LOOP
            	return "LOOP " + toString(nextByte());
            case (byte)0xE3: // JCXZ
            	return "JCXZ " + toString(nextByte());
            case (byte)0xE8: // CALL near imm16
                return "CALL NEAR " + toString(nextWord());		
            case (byte)0xE9: // JMP near imm16
            	return "JMP NEAR " + toString(nextWord());				
            case (byte)0xEA: // JMP far imm16:imm16
                return "JMP FAR " + toString(nextWord()) + ":" + toString(nextWord());				
            case (byte)0xEB: // JMP short imm8
            	return "JMP SHORT " + toString(nextByte());			
            case (byte)0xE4: // IN AL, imm8
            case (byte)0xE5: // IN AX, imm8
            case (byte)0xE6: // OUT imm8, AL
            case (byte)0xE7: // OUT imm8, AX				
            case (byte)0xEC: // IN AL, DX
            case (byte)0xED: // IN AX, DX
            case (byte)0xEE: // OUT DX, AL
            case (byte)0xEF: // OUT DX, AX
                throw new DisassemblerException();
            default:
                throw new RuntimeException();
        }
    }
	
    private static String opcodeFX(byte opcode) throws DisassemblerException {
        byte nextOpcode = getByte();
        switch (opcode) {
            case (byte)0xF2: // REPNZ
                switch (nextOpcode)
                {
                    case (byte)0xA6: // REPNZ CMPSB
                    	nextByte();
                        return "REPNZ CMPSB";
                    case (byte)0xA7: // REPNZ CMPSW
                    	nextByte();
                    	return "REPNZ CMPSW";
                    case (byte)0xAE: // REPNZ SCASB
                    	nextByte();
                    	return "REPNZ SCASB";
                    case (byte)0xAF: // REPNZ SCASW
                    	nextByte();
                    	return "REPNZ SCASW";
                    default:
                        throw new DisassemblerException();							
                }
            case (byte)0xF3: // REP, REPZ
                switch (nextOpcode)
                {
                    case (byte)0xA4: // REP MOVSB
                    	nextByte();
                        return "REP MOVSB";
                    case (byte)0xA5: // REP MOVSW
                    	nextByte();
                    	return "REP MOVSW";
                    case (byte)0xA6: // REPZ CMPSB
                    	nextByte();
                    	return "REP CMPSB";
                    case (byte)0xA7: // REPZ CMPSW
                    	nextByte();
                    	return "REP CMPSW";
                    case (byte)0xAA: // REP STOSB
                    	nextByte();
                    	return "REP STOSB";		
                    case (byte)0xAB: // REP STOSW
                    	nextByte();
                    	return "REP STOSW";			
                    case (byte)0xAC: // REP LODSB
                    	nextByte();
                    	return "REP LODSB";			
                    case (byte)0xAD: // REP LODSW
                    	nextByte();
                    	return "REP LODSW";
                    case (byte)0xAE: // REPZ SCASB
                    	nextByte();
                    	return "REP SCASB";
                    case (byte)0xAF: // REPZ SCASW
                    	nextByte();
                    	return "REP SCASW";
                    default:
                        throw new DisassemblerException();
                }				
            case (byte)0xF5: // CMC
                return "CMC";
            case (byte)0xF6: // <?> byte ptr [X]
                resetIndirect();
                switch (regIndex)
                {
                    case 0: // TEST imm8
                        return "TEST " + getMem8() + ", " + toString(nextByte());						
                    case 2: // NOT						
                        return "NOT " + getMem8();
                    case 3: // NEG
                    	return "NEG " + getMem8();
                    case 4: // MUL
                    	return "MUL " + getMem8();
                    case 6: // DIV
                        return "DIV " + getMem8();
                    case 1:
                    case 5: // TODO: IMUL
                    case 7: // TODO: IDIV
                        throw new DisassemblerException();
                    default:
                        throw new RuntimeException();
                }	
            case (byte)0xF7: // <?> word ptr [X]
	            resetIndirect();
	            switch (regIndex)
	            {
	                case 0: // TEST imm16
	                    return "TEST " + getMem16() + ", " + toString(nextWord());						
	                case 2: // NOT						
	                    return "NOT " + getMem16();
	                case 3: // NEG
	                	return "NEG " + getMem16();
	                case 4: // MUL
	                	return "MUL " + getMem16();
	                case 6: // DIV
	                    return "DIV " + getMem16();
	                case 1:
	                case 5: // TODO: IMUL
	                case 7: // TODO: IDIV
	                    throw new DisassemblerException();
	                default:
	                    throw new RuntimeException();
	            }		
            case (byte)0xF8: // CLC
                return "CLC";
            case (byte)0xF9: // STC
            	return "STC";
            case (byte)0xFA: // CLI
            	return "CLI";			
            case (byte)0xFB: // STI
            	return "STI";			
            case (byte)0xFC: // CLD
            	return "CLD";				
            case (byte)0xFD: // STD
            	return "STD";				
            case (byte)0xFE: // <?> byte ptr [X]
                resetIndirect();
                switch (regIndex)
                {
                    case 0: // INC
                        return "INC " + getMem8();
                    case 1: // DEC
                    	return "DEC " + getMem8();
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7: // invalid opcodes
                        throw new DisassemblerException();
                    default:
                        throw new RuntimeException();
                }
            case (byte)0xFF: // <?> word ptr [X]
                resetIndirect();
                switch (regIndex)
                {
                    case 0: // INC
                    	return "INC " + getMem16();
                    case 1: // DEC
                    	return "DEC " + getMem16();
                    case 2: // CALL near
                    	return "CALL NEAR " + getMem16();
                    case 3: // CALL far
                         if (mode == 3)
                            throw new DisassemblerException();
                         else
                        	 return "CALL FAR " + getMem16();
                    case 4: // JMP near
                        // FIXME: JMP SP bug ?
                        return "JMP NEAR " + getMem16();
                    case 5: // JMP far
                        if (mode == 3)
                            throw new DisassemblerException();
                        else
                        	return "JMP FAR " + getMem16();
                    case 6: // PUSH
                        return "PUSH " + getMem16();
                    case 7: // invalid opcode
                        throw new DisassemblerException();
                    default:
                        throw new RuntimeException();
                }
            case (byte)0xF0: // LOCK
            case (byte)0xF1: // 0xF1 - invalid opcode
            case (byte)0xF4: // HLT
                throw new DisassemblerException();
            default:
                throw new RuntimeException();
        }
    }
}