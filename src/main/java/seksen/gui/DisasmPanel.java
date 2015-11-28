/*
 * DisasmPanel.java
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.MaskFormatter;

import seksen.debug.Symbol;
import seksen.hardware.Address;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.RealModeMemory;
import seksen.util.Hex;
import seksen.util.disasm.Binary;
import seksen.util.disasm.Disasm86;
import seksen.util.disasm.Disassembler;

public class DisasmPanel extends JPanel implements
	PropertyChangeListener, MouseListener, ActionListener {

	static final int DISASMSIZE = 1024;
	static final int AVERAGEINST = 2;
	static final Color BREAKPOINTCOLOR = Color.RED;
	static final Color LABELCOLOR = Color.MAGENTA;
	protected JTable list;
	protected Vector lines;
	protected JFormattedTextField addrField;
	protected JComboBox symbolCombo;
	protected CpuFrame frame;
	protected RealModeMemory memory;
	protected MemBinary binary;
	protected Disassembler disasm;
	protected Address curAddress;
	protected Address gotoAddress;
	protected JPopupMenu popup;
	private boolean settingCombo = false;

	public static class MemBinary implements Binary {

		RealModeMemory memory;
		Address address;

		public MemBinary(RealModeMemory memory, Address address) {
			this.memory = memory;
			this.address = address;
		}

		public int getOffset() {
			return address.getOffset();
		}

		public int getAddress() {
			return address.getLinearAddress();
		}

		public Address getAddr() {
			return address;
		}

		public void setAddress(int address) {
			this.address = this.address.newAddress(address);
		}

		public void setAddr(Address address) {
			this.address = address;
		}

		public void seek(int offset) {
			address.addAddress(offset);
		}

		public byte nextByte() {
			byte b = 0;
			try {
				b = memory.readByte(address);
				address = address.addAddress(1);
			} catch (MemoryException ex) {
			}
			return b;
		}

		public short nextWord() {
			short b = 0;
			try {
				b = memory.readWord(address);
				address = address.addAddress(2);
			} catch (MemoryException ex) {
			}
			return b;
		}
	}

	public DisasmPanel(CpuFrame frame) {
		super(new BorderLayout());

		this.frame = frame;
		memory = frame.machine.memory;
		binary = new MemBinary(memory, frame.machine.newAddress(0, 0));
		disasm = new Disasm86(binary);

		curAddress = frame.machine.newAddress(0);

		{
			popup = new JPopupMenu();

			// Create and add a menu item
			JMenuItem item = new JMenuItem("Jump here");
			item.addActionListener(this);
			popup.add(item);

			item = new JMenuItem("Toggle breakpoint");
			item.addActionListener(this);
			popup.add(item);

			item = new JMenuItem("Goto CS:IP");
			item.addActionListener(this);
			popup.add(item);
		}

		{ // ScrollPane
			lines = new Vector();
			lines.ensureCapacity(DISASMSIZE / AVERAGEINST);

			list = new JTable(new SymbolTableModel());
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setRowSelectionAllowed(true);
			list.setFont(frame.monoFont);
			//list.setVisibleRowCount(row);
			list.addMouseListener(this);
			list.setDefaultRenderer(list.getColumnClass(0), new CellRenderer());

			JScrollPane splitpane = new JScrollPane(list);
			//splitpane.setPreferredSize(new Dimension(450,300));
			add(splitpane);
		}

		{// Address bar
			JPanel panel = new JPanel(new BorderLayout());

			try {
				addrField = new JFormattedTextField(new MaskFormatter("HHHH:HHHH"));
				addrField.addPropertyChangeListener("value", this);
				addrField.setText("0000:0000");
				addrField.setFont(frame.monoFont);
				panel.add(addrField, BorderLayout.WEST);
			} catch (ParseException e) {
			}

			symbolCombo = new JComboBox();
			symbolCombo.setFont(frame.monoFont);
			symbolCombo.addActionListener(this);
			symbolCombo.setPreferredSize(new Dimension(450, 20));
			panel.add(symbolCombo);

			add(panel, BorderLayout.NORTH);
		}

		gotoAddress(frame.machine.newAddress(0, 0));
	}

	public void gotoAddress(Address gotoAddr) {
		if (settingCombo) {
			return;
		}

		gotoAddress = gotoAddr;

		repaint();
	}

	public void updateSymbolList() {
		int len = frame.symbolList.length();
		Symbol symbols[] = new Symbol[len];
		for (int a = 0; a < len; a++) {
			symbols[a] = frame.symbolList.getSymbol(a);
		}
		Arrays.sort(symbols);

		DefaultComboBoxModel symbolComboModel =
			(DefaultComboBoxModel) symbolCombo.getModel();
		symbolComboModel.removeAllElements();
		for (int a = 0; a < len; a++) {
			symbolComboModel.addElement(symbols[a]);
		}
	}

	protected void addlines(int line, Address addr, int size) {
		binary.setAddr(addr);
		int newaddr = addr.getLinearAddress();
		int end = newaddr + size;

		if (lines.size() < line) {
			lines.setSize(line);
		}

		RealModeMemory mem = frame.machine.memory;
		try {
			while (newaddr < end) {
				Symbol symbol = frame.symbolList.getSymbolAt(addr);
				if (symbol != null) {
					ListLine listLine =
						new ListLine(addr, symbol.symbol, "", LABELCOLOR);

					lines.add(line++, listLine);
				}

				addr = binary.getAddr();

				Address addr2 = addr;
				String text = disasm.nextLine();

				newaddr = binary.getAddr().getLinearAddress();
				int sz = newaddr - addr.getLinearAddress();

				if (sz < 0) {
					newaddr += addr.getMaxAddr();
					sz += addr.getMaxAddr();
				}

				boolean dbtext = false;
				Color linecolor = frame.breakpointMan.isExecBP(addr2) ? BREAKPOINTCOLOR : null;

				if (newaddr > end) {
					sz -= newaddr - end;
					newaddr = end;
					text = "db ";
					dbtext = true;
				} else if (sz == 0) {
					sz = 1;
					text = "db ";
					dbtext = true;
				}

				StringBuffer sb = new StringBuffer();

				int a;
				for (a = 0; a < sz; a++) {
					String hex = Hex.toHexString((int) mem.readByte(addr), 2);
					addr = addr.addAddress(1);

					sb.append(hex).append(' ');
					if (dbtext) {
						text += "0" + hex + "h, ";
					}
				}

				ListLine listLine = new ListLine(
					addr2, text, sb.toString(),
					linecolor);

				lines.add(line++, listLine);
			}
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paint(Graphics g) {
		if (gotoAddress != null) {
			addrField.setText(gotoAddress.toString());

			Symbol symbol = frame.symbolList.getSymbol(gotoAddress);
			if (symbol != null) {
				DefaultComboBoxModel symbolComboModel =
					(DefaultComboBoxModel) symbolCombo.getModel();
				int index = symbolComboModel.getIndexOf(symbol);
				if (index > 0) {
					settingCombo = true;
					symbolCombo.setSelectedIndex(index);
					settingCombo = false;
				}
			}

			lines.clear();

			int before = gotoAddress.getLinearAddress();
			int after = gotoAddress.getMaxAddr() - before;
			if (before < DISASMSIZE / 2) {
				after = DISASMSIZE - before;
			} else if (after < DISASMSIZE / 2) {
				before = DISASMSIZE - after;
			} else {
				before = after = DISASMSIZE / 2;
			}

			Address head = gotoAddress.addAddress(-before);
			addlines(0, head, before);
			int index = lines.size();
			addlines(index, gotoAddress, after);

			curAddress = gotoAddress;
			gotoAddress = null;

			((AbstractTableModel) list.getModel()).fireTableDataChanged();

			list.setRowSelectionInterval(index, index);
			list.scrollRectToVisible(list.getCellRect(index, 0, true));
		}

		super.paint(g);
	}

	void update() {
		gotoAddress(gotoAddress != null ? gotoAddress : curAddress);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		gotoAddress(frame.machine.address.parseAddress((String) addrField.getValue()));
	}

	protected Address index2address(int index) {
		Object value = lines.get(index);
		if (!(value instanceof ListLine)) {
			return null;
		}
		ListLine line = (ListLine) value;
		return line.address;
	}

	protected void doubleClick(int index) {
		Object value = lines.get(index);
		if (!(value instanceof ListLine)) {
			return;
		}
		ListLine line = (ListLine) value;
		Address address = index2address(index);
		if (frame.breakpointMan.toggleExecBP(address)) {
			line.background = BREAKPOINTCOLOR;
		} else {
			line.background = null;
		}
		list.repaint();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			int index = list.rowAtPoint(e.getPoint());
			doubleClick(index);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			int index = list.rowAtPoint(e.getPoint());
			list.setRowSelectionInterval(index, index);
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == symbolCombo) {
			Symbol symbol = (Symbol) symbolCombo.getSelectedItem();
			if (symbol != null) {
				gotoAddress(symbol.address);
			}
		} else {
			String action = e.getActionCommand();
			int index = list.getSelectedRow();
			if (index >= 0) {
				if (action.equals("Jump here")) {
					Address address = index2address(index);
					if (address != null) {
						frame.machine.state.setCSIP(address);
						frame.update();
					}
				} else if (action.equals("Toggle breakpoint")) {
					doubleClick(index);
				} else if (action.equals("Goto CS:IP")) {
					gotoAddress(frame.machine.state.getCSIP(frame.machine.address));
				}
			}
		}
	}

	class SymbolTableModel extends AbstractTableModel {

		public String getColumnName(int column) {
			if (column == 0) {
				return "Address";
			} else if (column == 1) {
				return "Code";
			} else {
				return "Hex";
			}
		}

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return lines.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			ListLine line = (ListLine) lines.get(rowIndex);
			if (columnIndex == 0) {
				return line;
			} else if (columnIndex == 1) {
				return line.code;
			} else {
				return line.hex;
			}
		}
	}

	class CellRenderer extends JLabel implements TableCellRenderer {
		// This is the only method defined by ListCellRenderer.
		// We just reconfigure the JLabel each time we're called.
		public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row, int column) {
			Color color = null;
			if (value instanceof ListLine) {
				color = ((ListLine) value).background;
				Address address = ((ListLine) value).address;
				Symbol symbol = frame.symbolList.getSymbol(address);
				if (symbol != null) {
					setToolTipText(symbol.symbol + "@" + symbol.address);
				} else {
					setToolTipText("");
				}
			} else {
				setToolTipText("");
			}

			String s = value.toString();
			setText(s);
			if (isSelected) {
				if (color == null) {
					color = table.getSelectionBackground();
				}
				setBackground(color);
				setForeground(table.getSelectionForeground());
			} else {
				if (color == null) {
					color = table.getBackground();
				}
				setBackground(color);
				setForeground(table.getForeground());
			}

			setEnabled(table.isEnabled());
			setFont(table.getFont());
			setOpaque(true);
			return this;
		}
	}
}

class ListLine {

	Address address;
	String hex;
	String code;
	Color background;

	public ListLine(Address address, String code, String hex, Color background) {
		this.address = address;
		this.code = code;
		this.hex = hex;
		this.background = background;
	}

	public String toString() {
		return address.toString();
	}
}



