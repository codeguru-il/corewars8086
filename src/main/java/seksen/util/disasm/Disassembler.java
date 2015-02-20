/*
 * Disassembler.java
 *
 * Copyright (C) 2006 - 2008 Erdem Güven <zuencap@users.sourceforge.net>.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package seksen.util.disasm;

import seksen.util.Hex;

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
        return "db " + Hex.toHexString(opcode,2);
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
