package cpu;

import memory.MemoryException;
import memory.RealModeAddress;
import memory.RealModeMemory;
import utils.Unsigned;


/**
 * Implements a 8086 CPU. 
 * 
 * @author DL
 */
public class Cpu {
    
    /**
     * Constructor.
     * 
     * @param state     Startup state of CPU registers & flags.
     * @param memory    Real-Mode memory to use.
     */
    public Cpu(CpuState state, RealModeMemory memory) {
        m_state = state;
        m_memory = memory;
        m_fetcher = new OpcodeFetcher(m_state, m_memory);
        m_regs = new RegisterIndexingDecoder(m_state);
        m_indirect = new IndirectAddressingDecoder(m_state, m_memory, m_fetcher);
    }

    /**
     * Performs the next single opcode.
     * 
     * @throws CpuException    on any CPU error. 
     * @throws MemoryException on any Memory error. 
     */
    public void nextOpcode() throws CpuException, MemoryException {
        byte opcode = m_fetcher.nextByte();
        switch(opcode & 0xF0) {
            case 0x00:
                opcode0X(opcode);
                break;
            case 0x10:
                opcode1X(opcode);
                break;
            case 0x20:
                opcode2X(opcode);
                break;
            case 0x30:
                opcode3X(opcode);
                break;
            case 0x40:
                opcode4X(opcode);
                break;
            case 0x50:
                opcode5X(opcode);
                break;
            case 0x60:
                opcode6X(opcode);
                break;
            case 0x70:
                opcode7X(opcode);
                break;
            case 0x80:
                opcode8X(opcode);
                break;
            case 0x90:
                opcode9X(opcode);
                break;
            case 0xA0:
                opcodeAX(opcode);
                break;
            case 0xB0:
                opcodeBX(opcode);
                break;
            case 0xC0:
                opcodeCX(opcode);
                break;
            case 0xD0:
                opcodeDX(opcode);
                break;
            case 0xE0:
                opcodeEX(opcode);
                break;
            case 0xF0:
                opcodeFX(opcode);
                break;
        }
    }

    private void opcode0X(byte opcode) throws CpuException, MemoryException {
        switch (opcode) {
            case (byte)0x00: // ADD [X], reg8
                m_indirect.reset();
                m_indirect.setMem8(add8(m_indirect.getMem8(), m_indirect.getReg8()));
                break;
            case (byte)0x01: // ADD [X], reg16
                m_indirect.reset();
                m_indirect.setMem16(add16(m_indirect.getMem16(), m_indirect.getReg16()));
                break;
            case (byte)0x02: // ADD reg8, [X]
                m_indirect.reset();
                m_indirect.setReg8(add8(m_indirect.getReg8(), m_indirect.getMem8()));
                break;
            case (byte)0x03: // ADD reg16, [X]
                m_indirect.reset();
                m_indirect.setReg16(add16(m_indirect.getReg16(), m_indirect.getMem16()));
                break;
            case (byte)0x04: // ADD AL, imm8
                m_state.setAL(add8(m_state.getAL(), m_fetcher.nextByte()));
                break;
            case (byte)0x05: // ADD AX, imm16
                m_state.setAX(add16(m_state.getAX(), m_fetcher.nextWord()));
                break;
            case (byte)0x06: // PUSH ES
                push(m_state.getES());
                break;
            case (byte)0x07: // POP ES
                m_state.setES(pop());
                break;				
            case (byte)0x08: // OR [X], reg8
                m_indirect.reset();
                m_indirect.setMem8(or8(m_indirect.getMem8(), m_indirect.getReg8()));
                break;				
            case (byte)0x09: // OR [X], reg16
                m_indirect.reset();
                m_indirect.setMem16(or16(m_indirect.getMem16(), m_indirect.getReg16()));
                break;
            case (byte)0x0A: // OR reg8, [X]
                m_indirect.reset();
                m_indirect.setReg8(or8(m_indirect.getReg8(), m_indirect.getMem8()));
                break;
            case (byte)0x0B: // OR reg16, [X]
                m_indirect.reset();
                m_indirect.setReg16(or16(m_indirect.getReg16(), m_indirect.getMem16()));
                break;
            case (byte)0x0C: // OR AL, imm8
                m_state.setAL(or8(m_state.getAL(), m_fetcher.nextByte()));
                break;
            case (byte)0x0D: // OR AX, imm16
                m_state.setAX(or16(m_state.getAX(), m_fetcher.nextWord()));
                break;				
            case (byte)0x0E: // PUSH CS
                push(m_state.getCS());
                break;
            case (byte)0x0F:
                // 0x0F - invalid opcode
                throw new InvalidOpcodeException();
            default:
                throw new RuntimeException();
        }		
    }

    private void opcode1X(byte opcode) throws MemoryException {
        switch (opcode) {
            case (byte)0x10: // ADC [X], reg8
                m_indirect.reset();
                m_indirect.setMem8(adc8(m_indirect.getMem8(), m_indirect.getReg8()));
                break;
            case (byte)0x11: // ADC [X], reg16
                m_indirect.reset();
                m_indirect.setMem16(adc16(m_indirect.getMem16(), m_indirect.getReg16()));
                break;
            case (byte)0x12: // ADC reg8, [X]
                m_indirect.reset();
                m_indirect.setReg8(adc8(m_indirect.getReg8(), m_indirect.getMem8()));
                break;
            case (byte)0x13: // ADC reg16, [X]
                m_indirect.reset();
                m_indirect.setReg16(adc16(m_indirect.getReg16(), m_indirect.getMem16()));
                break;
            case (byte)0x14: // ADC AL, imm8
                m_state.setAL(adc8(m_state.getAL(), m_fetcher.nextByte()));
                break;
            case (byte)0x15: // ADC AX, imm16
                m_state.setAX(adc16(m_state.getAX(), m_fetcher.nextWord()));
                break;			
            case (byte)0x16: // PUSH SS
                push(m_state.getSS());
                break;
            case (byte)0x17: // POP SS
                m_state.setSS(pop());
                break;
            case (byte)0x18: // SBB [X], reg8
                m_indirect.reset();
                m_indirect.setMem8(sbb8(m_indirect.getMem8(), m_indirect.getReg8()));
                break;
            case (byte)0x19: // SBB [X], reg16
                m_indirect.reset();
                m_indirect.setMem16(sbb16(m_indirect.getMem16(), m_indirect.getReg16()));
                break;
            case (byte)0x1A: // SBB reg8, [X]
                m_indirect.reset();
                m_indirect.setReg8(sbb8(m_indirect.getReg8(), m_indirect.getMem8()));
                break;
            case (byte)0x1B: // SBB reg16, [X]
                m_indirect.reset();
                m_indirect.setReg16(sbb16(m_indirect.getReg16(), m_indirect.getMem16()));
                break;
            case (byte)0x1C: // SBB AL, imm8
                m_state.setAL(sbb8(m_state.getAL(), m_fetcher.nextByte()));
                break;
            case (byte)0x1D: // SBB AX, imm16
                m_state.setAX(sbb16(m_state.getAX(), m_fetcher.nextWord()));
                break;				
            case (byte)0x1E: // PUSH DS
                push(m_state.getDS());
                break;
            case (byte)0x1F: // POP DS
                m_state.setDS(pop());
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void opcode2X(byte opcode) throws CpuException, MemoryException {
        switch (opcode) {			
            case (byte)0x20: // AND [X], reg8
                m_indirect.reset();
                m_indirect.setMem8(and8(m_indirect.getMem8(), m_indirect.getReg8()));
                break;
            case (byte)0x21: // AND [X], reg16
                m_indirect.reset();
                m_indirect.setMem16(and16(m_indirect.getMem16(), m_indirect.getReg16()));
                break;
            case (byte)0x22: // AND reg8, [X]
                m_indirect.reset();
                m_indirect.setReg8(and8(m_indirect.getReg8(), m_indirect.getMem8()));
                break;
            case (byte)0x23: // AND reg16, [X]
                m_indirect.reset();
                m_indirect.setReg16(and16(m_indirect.getReg16(), m_indirect.getMem16()));
                break;
            case (byte)0x24: // AND AL, imm8
                m_state.setAL(and8(m_state.getAL(), m_fetcher.nextByte()));
                break;
            case (byte)0x25: // AND AX, imm16
                m_state.setAX(and16(m_state.getAX(), m_fetcher.nextWord()));
                break;			
            case (byte)0x26: // TODO: 'ES:' prefix
            case (byte)0x27: // TODO: DAA
                throw new UnimplementedOpcodeException();
            case (byte)0x28: // SUB [X], reg8
                m_indirect.reset();
                m_indirect.setMem8(sub8(m_indirect.getMem8(), m_indirect.getReg8()));
                break;
            case (byte)0x29: // SUB [X], reg16
                m_indirect.reset();
                m_indirect.setMem16(sub16(m_indirect.getMem16(), m_indirect.getReg16()));
                break;
            case (byte)0x2A: // SUB reg8, [X]
                m_indirect.reset();
                m_indirect.setReg8(sub8(m_indirect.getReg8(), m_indirect.getMem8()));
                break;
            case (byte)0x2B: // SUB reg16, [X]
                m_indirect.reset();
                m_indirect.setReg16(sub16(m_indirect.getReg16(), m_indirect.getMem16()));
                break;
            case (byte)0x2C: // SUB AL, imm8
                m_state.setAL(sub8(m_state.getAL(), m_fetcher.nextByte()));
                break;
            case (byte)0x2D: // SUB AX, imm16
                m_state.setAX(sub16(m_state.getAX(), m_fetcher.nextWord()));
                break;	
            case (byte)0x2E: // TODO: 'CS:' prefix
            case (byte)0x2F: // TODO: DAS
                throw new UnimplementedOpcodeException();
            default:
                throw new RuntimeException();
        }
    }

    private void opcode3X(byte opcode) throws CpuException, MemoryException {
        switch (opcode) {			
            case (byte)0x30: // XOR [X], reg8
                m_indirect.reset();
                m_indirect.setMem8(xor8(m_indirect.getMem8(), m_indirect.getReg8()));
                break;
            case (byte)0x31: // XOR [X], reg16
                m_indirect.reset();
                m_indirect.setMem16(xor16(m_indirect.getMem16(), m_indirect.getReg16()));
                break;
            case (byte)0x32: // XOR reg8, [X]
                m_indirect.reset();
                m_indirect.setReg8(xor8(m_indirect.getReg8(), m_indirect.getMem8()));
                break;
            case (byte)0x33: // XOR reg16, [X]
                m_indirect.reset();
                m_indirect.setReg16(xor16(m_indirect.getReg16(), m_indirect.getMem16()));
                break;
            case (byte)0x34: // XOR AL, imm8
                m_state.setAL(xor8(m_state.getAL(), m_fetcher.nextByte()));
                break;
            case (byte)0x35: // XOR AX, imm16
                m_state.setAX(xor16(m_state.getAX(), m_fetcher.nextWord()));
                break;			
            case (byte)0x36: // TODO: 'SS:' prefix
            case (byte)0x37: // TODO: AAA
                throw new UnimplementedOpcodeException();
            case (byte)0x38: // CMP [X], reg8
                m_indirect.reset();
                sub8(m_indirect.getMem8(), m_indirect.getReg8());
                break;
            case (byte)0x39: // CMP [X], reg16
                m_indirect.reset();
                sub16(m_indirect.getMem16(), m_indirect.getReg16());
                break;
            case (byte)0x3A: // CMP reg8, [X]
                m_indirect.reset();
                sub8(m_indirect.getReg8(), m_indirect.getMem8());
                break;
            case (byte)0x3B: // CMP reg16, [X]
                m_indirect.reset();
                sub16(m_indirect.getReg16(), m_indirect.getMem16());
                break;
            case (byte)0x3C: // CMP AL, imm8
                sub8(m_state.getAL(), m_fetcher.nextByte());
                break;
            case (byte)0x3D: // CMP AX, imm16
                sub16(m_state.getAX(), m_fetcher.nextWord());
                break;
            case (byte)0x3E: // TODO: 'DS:' prefix
            case (byte)0x3F: // TODO: AAS
                throw new UnimplementedOpcodeException();
            default:
                throw new RuntimeException();
        }
    }

    private void opcode4X(byte opcode) {
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
                m_regs.setReg16(index, inc16(m_regs.getReg16(index)));
                break;
            case (byte)0x48: // DEC reg16
            case (byte)0x49:
            case (byte)0x4A:
            case (byte)0x4B:
            case (byte)0x4C:
            case (byte)0x4D:
            case (byte)0x4E:
            case (byte)0x4F:
                m_regs.setReg16(index, dec16(m_regs.getReg16(index)));
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void opcode5X(byte opcode) throws MemoryException {
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
                push(m_regs.getReg16(index));
                break;
            case (byte)0x58: // POP reg16
            case (byte)0x59:
            case (byte)0x5A:
            case (byte)0x5B:
            case (byte)0x5C:
            case (byte)0x5D:
            case (byte)0x5E:
            case (byte)0x5F:
                m_regs.setReg16(index, pop());
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void opcode6X(byte opcode) throws CpuException {
        // 0x60.. 0x6F - invalid opcodes
        throw new InvalidOpcodeException();		
    }

    private void opcode7X(byte opcode) throws MemoryException {
        boolean branch = false;
        switch(opcode) {
            case (byte)0x70: // JO
                branch = m_state.getOverflowFlag();
                break;
            case (byte)0x71: // JNO
                branch = !m_state.getOverflowFlag();
                break;
            case (byte)0x72: // JC,JB,JNAE
                branch = m_state.getCarryFlag();
                break;
            case (byte)0x73: // JNC,JNC,JAE
                branch = !m_state.getCarryFlag();
                break;
            case (byte)0x74: // JE,JZ
                branch = m_state.getZeroFlag();
                break;
            case (byte)0x75: // JNE,JNZ
                branch = !m_state.getZeroFlag();
                break;
            case (byte)0x76: // JBE,JNA
                branch = (m_state.getCarryFlag() || m_state.getZeroFlag());				
                break;
            case (byte)0x77: // JNBE,JA
                branch = (!m_state.getCarryFlag() && !m_state.getZeroFlag());				
                break;
            case (byte)0x78: // JS
                branch = m_state.getSignFlag();			
                break;
            case (byte)0x79: // JNS
                branch = !m_state.getSignFlag();			
                break;
            case (byte)0x7A: // JP,JPE
                branch = m_state.getParityFlag();
                break;
            case (byte)0x7B: // JNP,JPO
                branch = !m_state.getParityFlag();
                break;
            case (byte)0x7C: // JL,JNGE
                branch = (m_state.getSignFlag() != m_state.getOverflowFlag());
                break;
            case (byte)0x7D: // JNL,JGE
                branch = (m_state.getSignFlag() == m_state.getOverflowFlag());
                break;
            case (byte)0x7E: // JLE,JNG
                branch = m_state.getZeroFlag() ||
                        (m_state.getSignFlag() != m_state.getOverflowFlag());
                break;
            case (byte)0x7F: // JNLE,JG
                branch = !m_state.getZeroFlag() &&
                        (m_state.getSignFlag() == m_state.getOverflowFlag());
                break;
        }
        byte offset = m_fetcher.nextByte();
        if (branch) {
            m_state.setIP((short)(m_state.getIP() + offset));
        }
    }

    private void opcode8X(byte opcode) throws CpuException, MemoryException {
        switch (opcode) {
            case (byte)0x80: // <?> byte ptr [X], imm8
            case (byte)0x82: // TODO: opcode 0x82 is identical to opcode 0x80 ?
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case 0: // ADD
                        m_indirect.setMem8(
                            add8(m_indirect.getMem8(), m_fetcher.nextByte()));
                        break;
                    case 1: // OR
                        m_indirect.setMem8(
                            or8(m_indirect.getMem8(), m_fetcher.nextByte()));
                        break;
                    case 2: // ADC
                        m_indirect.setMem8(
                            adc8(m_indirect.getMem8(), m_fetcher.nextByte()));						
                        break;
                    case 3: // SBB
                        m_indirect.setMem8(
                            sbb8(m_indirect.getMem8(), m_fetcher.nextByte()));						
                        break;
                    case 4: // AND
                        m_indirect.setMem8(
                            and8(m_indirect.getMem8(), m_fetcher.nextByte()));
                        break;
                    case 5: // SUB
                        m_indirect.setMem8(
                            sub8(m_indirect.getMem8(), m_fetcher.nextByte()));						
                        break;
                    case 6: // XOR
                        m_indirect.setMem8(
                            xor8(m_indirect.getMem8(), m_fetcher.nextByte()));
                        break;
                    case 7: // CMP
                        sub8(m_indirect.getMem8(), m_fetcher.nextByte());
                        break;
                    default:
                        throw new RuntimeException();
                }
                break;
            case (byte)0x81: // <?> word ptr [X], imm16
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case 0: // ADD
                        m_indirect.setMem16(
                            add16(m_indirect.getMem16(), m_fetcher.nextWord()));
                        break;
                    case 1: // OR
                        m_indirect.setMem16(
                            or16(m_indirect.getMem16(), m_fetcher.nextWord()));
                        break;
                    case 2: // ADC
                        m_indirect.setMem16(
                            adc16(m_indirect.getMem16(), m_fetcher.nextWord()));						
                        break;
                    case 3: // SBB
                        m_indirect.setMem16(
                            sbb16(m_indirect.getMem16(), m_fetcher.nextWord()));						
                        break;
                    case 4: // AND
                        m_indirect.setMem16(
                            and16(m_indirect.getMem16(), m_fetcher.nextWord()));
                        break;
                    case 5: // SUB
                        m_indirect.setMem16(
                            sub16(m_indirect.getMem16(), m_fetcher.nextWord()));						
                        break;
                    case 6: // XOR
                        m_indirect.setMem16(
                            xor16(m_indirect.getMem16(), m_fetcher.nextWord()));
                        break;
                    case 7: // CMP
                        sub16(m_indirect.getMem16(), m_fetcher.nextWord());
                        break;
                    default:
                        throw new RuntimeException();
                }
                break;
            case (byte)0x83: // <?> word ptr [X], sign-extended imm8
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case 0: // ADD
                        m_indirect.setMem16(
                            add16(m_indirect.getMem16(), m_fetcher.nextByte()));
                        break;
                    case 1: // OR
                        m_indirect.setMem16(
                            or16(m_indirect.getMem16(), m_fetcher.nextByte()));
                        break;
                    case 2: // ADC
                        m_indirect.setMem16(
                            adc16(m_indirect.getMem16(), m_fetcher.nextByte()));						
                        break;
                    case 3: // SBB
                        m_indirect.setMem16(
                            sbb16(m_indirect.getMem16(), m_fetcher.nextByte()));						
                        break;
                    case 4: // AND
                        m_indirect.setMem16(
                            and16(m_indirect.getMem16(), m_fetcher.nextByte()));
                        break;
                    case 5: // SUB
                        m_indirect.setMem16(
                            sub16(m_indirect.getMem16(), m_fetcher.nextByte()));						
                        break;
                    case 6: // XOR
                        m_indirect.setMem16(
                            xor16(m_indirect.getMem16(), m_fetcher.nextByte()));
                        break;
                    case 7: // CMP
                        sub16(m_indirect.getMem16(), m_fetcher.nextByte());
                        break;
                    default:
                        throw new RuntimeException();
                }
                break;
            case (byte)0x84: // TEST reg8, [X]
                m_indirect.reset();
                and8(m_indirect.getReg8(), m_indirect.getMem8());
                break;				
            case (byte)0x85: // TEST reg16, [X]
                m_indirect.reset();
                and16(m_indirect.getReg16(), m_indirect.getMem16());
                break;				
            case (byte)0x86: // XCHG reg8, [X]
                m_indirect.reset();
                byte tmpByte = m_indirect.getReg8();
                m_indirect.setReg8(m_indirect.getMem8());
                m_indirect.setMem8(tmpByte);
                break;
            case (byte)0x87: // XCHG reg16, [X]				
                m_indirect.reset();
                short tmpWord = m_indirect.getReg16();
                m_indirect.setReg16(m_indirect.getMem16());
                m_indirect.setMem16(tmpWord);
                break;			
            case (byte)0x88: // MOV [X], reg8
                m_indirect.reset();
                m_indirect.setMem8(m_indirect.getReg8());
                break;
            case (byte)0x89: // MOV [X], reg16
                m_indirect.reset();
                m_indirect.setMem16(m_indirect.getReg16());
                break;
            case (byte)0x8A: // MOV reg8, [X]
                m_indirect.reset();
                m_indirect.setReg8(m_indirect.getMem8());
                break;
            case (byte)0x8B: // MOV reg16, [X]
                m_indirect.reset();
                m_indirect.setReg16(m_indirect.getMem16());
                break;
            case (byte)0x8C: // MOV [X], seg
                m_indirect.reset();
                m_indirect.setMem16(m_indirect.getSeg());
                break;
            case (byte)0x8D: // LEA reg16, [X]
                m_indirect.reset();
                RealModeAddress address = m_indirect.getMemAddress();
                if (address == null) {
                    // "LEA reg16, reg16" is an invalid opcode
                    throw new InvalidOpcodeException();
                }
                m_indirect.setReg16(address.getOffset());
                break;
            case (byte)0x8E: // MOV seg, [X]
                m_indirect.reset();
                m_indirect.setSeg(m_indirect.getMem16());
                break;
            case (byte)0x8F: // POP [X]
                // Note: since Reg index bits are ignored, there are 8 different
                // machine-code representations for this opcode :-)
                m_indirect.reset();
                m_indirect.setMem16(pop());
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void opcode9X(byte opcode) throws CpuException, MemoryException {
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
                short tmp = m_regs.getReg16(index);
                m_regs.setReg16(index, m_state.getAX());
                m_state.setAX(tmp);
                break;
            case (byte)0x98: // CBW
                if (m_state.getAL() < 0) {
                    m_state.setAH((byte)0xFF);
                } else {
                    m_state.setAH((byte)0);					
                }
                break;
            case (byte)0x99: // CWD
                if (m_state.getAX() < 0) {
                    m_state.setDX((short)0xFFFF);
                } else {
                    m_state.setDX((short)0);					
                }
                break;				
            case (byte)0x9A: // CALL far imm16:imm16
                short newIP = m_fetcher.nextWord();
                short newCS = m_fetcher.nextWord();
                callFar(newCS, newIP);
                break;
            case (byte)0x9B: // original: WAIT, modified: virtual opcode NRG
                // The virtual NRG opcode is made up of 2 consecutive WAIT opcodes
                if (m_fetcher.nextByte() != (byte)0x9B) {
                    throw new UnsupportedOpcodeException();
                }
                int energy = Unsigned.unsignedShort(m_state.getEnergy());
                if (energy < 0xFFFF) {
                    m_state.setEnergy((short)(energy+1));
                }
                break;
            case (byte)0x9C: // PUSHF
                push(m_state.getFlags());
                break;
            case (byte)0x9D: // POPF
                m_state.setFlags(pop());
                break;
            case (byte)0x9E:
                // TODO: handle reserved bits (1,3,5)
                short flags = m_state.getFlags();
                flags &= 0xFF00;
                flags |= m_state.getAH();
                m_state.setFlags(flags);
                break;
            case (byte)0x9F: // LAHF
                m_state.setAH((byte)m_state.getFlags());
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void opcodeAX(byte opcode) throws MemoryException {
        RealModeAddress address = null;
        switch (opcode) {
            case (byte)0xA0: // MOV AL, [imm16]
                address = new RealModeAddress(m_state.getDS(), m_fetcher.nextWord());
                m_state.setAL(m_memory.readByte(address));
                break;
            case (byte)0xA1: // MOV AX, [imm16]
                address = new RealModeAddress(m_state.getDS(), m_fetcher.nextWord());
                m_state.setAX(m_memory.readWord(address));
                break;
            case (byte)0xA2: // MOV [imm16], AL
                address = new RealModeAddress(m_state.getDS(), m_fetcher.nextWord());
                m_memory.writeByte(address, m_state.getAL());
                break;
            case (byte)0xA3: // MOV [imm16], AX
                address = new RealModeAddress(m_state.getDS(), m_fetcher.nextWord());
                m_memory.writeWord(address, m_state.getAX());
                break;
            case (byte)0xA4: // MOVSB
                movsb();
                break;
            case (byte)0xA5: // MOVSW
                movsw();
                break;
            case (byte)0xA6: // CMPSB
                cmpsb();
                break;
            case (byte)0xA7: // CMPSW
                cmpsw();
                break;
            case (byte)0xA8: // TEST AL, imm8
                and8(m_state.getAL(), m_fetcher.nextByte());
                break;
            case (byte)0xA9: // TEST AX, imm16
                and16(m_state.getAX(), m_fetcher.nextWord());
                break;				
            case (byte)0xAA: // STOSB
                stosb();
                break;				
            case (byte)0xAB: // STOSW
                stosw();
                break;				
            case (byte)0xAC: // LODSB
                lodsb();
                break;				
            case (byte)0xAD: // LODSW
                lodsw();
                break;				
            case (byte)0xAE: // SCASB
                scasb();
                break;				
            case (byte)0xAF: // SCASW
                scasw();
                break;				
            default:
                throw new RuntimeException();
        }
    }

    private void opcodeBX(byte opcode) throws MemoryException {
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
                m_regs.setReg8(index, m_fetcher.nextByte());
                break;
            case (byte)0xB8: // MOV reg16, imm16
            case (byte)0xB9:
            case (byte)0xBA:
            case (byte)0xBB:
            case (byte)0xBC:
            case (byte)0xBD:
            case (byte)0xBE:
            case (byte)0xBF:
                m_regs.setReg16(index, m_fetcher.nextWord());
                break;
            default:
                throw new RuntimeException();
        }
    }	

    private void opcodeCX(byte opcode) throws CpuException, MemoryException {
        short sizeToPop;
        RealModeAddress address1 = null;
        RealModeAddress address2 = null;
        switch (opcode) {
            case (byte)0xC0:
            case (byte)0xC1:
                // 0xC0.. 0xC1 - invalid opcodes
                throw new InvalidOpcodeException();
            case (byte)0xC2: // RETN [imm16]
                sizeToPop = m_fetcher.nextWord();
                m_state.setIP(pop());				
                m_state.setSP((short)(m_state.getSP() + sizeToPop));
                break;
            case (byte)0xC3: // RETN
                m_state.setIP(pop());
                break;
            case (byte)0xC4: // LES reg16, [X]
                m_indirect.reset();
                address1 = m_indirect.getMemAddress();
                if (address1 == null) {
                    // "LES reg16, reg16" is an invalid opcode
                    throw new InvalidOpcodeException();					
                }
                address2 = new RealModeAddress(
                    address1.getSegment(), (short)(address1.getOffset() + 2));

                m_indirect.setReg16(m_memory.readWord(address1));
                m_state.setES(m_memory.readWord(address2));
                break;			
            case (byte)0xC5: // LDS reg16, [X]
                m_indirect.reset();
                address1 = m_indirect.getMemAddress();
                if (address1 == null) {
                    // "LDS reg16, reg16" is an invalid opcode
                    throw new InvalidOpcodeException();					
                }
                address2 = new RealModeAddress(
                    address1.getSegment(), (short)(address1.getOffset() + 2));

                m_indirect.setReg16(m_memory.readWord(address1));
                m_state.setDS(m_memory.readWord(address2));
                break;			
            case (byte)0xC6: // MOV [X], imm8
                // Note: since Reg index bits are ignored, there are 8 different
                // machine-code representations for this opcode :-)
                m_indirect.reset();
                m_indirect.setMem8(m_fetcher.nextByte());
                break;
            case (byte)0xC7: // MOV [X], imm16
                // Note: since Reg index bits are ignored, there are 8 different
                // machine-code representations for this opcode :-)
                m_indirect.reset();
                m_indirect.setMem16(m_fetcher.nextWord());
                break;
            case (byte)0xC8:
            case (byte)0xC9:
                // 0xC8.. 0xC9 - invalid opcodes
                throw new InvalidOpcodeException();
            case (byte)0xCA: // RETF [imm16]
                sizeToPop = m_fetcher.nextWord();
                m_state.setIP(pop());				
                m_state.setCS(pop());
                m_state.setSP((short)(m_state.getSP() + sizeToPop));
                break;
            case (byte)0xCB: // RETF
                m_state.setIP(pop());
                m_state.setCS(pop());
                break;
            case (byte)0xCC: // INT3
                throw new IntOpcodeException();
            case (byte)0xCD: // INT [imm8]
                {
                    byte opcodeId = m_fetcher.nextByte();
                    if (opcodeId == (byte)0x86) {
                        int86();
                    } else if (opcodeId == (byte)0x87) {
                        int87();
                    } else {
                        throw new IntOpcodeException();
                    }
                }
                break;
            case (byte)0xCE: // INTO
                throw new IntOpcodeException();
            case (byte)0xCF: // IRET
                m_state.setIP(pop());
                m_state.setCS(pop());
                m_state.setFlags(pop());
                break;
            default:
                throw new RuntimeException();
        }		
    }

    private void opcodeDX(byte opcode) throws CpuException, MemoryException {
        switch (opcode) {
            case (byte)0xD0: // <?> byte ptr [X], 1
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case (byte)0x00: // ROL
                        rol8(1);
                        break;
                    case (byte)0x01: // ROR
                        ror8(1);
                        break;
                    case (byte)0x02: // RCL
                        rcl8(1);
                        break;
                    case (byte)0x03: // RCR
                        rcr8(1);
                        break;
                    case (byte)0x04: // SHL
                        shl8(1);
                        break;
                    case (byte)0x05: // SHR
                        shr8(1);
                        break;
                    case (byte)0x06: // invalid opcode
                        throw new InvalidOpcodeException();
                    case (byte)0x07: // SAR
                        sar8(1);
                        break;
                    default:
                        throw new RuntimeException();
                }
                break;
            case (byte)0xD1: // <?> word ptr [X], 1
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case (byte)0x00: // ROL
                        rol16(1);
                        break;
                    case (byte)0x01: // ROR
                        ror16(1);
                        break;
                    case (byte)0x02: // RCL
                        rcl16(1);
                        break;
                    case (byte)0x03: // RCR
                        rcr16(1);
                        break;
                    case (byte)0x04: // SHL
                        shl16(1);
                        break;
                    case (byte)0x05: // SHR
                        shr16(1);
                        break;
                    case (byte)0x06: // invalid opcode
                        throw new InvalidOpcodeException();
                    case (byte)0x07: // SAR
                        sar16(1);
                        break;
                    default:
                        throw new RuntimeException();
                }
                break;
            case (byte)0xD2: // <?> byte ptr [X], CL
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case (byte)0x00: // ROL
                        rol8(m_state.getCL());
                        break;
                    case (byte)0x01: // ROR
                        ror8(m_state.getCL());
                        break;
                    case (byte)0x02: // RCL
                        rcl8(m_state.getCL());
                        break;
                    case (byte)0x03: // RCR
                        rcr8(m_state.getCL());
                        break;
                    case (byte)0x04: // SHL
                        shl8(m_state.getCL());
                        break;
                    case (byte)0x05: // SHR
                        shr8(m_state.getCL());
                        break;
                    case (byte)0x06: // invalid opcode
                        throw new InvalidOpcodeException();
                    case (byte)0x07: // SAR
                        sar8(m_state.getCL());
                        break;
                    default:
                        throw new RuntimeException();
                }
                break;
            case (byte)0xD3: // <?> word ptr [x], CL
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case (byte)0x00: // ROL
                        rol16(m_state.getCL());
                        break;
                    case (byte)0x01: // ROR
                        ror16(m_state.getCL());
                        break;
                    case (byte)0x02: // RCL
                        rcl16(m_state.getCL());
                        break;
                    case (byte)0x03: // RCR
                        rcr16(m_state.getCL());
                        break;
                    case (byte)0x04: // SHL
                        shl16(m_state.getCL());
                        break;
                    case (byte)0x05: // SHR
                        shr16(m_state.getCL());
                        break;
                    case (byte)0x06: // invalid opcode
                        throw new InvalidOpcodeException();
                    case (byte)0x07: // SAR
                        sar16(m_state.getCL());
                        break;
                    default:
                        throw new RuntimeException();
                }
                break;
            case (byte)0xD4: // TODO: AAM
            case (byte)0xD5: // TODO: AAD
                throw new UnimplementedOpcodeException();
            case (byte)0xD6:
                // 0xD6 - invalid opcode
                throw new InvalidOpcodeException();
            case (byte)0xD7: // XLAT, XLATB
                RealModeAddress address = new RealModeAddress(m_state.getDS(),
                    (short)(m_state.getBX() + Unsigned.unsignedByte(m_state.getAL())));
                m_state.setAL(m_memory.readByte(address));
                break;
            case (byte)0xD8: // FADD dword
            case (byte)0xD9: // FLD dword
            case (byte)0xDA: // FIADD dword
            case (byte)0xDB: // FILD dword
            case (byte)0xDC: // FADD qword
            case (byte)0xDD: // FLD qword
            case (byte)0xDE: // FIADD word
            case (byte)0xDF: // FILD word
                throw new UnsupportedOpcodeException();
            default:
                throw new RuntimeException();
        }
    }

    private void opcodeEX(byte opcode) throws CpuException, MemoryException {
        byte offset8;
        short offset16;
        short newCX;
        short newCS;
        short newIP;
        switch (opcode) {
            case (byte)0xE0: // LOOPNZ, LOOPNE
                offset8 = m_fetcher.nextByte();
                newCX = (short)(m_state.getCX() - 1);
                m_state.setCX(newCX);
                if ((newCX != 0) && (!m_state.getZeroFlag())) {
                    m_state.setIP((short)(m_state.getIP() + offset8));
                }
                break;
            case (byte)0xE1: // LOOPZ, LOOPE
                offset8 = m_fetcher.nextByte();
                newCX = (short)(m_state.getCX() - 1);
                m_state.setCX(newCX);
                if ((newCX != 0) && (m_state.getZeroFlag())) {
                    m_state.setIP((short)(m_state.getIP() + offset8));
                }
                break;
            case (byte)0xE2: // LOOP
                offset8 = m_fetcher.nextByte();
                newCX = (short)(m_state.getCX() - 1);
                m_state.setCX(newCX);
                if (newCX != 0) {
                    m_state.setIP((short)(m_state.getIP() + offset8));
                }
                break;
            case (byte)0xE3: // JCXZ
                offset8 = m_fetcher.nextByte();
                if (m_state.getCX() == 0) {
                    m_state.setIP((short)(m_state.getIP() + offset8));					
                }
                break;
            case (byte)0xE4: // IN AL, imm8
            case (byte)0xE5: // IN AX, imm8
            case (byte)0xE6: // OUT imm8, AL
            case (byte)0xE7: // OUT imm8, AX				
                throw new UnsupportedOpcodeException();
            case (byte)0xE8: // CALL near imm16
                offset16 = m_fetcher.nextWord();
                callNear((short)(m_state.getIP() + offset16));
                break;				
            case (byte)0xE9: // JMP near imm16
                offset16 = m_fetcher.nextWord();
                m_state.setIP((short)(m_state.getIP() + offset16));
                break;				
            case (byte)0xEA: // JMP far imm16:imm16
                newIP = m_fetcher.nextWord();
                newCS = m_fetcher.nextWord();
                m_state.setIP(newIP);
                m_state.setCS(newCS);
                break;				
            case (byte)0xEB: // JMP short imm8
                offset8 = m_fetcher.nextByte();
                m_state.setIP((short)(m_state.getIP() + offset8));
                break;			
            case (byte)0xEC: // IN AL, DX
            case (byte)0xED: // IN AX, DX
            case (byte)0xEE: // OUT DX, AL
            case (byte)0xEF: // OUT DX, AX
                throw new UnsupportedOpcodeException();
            default:
                throw new RuntimeException();
        }
    }

    private void opcodeFX(byte opcode) throws CpuException, MemoryException {
        byte nextOpcode;
        boolean doneLooping;
        switch (opcode) {
            case (byte)0xF0: // LOCK
                throw new UnsupportedOpcodeException();
            case (byte)0xF1:
                // 0xF1 - invalid opcode
                throw new InvalidOpcodeException();
            case (byte)0xF2: // REPNZ
                nextOpcode = m_fetcher.nextByte();
                doneLooping = true;
                if (m_state.getCX() != 0) {
                    m_state.setCX((short)(m_state.getCX() - 1));
                    doneLooping = false;
                }
                switch (nextOpcode) {
                    case (byte)0xA6: // REPNZ CMPSB
                        if (!doneLooping) {
                            cmpsb();
                            doneLooping = m_state.getZeroFlag();
                        }
                        break;
                    case (byte)0xA7: // REPNZ CMPSW
                        if (!doneLooping) {
                            cmpsw();
                            doneLooping = m_state.getZeroFlag();
                        }
                        break;
                    case (byte)0xAE: // REPNZ SCASB
                        if (!doneLooping) {
                            scasb();
                            doneLooping = m_state.getZeroFlag();
                        }
                        break;
                    case (byte)0xAF: // REPNZ SCASW
                        if (!doneLooping) {
                            scasw();
                            doneLooping = m_state.getZeroFlag();							
                        }
                        break;
                    default:
                        throw new InvalidOpcodeException();							
                }				
                // loop if needed
                if (!doneLooping) {
                    m_state.setIP((short)(m_state.getIP() - 2));					
                }
                break;				
            case (byte)0xF3: // REP, REPZ
                nextOpcode = m_fetcher.nextByte();
                doneLooping = true;
                if (m_state.getCX() != 0) {
                    m_state.setCX((short)(m_state.getCX() - 1));
                    doneLooping = false;
                }
                switch (nextOpcode) {
                    case (byte)0xA4: // REP MOVSB
                        if (!doneLooping) {
                            movsb();
                        }
                        break;
                    case (byte)0xA5: // REP MOVSW
                        if (!doneLooping) {
                            movsw();
                        }
                        break;
                    case (byte)0xA6: // REPZ CMPSB
                        if (!doneLooping) {
                            cmpsb();
                            doneLooping = !m_state.getZeroFlag();
                        }
                        break;
                    case (byte)0xA7: // REPZ CMPSW
                        if (!doneLooping) {
                            cmpsw();
                            doneLooping = !m_state.getZeroFlag();
                        }
                        break;
                    case (byte)0xAA: // REP STOSB
                        if (!doneLooping) {
                            stosb();
                        }
                        break;				
                    case (byte)0xAB: // REP STOSW
                        if (!doneLooping) {
                            stosw();
                        }
                        break;				
                    case (byte)0xAC: // REP LODSB
                        if (!doneLooping) {
                            lodsb();
                        }
                        break;				
                    case (byte)0xAD: // REP LODSW
                        if (!doneLooping) {
                            lodsw();
                        }
                        break;
                    case (byte)0xAE: // REPZ SCASB
                        if (!doneLooping) {
                            scasb();
                            doneLooping = !m_state.getZeroFlag();
                        }
                        break;
                    case (byte)0xAF: // REPZ SCASW
                        if (!doneLooping) {
                            scasw();
                            doneLooping = !m_state.getZeroFlag();
                        }
                        break;
                    default:
                        throw new InvalidOpcodeException();
                }				
                // loop if needed
                if (!doneLooping) {
                    m_state.setIP((short)(m_state.getIP() - 2));					
                }
                break;
            case (byte)0xF4: // HLT
                throw new UnsupportedOpcodeException();
            case (byte)0xF5: // CMC
                m_state.setCarryFlag(!m_state.getCarryFlag());
                break;
            case (byte)0xF6: // <?> byte ptr [X]
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case 0: // TEST imm8
                        and8(m_indirect.getMem8(), m_fetcher.nextByte());
                        break;
                    case 1:
                        throw new InvalidOpcodeException();							
                    case 2: // NOT						
                        m_indirect.setMem8((byte)(m_indirect.getMem8() ^ 0xFF));
                        break;
                    case 3: // NEG
                        m_indirect.setMem8(sub8((byte)0, m_indirect.getMem8()));						
                        break;
                    case 4: // MUL
                        // multiply
                        short result = (short)(
                            Unsigned.unsignedByte(m_state.getAL()) *
                            Unsigned.unsignedByte(m_indirect.getMem8()));
                        m_state.setAH((byte)(result >> 8));
                        m_state.setAL((byte)result);

                        // update flags
                        if (m_state.getAH() == 0) {
                            m_state.setOverflowFlag(false);
                            m_state.setCarryFlag(false);
                        } else {
                            m_state.setOverflowFlag(true);
                            m_state.setCarryFlag(true);							
                        }
                        break;
                    case 5: // TODO: IMUL
                        throw new UnimplementedOpcodeException();
                    case 6: // DIV
                        int tmp = Unsigned.unsignedShort(m_state.getAX());
                        short divisor = Unsigned.unsignedByte(m_indirect.getMem8());
                        if (divisor == 0) { // divide by zero ?
                            throw new DivisionException();
                        }
                        short quotient = (short)(tmp / divisor);
                        if (quotient > 0xFF) { // divide overflow ?
                            throw new DivisionException();
                        }
                        m_state.setAL((byte)quotient);
                        m_state.setAH((byte)(tmp % divisor));
                        break;
                    case 7: // TODO: IDIV
                        throw new UnimplementedOpcodeException();
                    default:
                        throw new RuntimeException();
                }
                break;		
            case (byte)0xF7: // <?> word ptr [X]
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case 0: // TEST imm16
                        and16(m_indirect.getMem16(), m_fetcher.nextWord());
                        break;
                    case 1:
                        throw new InvalidOpcodeException();							
                    case 2: // NOT						
                        m_indirect.setMem16((short)(m_indirect.getMem16() ^ 0xFFFF));
                        break;
                    case 3: // NEG
                        m_indirect.setMem16(sub16((short)0, m_indirect.getMem16()));						
                        break;
                    case 4: // MUL
                        // multiply
                        int result =
                            Unsigned.unsignedShort(m_state.getAX()) *
                            Unsigned.unsignedShort(m_indirect.getMem16());
                        m_state.setDX((short)(result >> 16));
                        m_state.setAX((short)result);

                        // update flags
                        if (m_state.getDX() == 0) {
                            m_state.setOverflowFlag(false);
                            m_state.setCarryFlag(false);
                        } else {
                            m_state.setOverflowFlag(true);
                            m_state.setCarryFlag(true);							
                        }
                        break;
                    case 5: // TODO: IMUL
                        throw new UnimplementedOpcodeException();
                    case 6: // DIV
                        long tmp = Unsigned.unsignedInt(
                            (Unsigned.unsignedShort(m_state.getDX()) << 16) +
                            Unsigned.unsignedShort(m_state.getAX()));
                        int divisor = Unsigned.unsignedShort(m_indirect.getMem16());
                        if (divisor == 0) { // divide by zero ?
                            throw new DivisionException();
                        }
                        int quotient = (int)(tmp / divisor);
                        if (quotient > 0xFFFF) { // divide overflow ?
                            throw new DivisionException();
                        }
                        m_state.setAX((short)quotient);
                        m_state.setDX((short)(tmp % divisor));
                        break;
                    case 7: // TODO: IDIV
                        throw new UnimplementedOpcodeException();
                    default:
                        throw new RuntimeException();
                }
                break;		
            case (byte)0xF8: // CLC
                m_state.setCarryFlag(false);
                break;
            case (byte)0xF9: // STC
                m_state.setCarryFlag(true);
                break;
            case (byte)0xFA: // CLI
                m_state.setInterruptFlag(false);
                break;				
            case (byte)0xFB: // STI
                m_state.setInterruptFlag(true);
                break;				
            case (byte)0xFC: // CLD
                m_state.setDirectionFlag(false);
                break;				
            case (byte)0xFD: // STD
                m_state.setDirectionFlag(true);
                break;				
            case (byte)0xFE: // <?> byte ptr [X]
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case 0: // INC
                        m_indirect.setMem8(inc8(m_indirect.getMem8()));
                        break;
                    case 1: // DEC
                        m_indirect.setMem8(dec8(m_indirect.getMem8()));
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        // invalid opcodes
                        throw new InvalidOpcodeException();
                    default:
                        throw new RuntimeException();
                }
                break;
            case (byte)0xFF: // <?> word ptr [X]
                m_indirect.reset();
                switch (m_indirect.getRegIndex()) {
                    case 0: // INC
                        m_indirect.setMem16(inc16(m_indirect.getMem16()));
                        break;
                    case 1: // DEC
                        m_indirect.setMem16(dec16(m_indirect.getMem16()));
                        break;
                    case 2: // CALL near
                        callNear(m_indirect.getMem16());
                        break;
                    case 3: // CALL far
                        {
                            RealModeAddress address = m_indirect.getMemAddress();
                            if (address == null) {
                                throw new InvalidOpcodeException();
                            }

                            short newIP = m_memory.readWord(address);
                            address = new RealModeAddress(address.getSegment(),
                                (short)(address.getOffset() + 2));
                            short newCS = m_memory.readWord(address);
                            callFar(newCS, newIP);
                        }
                        break;
                    case 4: // JMP near
                        // FIXME: JMP SP bug ?
                        m_state.setIP(m_indirect.getMem16());
                        break;
                    case 5: // JMP far
                        {
                            RealModeAddress address = m_indirect.getMemAddress();
                            if (address == null) {
                                throw new InvalidOpcodeException();
                            }

                            short newIP = m_memory.readWord(address);
                            address = new RealModeAddress(address.getSegment(),
                                (short)(address.getOffset() + 2));
                            short newCS = m_memory.readWord(address);
                            m_state.setCS(newCS);
                            m_state.setIP(newIP);
                        }
                        break;
                    case 6: // PUSH
                        push(m_indirect.getMem16());
                        break;
                    case 7: // invalid opcode
                        throw new InvalidOpcodeException();
                    default:
                        throw new RuntimeException();
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void push(short value) throws MemoryException {
        m_state.setSP((short)(m_state.getSP() - 2));
        RealModeAddress stackPtr = new RealModeAddress(
            m_state.getSS(), m_state.getSP());
        m_memory.writeWord(stackPtr, value);
    }

    private short pop() throws MemoryException {
        RealModeAddress stackPtr = new RealModeAddress(
            m_state.getSS(), m_state.getSP());
        short value = m_memory.readWord(stackPtr);
        m_state.setSP((short)(m_state.getSP() + 2));
        return value;
    }	

    /**
     * Updates the CPU flags register after an 8bit operation.
     * @param value  Result of an 8bit operation.
     */
    private void updateFlags8(short value) {
        m_state.setCarryFlag((value & 0xFF00) != 0);
        // TODO: update overflow flag
        updateFlagsNoCarryOverflow8((byte)value);
    }

    /**
     * Updates the CPU flags register after a 16bit operation.
     * @param value  Result of a 16bit operation.
     */
    private void updateFlags16(int value) {
        m_state.setCarryFlag((value & 0xFFFF0000) != 0);
        // TODO: update overflow flag
        updateFlagsNoCarryOverflow16((short)value);
    }

    /**
     * Updates the CPU flags register after an 8bit operation, except for the
     * Carry and Overflow flags.
     * @param value  Result of an 8bit operation.
     */
    private void updateFlagsNoCarryOverflow8(byte value) {
        // TODO: update aux flag		
        m_state.setParityFlag(getParity(value));
        m_state.setSignFlag((value & 0x80) != 0);
        m_state.setZeroFlag(value == 0);		
    }

    /**
     * Updates the CPU flags register after a 16bit operation, except for the
     * Carry and Overflow flags.
     * @param value  Result of a 16bit operation.
     */
    private void updateFlagsNoCarryOverflow16(short value) {
        byte byteValue = (byte)value;
        // TODO: update aux flag		
        m_state.setParityFlag(getParity(byteValue));
        m_state.setSignFlag((value & 0x8000) != 0);
        m_state.setZeroFlag(value == 0);
    }	

    /**
     * Adds two 8bit values, updates the flags and returns the result.
     */
    private byte add8(byte value1, byte value2) {
        short result16 = 
            (short)(Unsigned.unsignedByte(value1) + Unsigned.unsignedByte(value2));
        byte result8 = (byte)result16;	
        updateFlags8(result16);
        return result8;
    }

    /**
     * Adds two 16bit values, updates the flags and returns the result.
     */
    private short add16(short value1, short value2) {
        int result32 =
            Unsigned.unsignedShort(value1) + Unsigned.unsignedShort(value2);
        short result16 = (short)result32;		
        updateFlags16(result32);
        return result16;
    }

    /**
     * Increments an 8bit value, updates the flags and returns the result.
     * Note: does not modify the carry flag.
     */
    private byte inc8(byte value) {
        boolean oldCarry = m_state.getCarryFlag();
        byte result = add8(value, (byte)1);
        m_state.setCarryFlag(oldCarry);
        return result;		
    }

    /**
     * Increments a 16bit value, updates the flags and returns the result.
     * Note: does not modify the carry flag.
     */
    private short inc16(short value) {
        boolean oldCarry = m_state.getCarryFlag();
        short result = add16(value, (short)1);
        m_state.setCarryFlag(oldCarry);
        return result;		
    }

    /**
     * Decrements an 8bit value, updates the flags and returns the result.
     * Note: does not modify the carry flag.
     */
    private byte dec8(byte value) {
        boolean oldCarry = m_state.getCarryFlag();
        byte result = sub8(value, (byte)1);
        m_state.setCarryFlag(oldCarry);
        return result;		
    }

    /**
     * Decrements a 16bit value, updates the flags and returns the result.
     * Note: does not modify the carry flag.
     */
    private short dec16(short value) {
        boolean oldCarry = m_state.getCarryFlag();
        short result = sub16(value, (short)1);
        m_state.setCarryFlag(oldCarry);
        return result;		
    }

    /**
     * ORs two 8bit values, updates the flags and returns the result.
     */
    private byte or8(byte value1, byte value2) {
        short result16 = 
            (short)(Unsigned.unsignedByte(value1) | Unsigned.unsignedByte(value2));
        byte result8 = (byte)result16;	
        updateFlags8(result16);
        return result8;
    }

    /**
     * ORs two 16bit values, updates the flags and returns the result.
     */
    private short or16(short value1, short value2) {
        int result32 =
            Unsigned.unsignedShort(value1) | Unsigned.unsignedShort(value2);
        short result16 = (short)result32;		
        updateFlags16(result32);
        return result16;
    }

    /**
     * Adds two 8bit values with the carry flag, updates the flags and returns
     * the result.
     */
    private byte adc8(byte value1, byte value2) {
        short result16 = 
            (short)(Unsigned.unsignedByte(value1) + Unsigned.unsignedByte(value2));
        if (m_state.getCarryFlag()) {
            ++result16;
        }
        byte result8 = (byte)result16;	
        updateFlags8(result16);
        return result8;
    }

    /**
     * Adds two 16bit values with the carry flag, updates the flags and returns
     * the result.
     */
    private short adc16(short value1, short value2) {
        int result32 =
            Unsigned.unsignedShort(value1) + Unsigned.unsignedShort(value2);
        if (m_state.getCarryFlag()) {
            ++result32;
        }
        short result16 = (short)result32;		
        updateFlags16(result32);
        return result16;
    }	

    /**
     * Subtracts a given 8bit value with the sum of a second 8bit value and the
     * carry flag, updates the flags and returns the result.
     */
    private byte sbb8(byte value1, byte value2) {
        short result16 = 
            (short)(Unsigned.unsignedByte(value1) - Unsigned.unsignedByte(value2));
        if (m_state.getCarryFlag()) {
            --result16;
        }
        byte result8 = (byte)result16;	
        updateFlags8(result16);
        return result8;
    }

    /**
     * Subtracts a given 16bit value with the sum of a second 16bit value and the
     * carry flag, updates the flags and returns the result.
     */
    private short sbb16(short value1, short value2) {
        int result32 =
            Unsigned.unsignedShort(value1) - Unsigned.unsignedShort(value2);
        if (m_state.getCarryFlag()) {
            --result32;
        }
        short result16 = (short)result32;		
        updateFlags16(result32);
        return result16;
    }

    /**
     * ANDs two 8bit values, updates the flags and returns the result.
     */
    private byte and8(byte value1, byte value2) {
        short result16 = 
            (short)(Unsigned.unsignedByte(value1) & Unsigned.unsignedByte(value2));
        byte result8 = (byte)result16;	
        updateFlags8(result16);
        return result8;
    }

    /**
     * ANDs two 16bit values, updates the flags and returns the result.
     */
    private short and16(short value1, short value2) {
        int result32 =
            Unsigned.unsignedShort(value1) & Unsigned.unsignedShort(value2);
        short result16 = (short)result32;		
        updateFlags16(result32);
        return result16;
    }	

    /**
     * Subtracts two 8bit values, updates the flags and returns the result.
     */
    private byte sub8(byte value1, byte value2) {
        short result16 = 
            (short)(Unsigned.unsignedByte(value1) - Unsigned.unsignedByte(value2));
        byte result8 = (byte)result16;	
        updateFlags8(result16);
        return result8;
    }

    /**
     * Subtracts two 16bit values, updates the flags and returns the result.
     */
    private short sub16(short value1, short value2) {
        int result32 =
            Unsigned.unsignedShort(value1) - Unsigned.unsignedShort(value2);
        short result16 = (short)result32;		
        updateFlags16(result32);
        return result16;
    }

    /**
     * XORs two 8bit values, updates the flags and returns the result.
     */
    private byte xor8(byte value1, byte value2) {
        short result16 = 
            (short)(Unsigned.unsignedByte(value1) ^ Unsigned.unsignedByte(value2));
        byte result8 = (byte)result16;	
        updateFlags8(result16);
        return result8;
    }

    /**
     * XORs two 16bit values, updates the flags and returns the result.
     */
    private short xor16(short value1, short value2) {
        int result32 =
            Unsigned.unsignedShort(value1) ^ Unsigned.unsignedShort(value2);
        short result16 = (short)result32;		
        updateFlags16(result32);
        return result16;
    }

    /**
     * Implements a near call opcode.
     * @param offset    New value for IP (CS stays the same).
     * @throws MemoryException
     */
    private void callNear(short offset) throws MemoryException {
        push(m_state.getIP());
        m_state.setIP(offset);
    }

    /**
     * Implements a far call opcode.
     * @param segment   New value for CS.
     * @param offset    New value for IP.
     * @throws MemoryException
     */
    private void callFar(short segment, short offset) throws MemoryException {
        push(m_state.getCS());				
        m_state.setCS(segment);
        callNear(offset);
    }

    /**
     * Implements the 'movsb' opcode.
     * @throws MemoryException
     */
    private void movsb() throws MemoryException {
        RealModeAddress src =
            new RealModeAddress(m_state.getDS(), m_state.getSI());
        RealModeAddress dst =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        m_memory.writeByte(dst, m_memory.readByte(src));

        byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1); 
        m_state.setSI((short)(m_state.getSI() + diff));
        m_state.setDI((short)(m_state.getDI() + diff));		
    }

    /**
     * Implements the 'movsw' opcode.
     * @throws MemoryException
     */
    private void movsw() throws MemoryException {
        RealModeAddress src = 
            new RealModeAddress(m_state.getDS(), m_state.getSI());
        RealModeAddress dst =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        m_memory.writeWord(dst, m_memory.readWord(src));

        byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2); 
        m_state.setSI((short)(m_state.getSI() + diff));
        m_state.setDI((short)(m_state.getDI() + diff));		
    }

    /**
     * Implements the 'cmpsb' opcode.
     * @throws MemoryException
     */
    private void cmpsb() throws MemoryException {
        RealModeAddress address1 =
            new RealModeAddress(m_state.getDS(), m_state.getSI());
        RealModeAddress address2 =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        sub8(m_memory.readByte(address1), m_memory.readByte(address2));		

        byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1); 
        m_state.setSI((short)(m_state.getSI() + diff));
        m_state.setDI((short)(m_state.getDI() + diff));		
    }

    /**
     * Implements the 'cmpsw' opcode.
     * @throws MemoryException
     */
    private void cmpsw() throws MemoryException {
        RealModeAddress address1 = 
            new RealModeAddress(m_state.getDS(), m_state.getSI());
        RealModeAddress address2 =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        sub16(m_memory.readWord(address1), m_memory.readWord(address2));		

        byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2); 
        m_state.setSI((short)(m_state.getSI() + diff));
        m_state.setDI((short)(m_state.getDI() + diff));		
    }	

    /**
     * Implements the 'stosb' opcode.
     * @throws MemoryException
     */
    private void stosb() throws MemoryException {
        RealModeAddress address =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        m_memory.writeByte(address, m_state.getAL());
        byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1); 
        m_state.setDI((short)(m_state.getDI() + diff));
    }

    /**
     * Implements the 'stosw' opcode.
     * @throws MemoryException
     */
    private void stosw() throws MemoryException {
        RealModeAddress address =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        m_memory.writeWord(address, m_state.getAX());
        byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2); 
        m_state.setDI((short)(m_state.getDI() + diff));
    }

    /**
     * Implements the virtual 'stosdw' opcode.
     * @throws MemoryException
     */
    private void stosdw() throws MemoryException {
        RealModeAddress address1 =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        m_memory.writeWord(address1, m_state.getAX());

        RealModeAddress address2 =
            new RealModeAddress(m_state.getES(), (short)(m_state.getDI() + 2));
        m_memory.writeWord(address2, m_state.getDX());

        byte diff = (m_state.getDirectionFlag() ? (byte)-4 : (byte)4); 
        m_state.setDI((short)(m_state.getDI() + diff));
    }

    /**
     * Implements the 'lodsb' opcode.
     * @throws MemoryException
     */
    private void lodsb() throws MemoryException {
        RealModeAddress address =
            new RealModeAddress(m_state.getDS(), m_state.getSI());
        m_state.setAL(m_memory.readByte(address));
        byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1); 
        m_state.setSI((short)(m_state.getSI() + diff));		
    }

    /**
     * Implements the 'lodsw' opcode.
     * @throws MemoryException
     */
    private void lodsw() throws MemoryException {
        RealModeAddress address =
            new RealModeAddress(m_state.getDS(), m_state.getSI());
        m_state.setAX(m_memory.readWord(address));
        byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2); 
        m_state.setSI((short)(m_state.getSI() + diff));
    }

    /**
     * Implements the 'scasb' opcode.
     * @throws MemoryException
     */
    private void scasb() throws MemoryException {
        RealModeAddress address =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        sub8(m_state.getAL(), m_memory.readByte(address));
        byte diff = (m_state.getDirectionFlag() ? (byte)-1 : (byte)1); 
        m_state.setDI((short)(m_state.getDI() + diff));
    }

    /**
     * Implements the 'scasw' opcode.
     * @throws MemoryException
     */
    private void scasw() throws MemoryException {
        RealModeAddress address =
            new RealModeAddress(m_state.getES(), m_state.getDI());
        sub16(m_state.getAX(), m_memory.readWord(address));
        byte diff = (m_state.getDirectionFlag() ? (byte)-2 : (byte)2); 
        m_state.setDI((short)(m_state.getDI() + diff));
    }

    private void rol8(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            byte val = m_indirect.getMem8();
            byte msb1 = (byte)((val >> 7) & 0x01);
            byte msb2 = (byte)((val >> 6) & 0x01);

            val = (byte)((val << 1) | msb1);
            m_indirect.setMem8(val);

            m_state.setCarryFlag(msb1 != 0);
            m_state.setOverflowFlag(msb1 != msb2);
        }
    }

    private void ror8(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            byte val = m_indirect.getMem8();
            byte lsb = (byte)(val & 0x01);

            val = (byte)(((val & 0xFF) >>> 1) | (lsb << 7));
            m_indirect.setMem8(val);

            byte msb1 = (byte)((val >> 7) & 0x01);
            byte msb2 = (byte)((val >> 6) & 0x01);			

            m_state.setCarryFlag(lsb != 0);
            m_state.setOverflowFlag(msb1 != msb2);
        }
    }

    private void rcl8(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            byte val = m_indirect.getMem8();
            byte msb1 = (byte)((val >> 7) & 0x01);
            byte msb2 = (byte)((val >> 6) & 0x01);
            byte cf = (byte)(m_state.getCarryFlag() ? 1 : 0);

            val = (byte)((val << 1) | cf);
            m_indirect.setMem8(val);

            m_state.setCarryFlag(msb1 != 0);
            m_state.setOverflowFlag(msb1 != msb2);
        }
    }

    private void rcr8(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            byte val = m_indirect.getMem8();
            byte lsb = (byte)(val & 0x01);
            byte cf = (byte)(m_state.getCarryFlag() ? 1 : 0);

            val = (byte)(((val & 0xFF) >>> 1) | (cf << 7));
            m_indirect.setMem8(val);

            byte msb1 = (byte)((val >> 7) & 0x01);
            byte msb2 = (byte)((val >> 6) & 0x01);			

            m_state.setCarryFlag(lsb != 0);
            m_state.setOverflowFlag(msb1 != msb2);
        }
    }

    private void shl8(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            byte val = m_indirect.getMem8();
            byte msb1 = (byte)((val >> 7) & 0x01);
            byte msb2 = (byte)((val >> 6) & 0x01);

            val = (byte)(val << 1);
            m_indirect.setMem8(val);

            m_state.setCarryFlag(msb1 != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            updateFlagsNoCarryOverflow8(val);
        }
    }

    private void shr8(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            byte val = m_indirect.getMem8();
            byte lsb = (byte)(val & 0x01);

            val = (byte)((val & 0xFF) >>> 1);
            m_indirect.setMem8(val);

            byte msb1 = (byte)((val >> 7) & 0x01);
            byte msb2 = (byte)((val >> 6) & 0x01);			

            m_state.setCarryFlag(lsb != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            updateFlagsNoCarryOverflow8(val);
        }
    }

    private void sar8(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            byte val = m_indirect.getMem8();
            byte lsb = (byte)(val & 0x01);

            val = (byte)(val >> 1);
            m_indirect.setMem8(val);

            byte msb1 = (byte)((val >> 7) & 0x01);
            byte msb2 = (byte)((val >> 6) & 0x01);			

            m_state.setCarryFlag(lsb != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            updateFlagsNoCarryOverflow8(val);
        }
    }

    private void rol16(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            short val = m_indirect.getMem16();
            byte msb1 = (byte)((val >> 15) & 0x01);
            byte msb2 = (byte)((val >> 14) & 0x01);

            val = (short)((val << 1) | msb1);
            m_indirect.setMem16(val);

            m_state.setCarryFlag(msb1 != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            updateFlagsNoCarryOverflow16(val);
        }
    }

    private void ror16(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            short val = m_indirect.getMem16();
            byte lsb = (byte)(val & 0x01);

            val = (short)(((val & 0xFFFF) >>> 1) | (lsb << 15));
            m_indirect.setMem16(val);

            byte msb1 = (byte)((val >> 15) & 0x01);
            byte msb2 = (byte)((val >> 14) & 0x01);			

            m_state.setCarryFlag(lsb != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            updateFlagsNoCarryOverflow16(val);
        }
    }

    private void rcl16(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            short val = m_indirect.getMem16();
            byte msb1 = (byte)((val >> 15) & 0x01);
            byte msb2 = (byte)((val >> 14) & 0x01);
            byte cf = (byte)(m_state.getCarryFlag() ? 1 : 0);

            val = (short)((val << 1) | cf);
            m_indirect.setMem16(val);

            m_state.setCarryFlag(msb1 != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            updateFlagsNoCarryOverflow16(val);
        }
    }

    private void rcr16(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            short val = m_indirect.getMem16();
            byte lsb = (byte)(val & 0x01);
            byte cf = (byte)(m_state.getCarryFlag() ? 1 : 0);

            val = (short)(((val & 0xFFFF) >>> 1) | (cf << 15));
            m_indirect.setMem16(val);

            byte msb1 = (byte)((val >> 15) & 0x01);
            byte msb2 = (byte)((val >> 14) & 0x01);			

            m_state.setCarryFlag(lsb != 0);
            m_state.setOverflowFlag(msb1 != msb2);
        }
    }

    private void shl16(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            short val = m_indirect.getMem16();
            byte msb1 = (byte)((val >> 15) & 0x01);
            byte msb2 = (byte)((val >> 14) & 0x01);

            val = (short)(val << 1);
            m_indirect.setMem16(val);

            m_state.setCarryFlag(msb1 != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            m_state.setZeroFlag(val == 0);
        }
    }

    private void shr16(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            short val = m_indirect.getMem16();
            byte lsb = (byte)(val & 0x01);

            val = (short) ((val & 0xFFFF) >>> 1);
            m_indirect.setMem16(val);

            byte msb1 = (byte)((val >> 15) & 0x01);
            byte msb2 = (byte)((val >> 14) & 0x01);			

            m_state.setCarryFlag(lsb != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            m_state.setZeroFlag(val == 0);
        }
    }

    private void sar16(int count) throws MemoryException {
        count &= 0x1F; // restrict count to 0-31

        for (int i = 0; i < count; ++i) {
            short val = m_indirect.getMem16();
            byte lsb = (byte)(val & 0x01);

            val = (short)(val >> 1);
            m_indirect.setMem16(val);

            byte msb1 = (byte)((val >> 15) & 0x01);
            byte msb2 = (byte)((val >> 14) & 0x01);			

            m_state.setCarryFlag(lsb != 0);
            m_state.setOverflowFlag(msb1 != msb2);
            m_state.setZeroFlag(val == 0);
        }
    }


    /**
     * Implements the virtual INT 0x86 opcode.
     * If enough bombs are left, calls the stosdw virtual
     * opcode 64 times.  
     * 
     * @throws MemoryException
     */
    private void int86() throws MemoryException {
        byte bombCount = m_state.getBomb1Count();
        if (bombCount != 0) {
            m_state.setBomb1Count((byte)(bombCount - 1));

            for (int i = 0; i < 64; ++i) {
                stosdw();
            }
        }
    }

    /**
     * Implements the virtual INT 0x87 opcode.
     * If enough bombs are left, searches for a given 4 bytes and replaces
     * their first occurence with another given 4 bytes.
     *  
     * @throws MemoryException
     */
    private void int87() throws MemoryException {
        byte bombCount = m_state.getBomb2Count();
        if (bombCount != 0) {
            m_state.setBomb2Count((byte)(bombCount - 1));

            for (int i = 0; i <= 0xFFFF; ++i) {
                int diff = (m_state.getDirectionFlag() ? -i : i);

                RealModeAddress address1 = new RealModeAddress(
                    m_state.getES(), (short)(m_state.getDI() + diff));

                if (m_memory.readWord(address1) == m_state.getAX()) {
                    RealModeAddress address2 = new RealModeAddress(
                        m_state.getES(), (short)(m_state.getDI() + diff + 2));
                    if (m_memory.readWord(address2) == m_state.getDX()) {
                        // found!
                        m_memory.writeWord(address1, m_state.getBX());
                        m_memory.writeWord(address2, m_state.getCX());

                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns true iff the given byte's bit parity is EVEN.
     * @param value  Value for which bit parity will be tested.
     * @return true iff the given byte's bit parity is EVEN.
     */
    private boolean getParity(byte value) {
        return PARITY_TABLE[Unsigned.unsignedByte(value)];
    }

    /**
     * Parity table implementation.
     * An array memeber of 'true' means the bit parity for the given index is EVEN.
     */
    private static final boolean PARITY_TABLE[] = {
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true,
        true, false, false, true, false, true, true, false,
        true, false, false, true, false, true, true, false,
        false, true, true, false, true, false, false, true
    };

    /** Current state of registers & flags */
    private final CpuState m_state;

    /** Real-Mode memory used by this class */
    private final RealModeMemory m_memory;

    /** Used to fetch next instruction bytes */
    private final OpcodeFetcher m_fetcher;

    /** Used to decode register indexing in opcodes */
    private final RegisterIndexingDecoder m_regs;

    /** Used to decode indirect-addressing opcodes */
    private final IndirectAddressingDecoder m_indirect;
}
