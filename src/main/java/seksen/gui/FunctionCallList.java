/*
 * FunctionCallList.java
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import seksen.debug.Symbol;
import seksen.hardware.Address;
import seksen.hardware.cpu.CallListener;

public class FunctionCallList extends JFrame implements CallListener, ActionListener {
	private static final int NCOUNTERS = 10000;

	private CpuFrame cpuFrame;
	private Address[] funcAddrs;
	private int[] linearAddrs;
	private int[] callCounters;
	private int ncounters=0;
	private int refresh=0;
	private JTable callTable;
	private CallTableModel callTableModel;

	public FunctionCallList(CpuFrame cpuFrame) {
		super("Function Call Profiler");
		this.cpuFrame = cpuFrame;
		callCounters = new int[NCOUNTERS];
		funcAddrs = new Address[NCOUNTERS];
		linearAddrs = new int[NCOUNTERS];
		cpuFrame.machine.cpu.addCallListener(this);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		{
			Box box = new Box(BoxLayout.X_AXIS);
			JButton button;
			box.add(Box.createHorizontalGlue());
			button = new JButton("Clear");
			button.addActionListener(this);
			box.add(button);
			getContentPane().add(box,BorderLayout.NORTH);
		}

		callTableModel = new CallTableModel();
		callTable = new JTable(callTableModel);
		JScrollPane scrollPane = new JScrollPane(callTable);
		getContentPane().add(scrollPane);

		setSize(500, 500);
		setVisible(true);
	}

	public void dispose() {
		cpuFrame.machine.cpu.removeCallListener(this);
		super.dispose();
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if(command.equals("Clear")){
			ncounters = 0;
		}
	}

	public void callInst(Address from, Address to) {
		int head = 0;
		int end = ncounters;
		int index = ncounters/2;
		int linaddr = to.getLinearAddress();

		while(head<end){
			 int l = linearAddrs[index];
			 if( linaddr < l ){
				 end = index;
				 index = index/2;
			 } else if( linaddr > l ){
				 head = index+1;
				 index = (index+end)/2;
			 } else {
				 break;
			 }
		}
		if(head<end){
			callCounters[index]++;
		} else if(ncounters<NCOUNTERS){
			if(ncounters>head){
				System.arraycopy(callCounters, head, callCounters, head+1, ncounters-head);
				System.arraycopy(funcAddrs, head, funcAddrs, head+1, ncounters-head);
				System.arraycopy(linearAddrs, head, linearAddrs, head+1, ncounters-head);
			}
			callCounters[head] = 1;
			funcAddrs[head] = to;
			linearAddrs[head] = linaddr;
			ncounters++;
		}

		refresh++;
		if((refresh&0xfff) == 0){
			callTableModel.fireTableDataChanged();
		}
	}

	class CallTableModel extends AbstractTableModel {

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return ncounters;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex == 0){
				return funcAddrs[rowIndex];
			} else if(columnIndex == 1){
				Symbol symbol = cpuFrame.symbolList.getSymbol(funcAddrs[rowIndex]);
				if(symbol.address.equals(funcAddrs[rowIndex])){
					return symbol.symbol;
				}
				return "";
			} else {
				return new Integer(callCounters[rowIndex]);
			}
		}

		public String getColumnName(int column) {
			if(column == 0){
				return "Address";
			} else if(column == 1){
				return "Name";
			} else {
				return "Count";
			}
		}
	}
}
