package il.co.codeguru.corewars8086.util.disasm;

import il.co.codeguru.corewars8086.util.Hex;

/**
 *
 * @author Erdem Guven
 */
public class Disasm186 extends Disasm86 {

    public Disasm186(Binary binary) {
		super(binary);
    }

    private String hexUWord() {
	return "0x"+ Hex.toHexStringUnsigned(nextWord(), 4);
    }

    private String hexUByte() {
	return "0x"+ Hex.toHexStringUnsigned(nextByte(), 2);
    }

    private String hexSByte() {
	return Hex.toHexStringSigned(nextByte(), 2);
    }

    @Override
	protected String processOpcode(byte opcode) {
		int opc = opcode&0xff;

		switch( opc ) {
		case 0x60: // PUSHA
			return "pusha";

		case 0x61: // POPA
			return "popa";

		case 0x68: // PUSH iw
			return "push word "+hexUWord();

		case 0x69: // IMUL reg,reg,word
		    m_indirect.reset();
		    return "imul " + m_indirect.getReg16() + "," +
			m_indirect.getMem16() + ",word " + hexUWord();

		case 0x6A: // PUSH ib
			return "push byte "+hexSByte();

		case 0x6B: // IMUL reg,reg,byte
		    m_indirect.reset();
		    return "imul " + m_indirect.getReg16() + "," +
			m_indirect.getMem16() + ",byte " + hexSByte();

		case 0xC0:{
			m_indirect.reset();
			String cmnd;
			switch (m_indirect.getRegIndex()) {
				case (byte)0x00: // ROL
					cmnd = "rol";
					break;
				case (byte)0x01: // ROR
					cmnd = "ror";
					break;
				case (byte)0x02: // RCL
					cmnd = "rcl";
					break;
				case (byte)0x03: // RCR
					cmnd = "rcr";
					break;
				case (byte)0x04: // SHL
					cmnd = "shl";
					break;
				case (byte)0x05: // SHR
					cmnd = "shr";
					break;
				case (byte)0x07: // SAR
					cmnd = "sar";
					break;
				default:
					prevByte();
					return null;
			}
			return cmnd+" byte " + m_indirect.getMem8() + "," + hexUByte();
		}

		case 0xC1:{
			m_indirect.reset();
			String cmnd;
			switch (m_indirect.getRegIndex()) {
				case (byte)0x00: // ROL
					cmnd = "rol";
					break;
				case (byte)0x01: // ROR
					cmnd = "ror";
					break;
				case (byte)0x02: // RCL
					cmnd = "rcl";
					break;
				case (byte)0x03: // RCR
					cmnd = "rcr";
					break;
				case (byte)0x04: // SHL
					cmnd = "shl";
					break;
				case (byte)0x05: // SHR
					cmnd = "shr";
					break;
				case (byte)0x07: // SAR
					cmnd = "sar";
					break;
				default:
					prevByte();
					return null;
			}
			return cmnd+" word " + m_indirect.getMem16() + "," + hexUByte();
		}

		case 0xC8: // ENTER
			return "enter "+hexUWord()+","+hexUByte();

		case 0xC9: // LEAVE
			return "leave";

		default:
			return super.processOpcode(opcode);
		}
	}
}
