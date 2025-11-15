package utils;

/**
 * Unsigned numbers utilities.
 *
 * @author DL
 */
public class Unsigned {
    private Unsigned() {
    }

    public static short unsignedByte(byte num) {
        return (short) ((short) num & 0xFF);
    }

    public static int unsignedShort(short num) {
        return Short.toUnsignedInt(num);
    }

    public static int unsignedShort(int num) {
        return unsignedShort((short) num);
    }

    public static long unsignedInt(int num) {
        return Integer.toUnsignedLong(num);
    }
}
