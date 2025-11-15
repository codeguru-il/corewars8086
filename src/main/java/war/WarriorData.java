package war;

/**
 * Holds a single warrior's name & code.
 * 
 * @author DL
 */
public class WarriorData {

    /**
     * Constructor.
     * @param name   Warrior's name.
     * @param code   Warrior's code.
     * @param type   Warrior's type (
     */
    public WarriorData(String name, byte[] code, WarriorType type) {
        m_name = name;
        m_code = code;
        this.type = type;
    }

    /** @return the warrior's name. */
    public String getName() {
        return m_name;
    }

    /** @return the warrior's code. */
    public byte[] getCode() {
        return m_code;
    }

    public WarriorType getType() {
        return type;
    }

    /** Holds warrior's name */
    private final String m_name;

    /** Holds warrior's code */
    private final byte[] m_code;

    private final WarriorType type;


    @Override
    public String toString() {
        return m_name;
    }
}
