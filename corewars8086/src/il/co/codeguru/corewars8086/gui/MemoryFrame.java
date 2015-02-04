package il.co.codeguru.corewars8086.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.utils.Disassembler;
import il.co.codeguru.corewars8086.war.Competition;
import il.co.codeguru.corewars8086.war.CompetitionEventListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MemoryFrame extends JFrame implements CompetitionEventListener {

	private static final long serialVersionUID = 1L;
	ArrayList<JTextField> cells = new ArrayList<JTextField>();
	ArrayList<JLabel> labels = new ArrayList<JLabel>();
	
	Competition comp;
	private JTextArea instructionArea;
	private int last;
	
	public MemoryFrame(Competition c,int address){
		super("Memory viewer - CodeGuru");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		comp = c;
		
		setAlwaysOnTop(true);
		setSize(800,800);
		setVisible(true);
		
		JPanel l = new JPanel(new GridLayout(0,9));
		for (int i = 0; i < 32; i++) {
			JLabel jl = new JLabel();
			jl.setMinimumSize(new Dimension(40, 20));
			labels.add(jl);
			l.add(jl);
			for (int j = 0; j < 8; j++) {
				JTextField t = new JTextField(2);
				cells.add(t);
				l.add(t);
			}
		}
		
		add(l);
		
		
		instructionArea = new JTextArea(); 
		instructionArea.setFont(new Font("Monospaced",Font.PLAIN,15));
		instructionArea.setSize(50, 100);
		instructionArea.setLineWrap(true);
		instructionArea.setWrapStyleWord(true);
		
		JPanel top = new JPanel(new GridLayout(0,2));
		top.add(l);
		top.add(instructionArea);
		
		add(top);
		
		refrash(address);
		
	}

	
	public void refrash(int address) {
		last = address;
		int i = 0;
		for (JLabel la : labels) {
			la.setText(String.format("%08X", address+i++*8).toUpperCase() + ":");
		}
		
		i = 0;
		for (JTextField tf : cells) {
			byte b = comp.getCurrentWar().getMemory().readByte(new RealModeAddress(address + i++));
			tf.setText("" + String.format("%02X", b & 0xFF));
		}
		
		
		byte[] bytes = new byte[30];
		
		for (short k = 0; k < 30; k++) {
			short vs = comp.getCurrentWar().getMemory().readByte(new RealModeAddress(address +k));
			bytes[k] = (byte) vs;
		}
		
		try {
			instructionArea.setText(Disassembler.disassembler(bytes));
		} catch (Exception e) {
			instructionArea.setText(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void onEndRound() {
		refrash(last);
	}


	@Override
	public void onWarStart() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onWarEnd(int reason, String winners) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onRound(int round) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onWarriorBirth(String warriorName) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onWarriorDeath(String warriorName, String reason) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCompetitionStart() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCompetitionEnd() {
		// TODO Auto-generated method stub
		
	}
	
	

}
