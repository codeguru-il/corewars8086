package utils;

/**
 * Unsigned numbers utilities.
 * 
 * @author DL
 */
public class Unsigned {
    
    public static short unsignedByte(byte num) {
        return (short)((short)num & 0xFF);
    }
    
    public static int unsignedShort(short num) {
        return unsignedShort((int)num);
    }
    
    public static int unsignedShort(int num) {
        return (num & 0xFFFF);
    }
    
    public static long unsignedInt(int num) {
        return ((long)num & 0xFFFFFFFF);
    }
    
    /**
     * Private constructor.
     */
    private Unsigned() {}
}
