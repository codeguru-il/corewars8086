/*
 * CommandPrompt.java
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import seksen.debug.Symbol;
import seksen.hardware.Address;
import seksen.hardware.cpu.CpuState;

public class CommandPrompt extends JPanel implements ActionListener {
	private CpuFrame frame;
	private JComboBox commands;
	private JTextArea output;
	private TextWriter writer;

	public CommandPrompt( CpuFrame frame ) {
		super(new BorderLayout());
		this.frame = frame;

		commands = new JComboBox();
		commands.setFont(frame.monoFont);
		commands.addActionListener(this);
		commands.setPreferredSize(new Dimension(450,20));
		commands.setEditable(true);
		add(commands,BorderLayout.NORTH);

		output = new JTextArea();
		output.setEditable(false);
		output.setLineWrap(true);
		output.setFont(frame.monoFont);
		add(new JScrollPane(output,
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		writer = new TextWriter(output);

		printHelp();
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("comboBoxEdited"))
		{
			String command = (String) commands.getSelectedItem();
			if(command.length()==0){
				return;
			}
			commands.setSelectedItem("");

			execute(command);

			commands.removeItem(command);
			commands.insertItemAt(command,0);

			if(commands.getItemCount() == 21){
				commands.removeItemAt(20);
			}
		}
	}

	public void execute(String command){
		println(command);

		String args[] = command.split(" ");

		try{
		if(args[0].length() == 2 && args[0].charAt(0) == 'b')
		{
			char c = args[0].charAt(1);
			if(args.length == 2){
				Address address = frame.machine.address.parseAddress(args[1]);
				if(address != null){
                    boolean setbp;
					if(c=='e') {
						setbp = frame.breakpointMan.toggleExecBP(address);
					} else if(c=='r') {
						setbp = frame.breakpointMan.toggleReadBP(address);
					} else {
						setbp = frame.breakpointMan.toggleWriteBP(address);
					}
                    println((setbp?"Setted":"Cleared")+" breakpoint at "+address);
				}
			} else {
				Iterator iterator;
				if(c=='e') {
					iterator = frame.breakpointMan.getExecBPsIterator();
				} else if(c=='r') {
					iterator = frame.breakpointMan.getReadBPsIterator();
				} else {
					iterator = frame.breakpointMan.getWriteBPsIterator();
				}

				while(iterator.hasNext()){
					println(iterator.next().toString());
				}
			}
		}
		else if(args[0].length() == 2 && args[0].charAt(0) == 'w' && args.length == 3 )
		{
			Address address = frame.machine.address.parseAddress(args[1]);
			int value = (int) Long.parseLong(args[2],16);

			switch( args[0].charAt(1) ) {
				case 'b': frame.machine.memory.writeByte(address, (byte)value); break;
				case 'w': frame.machine.memory.writeWord(address, (short)value); break;
				case 'd': frame.machine.memory.writeDWord(address, value); break;
			}
		}
		else if(args[0].equals("goto") && args.length == 2) {
			Symbol symbol = frame.symbolList.getSymbol(args[1]);
			Address address;
			if(symbol != null){
				address = symbol.address;
			} else {
				address = frame.machine.address.parseAddress(args[1]);
			}
			frame.gotoAddress(address);
		} else if(args[0].equals("jump") && args.length == 2) {
			Symbol symbol = frame.symbolList.getSymbol(args[1]);
			Address address;
			if(symbol != null){
				address = symbol.address;
			} else {
				address = frame.machine.address.parseAddress(args[1]);
			}
			CpuState state = frame.machine.state;
			state.setCS(address.getSegment());
			state.setIP(address.getOffset());
		} else {
			if(!args[0].equals("help")){
				println("Unknown command");
			}
			printHelp();
		}
		} catch (Exception e) {
			println(e.toString());
		}
	}

	void printHelp()
	{
		println("Commands:");
		println("be -	exec-breakpoint");
		println("br -	read-breakpoint");
		println("bw -	write-breakpoint");
		println("wb/ww/wd address hex_int - write byte/word/double-word to memory");
		println("goto -	go to a symbol or an address");
		println("jump -	jump to a symbol or an address");
	}

	void println(String str){
		try {
			writer.write(str);
			writer.write("\n");
		} catch (IOException e) {
		}
	}
}
