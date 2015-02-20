package il.co.codeguru.corewars8086.hardware.cpu;

/**
 * Decodes the CPU's internal register indexing to the actual registers.
 * 
 * @author DL
 */
public class RegisterIndexingDecoder {

    /**
     * Constructor.
     * 
     * @param state   CPU Registers.
     */
    RegisterIndexingDecoder(CpuState state) {
        m_state = state;
    }

    /**
     * Returns the value of the 8bit register whose index is given.
     * @param index   CPU's internal register index.
     * @return the value of the 8bit register whose index is given.
     */
    public byte getReg8(byte index) {
        switch (index) {
            case 0:
                return m_state.getAL();
            case 1:
                return m_state.getCL();
            case 2:
                return m_state.getDL();
            case 3:
                return m_state.getBL();
            case 4:
                return m_state.getAH();
            case 5:
                return m_state.getCH();
            case 6:
                return m_state.getDH();
            case 7:
                return m_state.getBH();				
            default:
                throw new RuntimeException();				
        }
    }

    /**
     * Sets the value of the 8bit register whose index is given.
     * @param index   CPU's internal register index.
     * @param value   New value for above register.
     */
    public void setReg8(byte index, byte value) {
        switch (index) {
            case 0:
                m_state.setAL(value);
                break;
            case 1:
                m_state.setCL(value);
                break;
            case 2:
                m_state.setDL(value);
                break;
            case 3:
                m_state.setBL(value);
                break;
            case 4:
                m_state.setAH(value);
                break;
            case 5:
                m_state.setCH(value);
                break;
            case 6:
                m_state.setDH(value);
                break;
            case 7:
                m_state.setBH(value);
                break;
            default:
                throw new RuntimeException();				
        }
    }	

    /**
     * Returns the value of the 16bit register whose index is given.
     * @param index   CPU's internal register index.
     * @return the value of the 16bit register whose index is given.
     */	
    public short getReg16(byte index) {
        switch (index) {
            case 0:
                return m_state.getAX();
            case 1:
                return m_state.getCX();
            case 2:
                return m_state.getDX();
            case 3:
                return m_state.getBX();
            case 4:
                return m_state.getSP();
            case 5:
                return m_state.getBP();
            case 6:
                return m_state.getSI();
            case 7:
                return m_state.getDI();				
            default:
                throw new RuntimeException();				
        }
    }

    /**
     * Sets the value of the 16bit register whose index is given.
     * @param index   CPU's internal register index.
     * @param value   New value for above register.
     */
    public void setReg16(byte index, short value) {
        switch (index) {
            case 0:
                m_state.setAX(value);
                break;
            case 1:
                m_state.setCX(value);
                break;
            case 2:
                m_state.setDX(value);
                break;
            case 3:
                m_state.setBX(value);
                break;
            case 4:
                m_state.setSP(value);
                break;
            case 5:
                m_state.setBP(value);
                break;
            case 6:
                m_state.setSI(value);
                break;
            case 7:
                m_state.setDI(value);
                break;
            default:
                throw new RuntimeException();				
        }
    }

    /**
     * Returns the value of the segment register whose index is given.
     * @param index   CPU's internal register index.
     * @return the value of the segment register whose index is given.
     */
    public short getSeg(byte index) {
        switch (index) {
            case 0:
                return m_state.getES();
            case 1:
                return m_state.getCS();
            case 2:
                return m_state.getSS();
            case 3:
                return m_state.getDS();
            case 4:
                return m_state.getES();
            case 5:
                return m_state.getCS();
            case 6:
                return m_state.getSS();
            case 7:
                return m_state.getDS();				
            default:
                throw new RuntimeException();				
        }
    }

    /**
     * Sets the value of the segment register whose index is given.
     * @param index   CPU's internal register index.
     * @param value   New value for above register.
     */
    public void setSeg(byte index, short value) {
        switch (index) {
            case 0:
                m_state.setES(value);
                break;
            case 1:
                m_state.setCS(value);
                break;
            case 2:
                m_state.setSS(value);
                break;
            case 3:
                m_state.setDS(value);
                break;
            case 4:
                m_state.setES(value);
                break;
            case 5:
                m_state.setCS(value);
                break;
            case 6:
                m_state.setSS(value);
                break;
            case 7:
                m_state.setDS(value);
                break;
            default:
                throw new RuntimeException();				
        }
    }	

    /** Used to access the actual registers */
    private final CpuState m_state;
}
