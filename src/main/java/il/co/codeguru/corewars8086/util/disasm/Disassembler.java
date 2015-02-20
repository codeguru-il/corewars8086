package il.co.codeguru.corewars8086.util.disasm;

import il.co.codeguru.corewars8086.util.Hex;

/**
 *
 * @author Erdem Guven
 */
public class Disassembler {

    protected Binary binary;

    public Disassembler(Binary binary) {
        this.binary = binary;
    }

    public void setAddress(int adr) {
        binary.setAddress(adr);
    }

    public int getAddr() {
        return binary.getAddress();
    }

    public String nextLine() {
        byte opcode = nextByte();
        return "db " + Hex.toHexString(opcode, 2);
    }

    protected byte nextByte() {
        return binary.nextByte();
    }

    protected short nextWord() {
        return binary.nextWord();
    }

    protected void prevByte() {
        binary.seek(-1);
    }

}
