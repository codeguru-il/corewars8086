package il.co.codeguru.corewars8086.util.disasm;

/**
 *
 * @author Erdem Guven
 */
public interface Binary {

    int getOffset();

    int getAddress();

    void setAddress(int address);

    void seek(int offset);

    byte nextByte();

    short nextWord();

}
