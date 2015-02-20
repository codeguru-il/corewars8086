/*
 * SymbolPanel.java
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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import seksen.debug.Symbol;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.RealModeMemory;
import seksen.util.Hex;


public class SymbolPanel extends JPanel
{
	protected JTable table;

	protected CpuFrame frame;
	protected RealModeMemory memory;

	public SymbolPanel( CpuFrame frame ) {
		super(new BorderLayout());

		this.frame = frame;
		memory = frame.machine.memory;

		{
			table = new JTable(new SymbolTableModel());
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setFont(frame.monoFont);

			JScrollPane splitpane = new JScrollPane(table);
			add(splitpane);
		}
	}

	public void update() {
		((SymbolTableModel)table.getModel()).fireTableDataChanged();
	}

	class SymbolTableModel extends AbstractTableModel {
		public String getColumnName(int column) {
			if(column == 0){
				return "Address";
			} else if(column == 1){
				return "Name";
			} else {
				return "Value";
			}
		}

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return frame.symbolList.length();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Symbol symbol = frame.symbolList.getSymbol(rowIndex);
			if(columnIndex == 0){
				return symbol.address;
			} else if(columnIndex == 1){
				return symbol.symbol;
			} else {
				int datalen = getDataLength( rowIndex );

				if(datalen > 100){
					datalen = 100;
				}

				byte[] data;
				try {
					data = memory.readMemory(symbol.address, datalen);
				} catch (MemoryException e) {
					return "Memory read exception";
				}

				StringBuffer sb = new StringBuffer();
				if(datalen<8){
					for(int a=0; a<datalen; a++){
						sb.insert(0, Hex.toHexString((int)data[a],2));
					}
					try{
						long l = Long.parseLong(sb.toString(), 16);
						sb.append(" (").append(l).append(')');
					} catch(NumberFormatException e) {
					}
				} else {
					for(int a=0; a<datalen; a++){
						sb.append(Hex.toHexString((int)data[a],2)).append(' ');
					}
				}
				return sb;
			}
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			String value = ((String)aValue);
			int datalen = getDataLength( rowIndex );
			byte[] data = new byte[datalen];
			int valuelen = value.length() & ~1;
			if(valuelen>datalen*2){
				valuelen=datalen*2;
			}
			for(int a=0,b=datalen;a<valuelen;a+=2){
				data[--b] = Byte.parseByte(value.substring(a, a+2), 16);
			}
			try {
				memory.writeMemory(frame.symbolList.getSymbol(rowIndex).address,
						data, 0, datalen);
			} catch (MemoryException e) {
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(columnIndex == 2){
				return getDataLength( rowIndex ) <= 8;
			}
			return false;
		}

		int getDataLength( int rowIndex ) {
			Symbol symbol = frame.symbolList.getSymbol(rowIndex);
			int datalen;
			if(rowIndex+1 < frame.symbolList.length()){
				Symbol nextsymbol = frame.symbolList.getSymbol(rowIndex+1);
				datalen = nextsymbol.address.getLinearAddress() - symbol.address.getLinearAddress();
			} else {
				datalen = symbol.address.getMaxAddr() - symbol.address.getLinearAddress();
			}
			return datalen;
		}
	}
}
