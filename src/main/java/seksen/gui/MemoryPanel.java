/*
 * MemoryPanel.java
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
package seksen.gui;

import java.awt.Color;

import seksen.hardware.Address;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.RealModeMemory;
import seksen.hardware.memory.MemoryAccessProtection;
import seksen.util.Hex;

public class MemoryPanel extends DisasmPanel {

	static final Color[] PROTECTIONCOLORS = new Color[]{
		Color.GRAY,Color.RED,Color.BLUE,Color.MAGENTA,
		Color.GREEN,Color.YELLOW,Color.CYAN,Color.WHITE};

	public MemoryPanel( CpuFrame frame ) {
		super(frame);
	}

    @Override
	protected void addlines(int line, Address addr, int size){
		binary.setAddr(addr);
		int newaddr = addr.getLinearAddress();
		int end = newaddr+size;

		if(lines.size()<line){
			lines.setSize(line);
		}

		RealModeMemory mem = frame.machine.memory;
		MemoryAccessProtection resmem = (MemoryAccessProtection)
			frame.machine.getDevice(MemoryAccessProtection.class);

		try {
			while(newaddr<end){
				Address addr2 = addr;

				StringBuffer sb = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();

				int a;
				for(a=0; a<16; a++){
					byte b = mem.readByte(addr);
					String hex = Hex.toHexString((int)b,2);
					addr = addr.addAddress(1);
					if(a==8){
						sb.append(' ');
					}
					sb.append(hex).append(' ');
					if(b>=0x20 && b<='z'){
						sb2.append((char)b);
					} else {
						sb2.append('.');
					}
				}

				newaddr += 16;

				Color color = null;
				if(resmem != null){
					int prot = resmem.getProtection(addr2, 16, MemoryAccessProtection.PROT_ALL);
					color = PROTECTIONCOLORS[prot];
				}

				ListLine listLine = new ListLine(addr2,sb.toString(),sb2.toString(),color);

				lines.add(line++,listLine);
			}
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void gotoAddress(Address gotoAddr) {
		super.gotoAddress( gotoAddr.addOffset(-(gotoAddr.getOffset()&0xf)) );
	}

	@Override
	public void doubleClick(int index){
		Object value = lines.get(index);
        if( !(value instanceof ListLine) ){
        	return;
        }

		MemoryAccessProtection resmem = (MemoryAccessProtection)
			frame.machine.getDevice(MemoryAccessProtection.class);
		if(resmem == null){
			return;
		}

        ListLine line = (ListLine)value;
        Address address = line.address;

        int prot = resmem.getProtection(address, 16, MemoryAccessProtection.PROT_ALL);

        prot ^= 7;
        resmem.setProtection(address, 16, prot);

        line.background =  PROTECTIONCOLORS[prot];
        list.repaint();
	}
}
