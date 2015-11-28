/*
 * NewMachineDialog.java
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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import seksen.hardware.Machine;

public class NewMachineDialog extends JFrame implements ActionListener {
	JComboBox cpucb;
	JComboBox addressingcb;
	JCheckBox accesscheckcb;
	JCheckBox testcb;
	private JFileChooser fileDialog;

	public NewMachineDialog() {
		setTitle("New Machine");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container cpane = getContentPane();
		cpane.setLayout(new BoxLayout(cpane, BoxLayout.LINE_AXIS));

		cpucb = new JComboBox(new Object[]{"8086"});
		cpucb.setToolTipText("CPU");
		cpane.add(cpucb);

		addressingcb = new JComboBox(new Object[]{"Normal (20 bits)"});
		addressingcb.setToolTipText("Memory addressing");
		addressingcb.addActionListener(this);
		cpane.add(addressingcb);

		accesscheckcb = new JCheckBox("Access check");
		accesscheckcb.setToolTipText("Memory access check");
		cpane.add(accesscheckcb);

		testcb = new JCheckBox("Test");
		testcb.setToolTipText("Test interpreter");
		cpane.add(testcb);

		JButton button = new JButton("Create");
		button.addActionListener(this);
		cpane.add(button);

		button = new JButton("Load");
		button.addActionListener(this);
		cpane.add(button);

		pack();
		setResizable(false);
		setLocationRelativeTo(null);

        fileDialog = new JFileChooser();
        fileDialog.addChoosableFileFilter(SFileFilter.FF_MAC);
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if( src == addressingcb ) {
			testcb.setEnabled(addressingcb.getSelectedIndex() == 0);
		} else if( e.getActionCommand().equals("Create") ) {
			int type = 0;
			if(accesscheckcb.isSelected()) {
				type |= Machine.ACCESS_CHECK;
			}
			if(testcb.isSelected()) {
				type |= Machine.TEST_INTERPRETER;
			}
			dispose();
			new CpuFrame(new Machine());
		} else if( e.getActionCommand().equals("Load") ) {
			fileDialog.setDialogTitle("Load Machine file");
	        //fileDialog.setFileFilter(SFileFilter.FF_MAC);

	    	int returnVal = fileDialog.showOpenDialog(this);

	        if (returnVal != JFileChooser.APPROVE_OPTION) {
	        	return;
	        }
	        File file = fileDialog.getSelectedFile();

	        Machine mac = null;

			try {
	        	FileInputStream filestream = new FileInputStream(file);
				mac = Machine.loadNewMachine(filestream);
				filestream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			if( mac != null ) {
				dispose();
				new CpuFrame(mac);
			}
		}
	}
}
