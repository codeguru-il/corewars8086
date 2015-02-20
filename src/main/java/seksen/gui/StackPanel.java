/*
 * StackPanel.java
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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

import javax.swing.AbstractListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.MaskFormatter;

import seksen.hardware.Address;
import seksen.hardware.memory.MemoryException;
import seksen.util.Hex;

public class StackPanel extends JPanel implements PropertyChangeListener {
	private JList list;
	private CpuFrame frame;
	private Address stackAddress;
	private JFormattedTextField addrField;

	public StackPanel(CpuFrame frame) {
		setLayout(new BorderLayout());
		this.frame = frame;

		try {
			addrField = new JFormattedTextField(new MaskFormatter("HHHH:HHHH"));
			addrField.addPropertyChangeListener("value",this);
			addrField.setText("0000:0000");
			addrField.setFont(frame.monoFont);
		} catch (ParseException e) {
		}

    	add(addrField,BorderLayout.NORTH);

    	list = new JList(new StackListModel());
    	list.setFont(frame.monoFont);
        add(new JScrollPane(list));

    	gotoAddress( frame.machine.state.getSSSP(frame.machine.address));
	}

	class StackListModel extends AbstractListModel {
	    public int getSize() {
	    	//return 0x10000 - machine.getState().getSP();
	    	int size = (stackAddress.getSegmentSize()-stackAddress.getOffset())/2;
	   	 	return size > 0x1ff ? 0x1ff : size;
	    }

	    public Object getElementAt(int index) {
	   	 Address addr = stackAddress.addOffset(index*2);
	   	 String text = Hex.toHexString(addr.getOffset(),4) + " ";
	   	 try {
				text += Hex.toHexString(frame.machine.memory.readWord(addr),4);
			} catch (MemoryException e) {
				text += "----";
			}
			return text;
	    }
	}

	public void gotoAddress(Address address) {
		stackAddress = address;
		addrField.setText(address.toString());
		list.repaint();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String txt = (String)addrField.getValue();
		int seg = Integer.parseInt(txt.substring(0, 4),16);
		int off = Integer.parseInt(txt.substring(5, 9),16);
		gotoAddress(frame.machine.newAddress(seg, off));
	}

	public void update() {
		list.repaint();
	}
}
