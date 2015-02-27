package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.memory.MemoryEventListener;
import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.utils.Unsigned;
import il.co.codeguru.corewars8086.war.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * The main GUI class for core-wars. 
 * The frame includes:
 * <ul>
 * <li> Canvas for showing the memory
 * <li> a list of warrior names
 * <li> messaging area
 * <li> disassembler + cpu state viewer panel
 * <li> start/stop buttons
 * <li> pause/resume and single round buttons
 * <li> speed slider
 * </ul>
 * 
 * @author BS
 */
public class WarFrame extends JFrame
    implements MemoryEventListener,  CompetitionEventListener, MouseAddressRequest{
	private static final long serialVersionUID = 1L;

	/** the canvas which show the core war memory area */
    private Canvas warCanvas;

    /** the message area show misc. information about the current fight */
    private JTextArea messagesArea;
    
    /** the disassembler panel show dissasembled data from the arena */
    private DisassemblerPanel disassemblerPanel;
    
    private int disassemblerHeight;
    
    /** list of warrior names */
    private JList nameList;

    /** Model for the name list */
    private DefaultListModel nameListModel;

    /** Holds the current round number */
    private int nRoundNumber;

    /** A text field showing the current round number */
    private JTextField roundNumber;
    
    /** A text field showing the current seed */
    private JTextField seedField;
    
    private JSlider speedSlider;
    
    /** Buttons used to stop and resume the war */
    private JButton pauseButton, singleRoundButton;
    
    private final Competition competition;

    public WarFrame(Competition competition) {
        super("CodeGuru Extreme - Session Viewer");        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.competition = competition;
        getContentPane().setLayout(new BorderLayout());

        // build widgets
        JPanel mainPanel = new JPanel(new BorderLayout());

        // build war zone (canvas + title)
        JPanel warZone = new JPanel(new GridLayout());
        warZone.setBackground(Color.BLACK);

        JPanel canvasPanel = new JPanel(new BorderLayout());
        canvasPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(169,154,133),3), 
            BorderFactory.createEmptyBorder()));
        canvasPanel.setBackground(Color.BLACK);
        warCanvas = new Canvas();
        warCanvas.addListener(this);
        canvasPanel.add(warCanvas);
        warZone.add(canvasPanel, BorderLayout.CENTER);

        //warZone.add(new JLabel(new ImageIcon("images/warzone.jpg")), BorderLayout.NORTH);
        mainPanel.add(warZone, BorderLayout.CENTER);

        // build info zone (message area + buttons)
        JPanel infoZone = new JPanel(new BorderLayout());
        
        // build additional info zone (message + disassembler area)
        JPanel additionalInfoPanel = new JPanel();
        additionalInfoPanel.setLayout(new BoxLayout(additionalInfoPanel, BoxLayout.Y_AXIS));
        
        disassemblerPanel = new DisassemblerPanel(competition, 32);
        competition.addCompetitionEventListener(disassemblerPanel);
        competition.addMemoryEventLister(disassemblerPanel);
        additionalInfoPanel.add(disassemblerPanel);
        
        disassemblerHeight = -1;
        
        messagesArea = new JTextArea();
        messagesArea.setRows(6);
        messagesArea.setEditable(false);
        messagesArea.setFont(new Font("Tahoma", Font.PLAIN, 12));
        additionalInfoPanel.add(new JScrollPane(messagesArea));
        
        infoZone.add(additionalInfoPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.add(new JLabel("Round:"));
        roundNumber = new JTextField(4);
        roundNumber.setEditable(false);
        mainButtonPanel.add(roundNumber);
        mainButtonPanel.add(Box.createHorizontalStrut(20));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        mainButtonPanel.add(closeButton);
        mainButtonPanel.add(Box.createHorizontalStrut(20));
        mainButtonPanel.add(new JLabel("Speed:"));
        speedSlider = new JSlider(1, Competition.MAXIMUM_SPEED, competition.getSpeed());
        speedSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            	WarFrame.this.competition.setSpeed(speedSlider.getValue());
                if (speedSlider.getValue() == 1)
                	WarFrame.this.pauseWar();
            }
        });
        mainButtonPanel.add(speedSlider);
        mainButtonPanel.add(Box.createHorizontalStrut(20));
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (WarFrame.this.pauseButton.getText() == "Resume")
					WarFrame.this.resumeWar();
				else
					WarFrame.this.pauseWar();
			}
		});
        mainButtonPanel.add(pauseButton);
        mainButtonPanel.add(Box.createHorizontalStrut(20));
        singleRoundButton = new JButton("Single Round");
        singleRoundButton.setEnabled(false);
        singleRoundButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (WarFrame.this.competition.getCurrentWar() == null) return;
				
				WarFrame.this.competition.getCurrentWar().setSingleRoundFlag(true);
				WarFrame.this.competition.getCurrentWar().setPausedFlag(false);
			}
		});
        mainButtonPanel.add(singleRoundButton);
        buttonPanel.add(mainButtonPanel);
        buttonPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        
        JPanel advancedButtonPanel = new JPanel();
        advancedButtonPanel.add(new JLabel("Seed:"));
        seedField = new JTextField(10);
        seedField.setEditable(false);
        advancedButtonPanel.add(seedField);
        advancedButtonPanel.add(Box.createHorizontalStrut(20));
        JButton showDisassembler = new JButton("Disassembler");
        showDisassembler.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disassemblerPanel.toggleDisassembler();
				if ((disassemblerPanel.isEnabled() && !disassemblerPanel.isVisible()) || 
						(!disassemblerPanel.isEnabled() && disassemblerPanel.isVisible()))
				{
					setSize(getWidth(), getHeight() + disassemblerHeight - 2*disassemblerPanel.getHeight());
					disassemblerPanel.setVisible(!disassemblerPanel.isVisible());
				}
			}
		});
        advancedButtonPanel.add(showDisassembler);
        advancedButtonPanel.add(Box.createHorizontalStrut(20));
        JButton showCpuReader = new JButton("CPU State Viewer");
        showCpuReader.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disassemblerPanel.toggleCpuReader();
				if ((disassemblerPanel.isEnabled() && !disassemblerPanel.isVisible()) || 
						(!disassemblerPanel.isEnabled() && disassemblerPanel.isVisible()))
				{
					setSize(getWidth(), getHeight() + disassemblerHeight - 2*disassemblerPanel.getHeight());
					disassemblerPanel.setVisible(!disassemblerPanel.isVisible());
				}
			}
		});
        advancedButtonPanel.add(showCpuReader);
        buttonPanel.add(advancedButtonPanel);
        
        nRoundNumber = 0;
        infoZone.add(buttonPanel, BorderLayout.SOUTH);
        infoZone.setBackground(Color.black);

        // build warrior zone (warrior list + title) 
        JPanel warriorZone = new JPanel(new BorderLayout());
        warriorZone.setBackground(Color.BLACK);
        nameListModel = new DefaultListModel();
        nameList = new JList(nameListModel);
        nameList.setPreferredSize(new Dimension(200,0));
        nameList.setCellRenderer(new NameCellRenderer());
        nameList.setOpaque(false);
        nameList.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(169,154,133),3), 
            BorderFactory.createEmptyBorder(10,10,20,10)));
        nameList.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                disassemblerPanel.setType(list.getSelectedIndex() + 1);
            }
		});
        nameList.repaint();
        warriorZone.add(nameList, BorderLayout.CENTER);
        //warriorZone.add(new JLabel(new ImageIcon("images/warriors.jpg")), BorderLayout.NORTH);
        warriorZone.add(Box.createHorizontalStrut(20), BorderLayout.WEST);
        mainPanel.add(warriorZone, BorderLayout.EAST);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        //getContentPane().add(new JLabel(new ImageIcon("images/title2.png")), BorderLayout.EAST);
        getContentPane().add(infoZone, BorderLayout.SOUTH);
    }
    
    private void pauseWar()
    {
    	if (competition.getCurrentWar() == null)
    	{
    		competition.setPausedFlag(true);
    	}
    	else
    	{
    		competition.getCurrentWar().setPausedFlag(true);
    		singleRoundButton.setEnabled(true);
    	}
    	
		pauseButton.setText("Resume");
    }
    
    private void resumeWar()
    {
    	if (competition.getCurrentWar() == null)
    	{
    		competition.setPausedFlag(false);
    	}
    	else
    	{
    		competition.getCurrentWar().setPausedFlag(false);
    	}
    	
		pauseButton.setText("Pause");
		singleRoundButton.setEnabled(false);
    }

    /** Add a message to the message zone */
    public void addMessage(String message) {
        messagesArea.append(message + "\n");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
            }
        });
    }
    
    /** Add a message to the message zone (with round number) */
    public void addMessage(int round, String message) {
        addMessage("[" + round + "] "+ message);
    }
    
    /** updates the disassembler log zone*/
    

    /** @see MemoryEventListener#onMemoryWrite(RealModeAddress)
     * originally done by kiril */
    public void onMemoryWrite(RealModeAddress address) {
    	int ipInsideArena = address.getLinearAddress() - 0x1000 * 0x10; // arena * paragraph
    	 		
    	if ( address.getLinearAddress() >= War.ARENA_SEGMENT*0x10 && 
    			address.getLinearAddress() < 2*War.ARENA_SEGMENT*0x10 ) //inside arena
    	{
    		warCanvas.paintPixel(
    				Unsigned.unsignedShort(ipInsideArena),
    				(byte) competition.getWarriorRepository().getGroupOrZombieIndicie(
    						competition.getCurrentWar().getWarrior(competition.getCurrentWarrior())),
    				!competition.getCurrentWar().getWarrior(competition.getCurrentWarrior()).isFirst());
    	}
    }

    /** @see CompetitionEventListener#onWarStart(int) */
    public void onWarStart() {
        addMessage("=== Session started ===");
        nameListModel.clear();
        warCanvas.clear();
        nRoundNumber = 0;
        roundNumber.setText(Integer.toString(nRoundNumber));
        roundNumber.repaint();
        
        if (pauseButton.getText() == "Resume")
        {
        	pauseButton.setEnabled(true);
        	singleRoundButton.setEnabled(true);
        	competition.setPausedFlag(false);
        }
        
        seedField.setText(competition.getSeed() + "");
    }

    /** @see CompetitionEventListener#onWarEnd(int, String) */
    public void onWarEnd(int reason, String winners) {
        roundNumber.setText(Integer.toString(nRoundNumber));
        roundNumber.repaint();		

        switch (reason) {
            case SINGLE_WINNER:
                addMessage(nRoundNumber,
                    "Session over: The winner is " + winners + "!");
                break;
            case MAX_ROUND_REACHED:
                addMessage(nRoundNumber,
                    "Maximum round reached: The winners are " + winners + "!");
                break;
            case ABORTED:
                addMessage(nRoundNumber,
                    "Session aborted: The winners are " + winners + "!");
                break;
            default:
                throw new RuntimeException();			
        }
    }	

    /** @see CompetitionEventListener#onRound(int) */
    public void onRound(int round) {
    	if (nRoundNumber == round)
    		return;
    	
        nRoundNumber = round;
        if ((nRoundNumber % 1000) == 0 || disassemblerPanel.isVisible()) {
            roundNumber.setText(Integer.toString(nRoundNumber));
            roundNumber.repaint();
        }
    }
    
    @Override
    public void onEndRound()
    {
    }

    /** @see CompetitionEventListener#onWarriorBirth(String) */
    public void onWarriorBirth(String warriorName) {
        addMessage(nRoundNumber, warriorName + " enters the arena.");
        nameListModel.addElement(new WarriorInfo(warriorName));
    }

    /** @see CompetitionEventListener#onWarriorDeath(String) */
    public void onWarriorDeath(String warriorName, String reason) {
        addMessage(nRoundNumber, warriorName + " died due to " + reason + ".");
		Enumeration<?> namesListElements = nameListModel.elements();
        while(namesListElements.hasMoreElements()) {
            WarriorInfo info = (WarriorInfo) namesListElements.nextElement();
            if (info.name.equals(warriorName)) {
                info.alive = false;
                break;
            }
        }

        // a bit bogus... just to make the list refresh and show the new status.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nameList.repaint();
            }
        });
    }	

    /**
     * A renderer for the names on the warrior list. 
     * Paints each warrior with its color and uses <S>strikeout</S> to show
     * dead warriors.
     */
    class NameCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private static final int FONT_SIZE = 20;

        /**
         * Construct a name cell renderer
         * Set font size to FONT_SIZE.
         */
        public NameCellRenderer() {
           setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
        }

        /**
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            WarriorInfo info = (WarriorInfo)value;
            /*
            float warriorScore = m_warSession.m_scoreBoard.getScore(warriorName);
            warriorScore = (float)((int)(warriorScore * 100)) / 100;
            */
            String text = info.name;// + " (" + warriorScore + ")";
            if (!info.alive) {
                // strike out dead warriors
                text = "<html><S>" + text + "</S></html>";
            }
            setText(text);
            setForeground(warCanvas.getColorForWarrior((byte) competition.getWarriorRepository().getGroupOrZombieIndicie(
					competition.getCurrentWar().getWarrior(index)),
					!competition.getCurrentWar().getWarrior(index).isFirst()));
            setToolTipText(info.alive + "");
            return this;
        }
    }

    public void onCompetitionStart() {
    }

    public void onCompetitionEnd() {
    }	

    class WarriorInfo {
        String name;
        boolean alive;

        public WarriorInfo(String name) {
            this.name= name;
            this.alive = true;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj!=null) && (obj instanceof String) &&
                (((String)obj).equals(name));
        }
    }
    
    @Override
    public void setVisible(boolean setVisible)
    {
    	super.setVisible(setVisible);
    	if (disassemblerHeight == -1)
    	{
    		disassemblerHeight = disassemblerPanel.getHeight();
    		disassemblerPanel.setVisible(false);
    	}
    }
    
    @Override
	public void addressAtMouseLocationRequested(int address) {
    	disassemblerPanel.onArenaClick(address);
	}
}