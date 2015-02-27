package il.co.codeguru.corewars8086.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import il.co.codeguru.corewars8086.cpu.CpuState;
import il.co.codeguru.corewars8086.memory.MemoryEventListener;
import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.utils.Disassembler;
import il.co.codeguru.corewars8086.war.Competition;
import il.co.codeguru.corewars8086.war.CompetitionEventListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DisassemblerPanel extends JPanel implements CompetitionEventListener, MemoryEventListener
{
	private static final long serialVersionUID = 1L;
	
	private Competition competition;
	
	private int disassemblerLength;
	
	private RealModeAddress lastAddress;
	
	private boolean runDisassembler, runCpuReader;
	
	ArrayList<short[]> originialSegments;
	
	//The input elements
	private JComboBox<String> type;
	
	private JComboBox<String> subType;
	private JTextField segment;
	
	private JTextField offset;
	
	// in order to make the focus lister more useful
	private String segmentS, offsetS;
	
	//the output elements
	private JTextArea display;
	private CpuPanel cpuPanel;
	
	public DisassemblerPanel(Competition competition, int disassemblerLength)
	{
		runDisassembler = false;
		runCpuReader = false;
		
		this.disassemblerLength = disassemblerLength;
		this.competition = competition;
		
		originialSegments = new ArrayList<short[]>();
		
		this.setLayout(new BorderLayout());
		
		JPanel inputPanel = new JPanel();
		
		// The input type widget
		type = new JComboBox<String>(new String[] {"Arena"});
		type.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int index = ((JComboBox<String>)arg0.getSource()).getSelectedIndex();
				if (index > -1)
					setType(index);
			}
		});
		type.setEnabled(true);
		inputPanel.add(type);
		
		// the subType widget
		subType = new JComboBox<String>(new String[] {"Current CS + IP", "Original ES", "Original SS" });
		subType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateDisassembler();
			}
		});
		subType.setVisible(false);
		subType.setEnabled(true);
		inputPanel.add(subType);
		
		//the segment chooser widget
		segment = new JTextField(6);
		segment.setText("1000");
		segmentS = "1000";
		segment.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				segmentS = segment.getText();
				updateDisassembler();
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		segment.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					segmentS = segment.getText();
					updateDisassembler();
				}
			}
		});
		segment.setEditable(true);
		inputPanel.add(segment);
		
		inputPanel.add(new JLabel(":"));
		//the offset chooser widget
		offset = new JTextField(6);
		offset.setText("0000");
		offsetS = "0000";
		offset.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				offsetS = offset.getText();
				updateDisassembler();
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		offset.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					offsetS = offset.getText();
					updateDisassembler();
				}
			}
		});
		offset.setEditable(true);
		inputPanel.add(offset);
		
		add(inputPanel, BorderLayout.NORTH);
		
		JPanel displayer = new JPanel(new GridLayout(0, 2));
		
		display = new JTextArea();
		display.setRows(6);
		display.setEditable(false);
		display.setFont(new Font("Tahoma", Font.PLAIN, 12));
		display.setBackground(Color.lightGray);
		displayer.add(new JScrollPane(display));
		
		cpuPanel = new CpuPanel();
		cpuPanel.setAllBackground(Color.lightGray);
		displayer.add(cpuPanel);
		
		add(displayer, BorderLayout.SOUTH);
	}
	
	/** Sets the disassembler onto the address that was clicked on the arena */
	public void onArenaClick(int address)
	{
		segment.setText("1000");
		segmentS = "1000";
		setType(0);
		offset.setText(address + "");
		offsetS = address + "";
		updateDisassembler();
	}
	
	/** give 0 for arena, 1, 2, 3.... for warrior index */
	public void setType(int index)
	{
		type.setSelectedIndex(index);
		
		offset.setText("0000");
		offsetS = "0000";
		
		if (index == 0)
		{
			subType.setVisible(false);
			segment.setVisible(true);
		}
		else
		{
			subType.setVisible(true);
			segment.setVisible(false);
		}
		
		updateDisassembler();
		updateCpuReader();
	}
	
	private RealModeAddress getCurrentAddress()
	{
		if (type.getSelectedIndex() == 0) //arena
		{
			try
			{
				return new RealModeAddress((short) Integer.parseInt(segmentS, 16), (short) Integer.parseInt(offsetS, 16));
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "Disassembler settings are not in the right format!");
				return null;
			}
		}
		else //warrior
		{
			RealModeAddress address;
			switch (subType.getSelectedIndex())
			{
				case 0: //cs + ip
					CpuState curWarriorCpuState = competition.getCurrentWar().getWarrior(type.getSelectedIndex() - 1).getCpuState();
					address = new RealModeAddress(curWarriorCpuState.getCS(), curWarriorCpuState.getIP());
					break;
				case 1: //es
					address = new RealModeAddress(originialSegments.get(type.getSelectedIndex() - 1)[0], (short) 0x0000);
					break;
				default: //ss
					address = new RealModeAddress(originialSegments.get(type.getSelectedIndex() - 1)[1], (short) 0x0000);
					break;
			}
			return new RealModeAddress(address.getSegment(), (short) (address.getOffset() + Integer.parseInt(offsetS, 16)));
		}
	}
	
	private void updateDisassembler()
    {
		if (runDisassembler)
		{
			RealModeAddress address = getCurrentAddress();
			
			if (address != null)
			{
				byte[] bytes = new byte[disassemblerLength];
		        for (int i = 0; i < bytes.length; i++)
		          	bytes[i] = competition.getCurrentWar().getMemory().readByte(new RealModeAddress(address.getLinearAddress() + i));
		        
		        display.setText("");
		        for (String s : Disassembler.disassemble(bytes, address))
		        	display.append(s + "\n");
		        
		        display.setCaretPosition(0);
			}
	        
	        lastAddress = address;
		}
    }
	
	public void updateCpuReader()
	{
		if (runCpuReader)
		{
			if (type.getSelectedIndex() > 0)
	        	cpuPanel.updateState(competition.getCurrentWar().getWarrior(type.getSelectedIndex() - 1).getCpuState());
	        else
	        	cpuPanel.updateState(null);
		}
	}

	@Override
	public void onWarStart()
	{
		type.removeAllItems();
		type.addItem("Arena");
		
		subType.setVisible(false);
		
		segment.setText("1000");
		segmentS = "1000";
		
		offset.setText("0000");
		offsetS = "0000";
		
		if (runCpuReader == false)
			cpuPanel.updateState(null);
		
		updateDisassembler();
		updateCpuReader();
	}

	@Override
	public void onWarEnd(int reason, String winners)
	{
	}

	@Override
	public void onRound(int round) {
		if (lastAddress != null && lastAddress.getLinearAddress() != getCurrentAddress().getLinearAddress())
			updateDisassembler();
		
		updateCpuReader();
	}
	
	@Override
	public void onEndRound()
	{
	}

	@Override
	public void onWarriorBirth(String warriorName)
	{
		type.addItem(warriorName);
		CpuState cpuState = competition.getCurrentWar().getWarrior(competition.getCurrentWarrior() - 1).getCpuState();
		originialSegments.add(new short[] {cpuState.getES(), cpuState.getSS() });
	}

	@Override
	public void onWarriorDeath(String warriorName, String reason) {
	}

	@Override
	public void onCompetitionStart() {
	}

	@Override
	public void onCompetitionEnd() {
	}

	@Override
	public void onMemoryWrite(RealModeAddress address) {
		RealModeAddress curAddress = null;
		if (runDisassembler)
			curAddress = getCurrentAddress();
    	if (curAddress != null && address.getLinearAddress() >= curAddress.getLinearAddress() &&
    			address.getLinearAddress() < curAddress.getLinearAddress() + disassemblerLength)
    		updateDisassembler();
		
	}
	
	public void toggleDisassembler()
	{
		runDisassembler = !runDisassembler;
		
		display.setBackground(runDisassembler ? Color.white : Color.lightGray);
		if (competition.getCurrentWar() != null)
			updateDisassembler();
	}
	
	public void toggleCpuReader()
	{
		runCpuReader = !runCpuReader;
		
		cpuPanel.setAllBackground(runCpuReader ? Color.white : Color.lightGray);
		if (competition.getCurrentWar() != null)
			updateCpuReader();
	}
	
	public boolean isEnabled()
	{
		return runCpuReader || runDisassembler;
	}
}