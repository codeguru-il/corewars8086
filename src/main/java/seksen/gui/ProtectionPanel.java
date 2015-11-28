/*
 * ProtectionPanel.java
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.MaskFormatter;

import seksen.hardware.memory.MemoryAccessProtection;
import seksen.util.Hex;

public class ProtectionPanel extends JPanel implements ActionListener, ListSelectionListener {
	private CpuFrame frame;
	private JTable table;

	private JFormattedTextField startField;
	private JFormattedTextField sizeField;
	private JCheckBox readCheck;
	private JCheckBox writeCheck;
	private JCheckBox execCheck;
	private JButton setButton;
	private MemoryAccessProtection mempro;

	public ProtectionPanel( CpuFrame frame ) {
		super(new BorderLayout());
		this.frame = frame;

		mempro = (MemoryAccessProtection)
				frame.machine.getDevice(MemoryAccessProtection.class);

		if( mempro == null ) {
			throw new Error("Can't find MemoryAccessProtection device");
		}

		table = new JTable(new ProtectionTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		table.setFont(frame.monoFont);
		add(new JScrollPane(table));

		{
			JPanel uppanel = new JPanel();
			add(BorderLayout.NORTH,uppanel);

			uppanel.add(new JLabel("Start"));
			try {
				startField = new JFormattedTextField(new MaskFormatter("HHHHHHHH"));
				startField.setValue("00000000");
				startField.setFont(frame.monoFont);
				uppanel.add(startField,BorderLayout.WEST);
			} catch (ParseException e) {
			}

			uppanel.add(new JLabel("Size"));
			try {
				sizeField = new JFormattedTextField(new MaskFormatter("HHHHHHHH"));
				sizeField.setValue("00000000");
				sizeField.setFont(frame.monoFont);
				uppanel.add(sizeField,BorderLayout.WEST);
			} catch (ParseException e) {
			}

			uppanel.add(new JLabel("R"));
			readCheck = new JCheckBox();
			uppanel.add(readCheck);

			uppanel.add(new JLabel("W"));
			writeCheck = new JCheckBox();
			uppanel.add(writeCheck);

			uppanel.add(new JLabel("E"));
			execCheck = new JCheckBox();
			uppanel.add(execCheck);

			setButton = new JButton("Set");
			setButton.addActionListener(this);
            setButton.setMargin(new java.awt.Insets(0,10,0,10));
			uppanel.add(setButton);
		}
	}

	public void actionPerformed(ActionEvent event) {
		int s = Integer.parseInt((String) startField.getValue(),16);
		int sz = Integer.parseInt((String) sizeField.getValue(),16);
		int prot = MemoryAccessProtection.PROT_NONE;
		if(readCheck.isSelected()){
			prot |= MemoryAccessProtection.PROT_READ;
		}
		if(writeCheck.isSelected()){
			prot |= MemoryAccessProtection.PROT_WRITE;
		}
		if(execCheck.isSelected()){
			prot |= MemoryAccessProtection.PROT_EXEC;
		}

		mempro.setProtection(frame.machine.newAddress(s), sz, prot);

		update();
	}

	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()) {
			int index = table.getSelectedRow();
			if(index != -1) {
				int values[] = mempro.getRegion(index+1);
				if(values == null || values.length != 3){
					return;
				}
				startField.setValue(Hex.toHexString(values[0], 8));
				sizeField.setValue(Hex.toHexString(values[1], 8));
				readCheck.setSelected((values[2]&MemoryAccessProtection.PROT_READ)!=0);
				writeCheck.setSelected((values[2]&MemoryAccessProtection.PROT_WRITE)!=0);
				execCheck.setSelected((values[2]&MemoryAccessProtection.PROT_EXEC)!=0);
			}
		}
	}

	public void update() {
		((ProtectionTableModel)table.getModel()).fireTableDataChanged();
	}

	class ProtectionTableModel extends AbstractTableModel {

		public String getColumnName(int column) {
			final String columnNames[] = new String[]{"Start","Size","R","W","E"};
			return columnNames[column];
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return mempro.getRegionCount()-1;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			int values[] = mempro.getRegion(rowIndex+1);
			if(values == null || values.length != 3){
				return "";
			}

			if(columnIndex == 0){
				return Hex.toHexString(values[0],8);
			} else if(columnIndex == 1){
				return Hex.toHexString(values[1],8);
			} else if(columnIndex == 2){
				return new Character((values[2] & MemoryAccessProtection.PROT_READ) != 0 ? 'X' : ' ');
			}  else if(columnIndex == 3){
				return new Character((values[2] & MemoryAccessProtection.PROT_WRITE) != 0 ? 'X' : ' ');
			}  else {
				return new Character((values[2] & MemoryAccessProtection.PROT_EXEC) != 0 ? 'X' : ' ');
			}
		}
	}
}
