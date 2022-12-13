package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.memory.MemoryEventListener;
import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.utils.Unsigned;
import il.co.codeguru.corewars8086.war.*;

import java.awt.*;
import java.util.Enumeration;

import javax.swing.*;


/**
 * The main GUI class for core-wars. 
 * The frame includes:
 * <ul>
 * <li> Canvas for showing the memory
 * <li> a list of warrior names
 * <li> messaging area
 * <li> start/stop buttons
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

    /** list of warrior names */
    private JList nameList;

    /** Model for the name list */
    private DefaultListModel nameListModel;

    /** Holds the current round number */
    private int nRoundNumber;

    /** A text field showing the current round number */
    private JTextField roundNumber;
    
	// Debugger
	private JLabel addressFiled;
	private JButton btnCpuState;
	private CpuFrame cpuFrame;
	private JButton btnPause;
	private JButton btnSingleRound;
    

    private JSlider speedSlider;

    private final Competition competition;

	private MemoryFrame memoryFrame;

    public WarFrame(final Competition competition) {
        super("CodeGuru Extreme - Session Viewer");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.competition = competition;
        getContentPane().setLayout(new BorderLayout());

        // build widgets
        JPanel mainPanel = new JPanel(new BorderLayout());

        // build war zone (canvas + title)
        JPanel warZone = new JPanel(new BorderLayout());
        warZone.setBackground(Color.BLACK);

        JPanel canvasPanel = new JPanel();
        canvasPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(169,154,133),3), 
            BorderFactory.createEmptyBorder(10,10,20,10)));
        canvasPanel.setBackground(Color.BLACK);
        warCanvas = new Canvas();
        canvasPanel.add(warCanvas);
        warZone.add(canvasPanel, BorderLayout.CENTER);

        //warZone.add(new JLabel(new ImageIcon("images/warzone.jpg")), BorderLayout.NORTH);
        mainPanel.add(warZone, BorderLayout.CENTER);

        // build info zone (message area + buttons)
        JPanel infoZone = new JPanel(new BorderLayout());
        messagesArea = new JTextArea(5, 60);
        messagesArea.setFont(new Font("Tahoma", Font.PLAIN, 12));

        infoZone.add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(new JLabel("Round:"));
        roundNumber = new JTextField(4);
        roundNumber.setEditable(false);
        buttonPanel.add(roundNumber);
        buttonPanel.add(Box.createHorizontalStrut(20));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(new JLabel("Speed:"));
        speedSlider = new JSlider(1,100,competition.getSpeed());
        speedSlider.addChangeListener(e -> {
            WarFrame.this.competition.setSpeed((int) Math.pow(1.2, speedSlider.getValue()) ); //exponential speed slider
        });
        buttonPanel.add(speedSlider);
        nRoundNumber = 0;
        infoZone.add(buttonPanel, BorderLayout.SOUTH);
        infoZone.setBackground(Color.black);
        
		// Debugger
		addressFiled = new JLabel("Click on the arena to see the memory");
		warCanvas.addListener(this);

		btnCpuState = new JButton("View CPU");
		btnCpuState.setEnabled(false);
		btnCpuState.addActionListener(event -> {
            cpuFrame = new CpuFrame(competition);
            WarFrame.this.competition.addCompetitionEventListener(cpuFrame);
        });

		competition.addCompetitionEventListener(this);
		
		btnPause = new JButton("Pause");
		btnPause.setEnabled(false);
		btnPause.addActionListener(event -> {
            if (competition.getCurrentWar().isPaused()) {
                competition.getCurrentWar().resume();
                btnPause.setText("Pause");
                btnSingleRound.setEnabled(false);
            } else {
                competition.getCurrentWar().pause();
                btnPause.setText("Resume");
                btnSingleRound.setEnabled(true);
            }

        });

		btnSingleRound = new JButton("Single Round");
		btnSingleRound.setEnabled(false);
		btnSingleRound.addActionListener(event -> competition.getCurrentWar().runSingleRound());
        
		buttonPanel.add(btnCpuState);
		buttonPanel.add(btnPause);
		buttonPanel.add(btnSingleRound);
		buttonPanel.add(addressFiled);

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

    /** @see MemoryEventListener#onMemoryWrite(RealModeAddress) */
    public void onMemoryWrite(RealModeAddress address) {
		int ipInsideArena = address.getLinearAddress() - 0x1000 *0x10; // arena * paragraph
		
        if ( address.getLinearAddress() >= War.ARENA_SEGMENT*0x10 && address.getLinearAddress() < 2*War.ARENA_SEGMENT*0x10 ) {
        	warCanvas.paintPixel(
        			Unsigned.unsignedShort(ipInsideArena),
        			(byte)competition.getCurrentWarrior());
        }
    }

    /** @see CompetitionEventListener#onWarStart(long) */
    public void onWarStart(long seed) {
        addMessage("=== Session started ===");
        nameListModel.clear();
        warCanvas.clear();
        if (competition.getCurrentWar().isPaused()){
			btnPause.setText("Resume");
			btnSingleRound.setEnabled(true);
        }
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
        nRoundNumber = round;
        if ((nRoundNumber % 1000) == 0) {
            roundNumber.setText(Integer.toString(nRoundNumber));
            roundNumber.repaint();
        }
        btnCpuState.setEnabled(true); //in case we open the window during a match
        btnPause.setEnabled(true);
    }	

    /** @see CompetitionEventListener#onWarriorBirth(String) */
    public void onWarriorBirth(String warriorName) {
        addMessage(nRoundNumber, warriorName + " enters the arena.");
        nameListModel.addElement(new WarriorInfo(warriorName));
    }

    /** @see CompetitionEventListener#onWarriorDeath(String) */
    public void onWarriorDeath(String warriorName, String reason) {
        addMessage(nRoundNumber, warriorName + " died due to " + reason + ".");
        Enumeration namesListElements = nameListModel.elements();
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
            setForeground(warCanvas.getColorForWarrior(index));
            return this;
        }
    }

    public void onCompetitionStart() {
    	btnCpuState.setEnabled(true);
    	btnPause.setEnabled(true);
    }

    public void onCompetitionEnd() {
    	btnCpuState.setEnabled(false);
    	btnPause.setEnabled(false);
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
	public void onEndRound() {
		this.warCanvas.deletePointers();
		for (int i = 0; i < this.competition.getCurrentWar().getNumWarriors(); i++)
			if (this.competition.getCurrentWar().getWarrior(i).isAlive()) {
				short ip = this.competition.getCurrentWar().getWarrior(i).getCpuState().getIP();
				short cs = this.competition.getCurrentWar().getWarrior(i).getCpuState().getCS();
				
				int ipInsideArena = new RealModeAddress(cs, ip).getLinearAddress() - 0x10000;
				
				this.warCanvas.paintPointer((char) ipInsideArena,(byte) i);
			}
	}

	@Override
	public void dispose() {

		// bug fix - event casted while window is being disposed FIXME find a
		// better solution
		this.competition.getCurrentWar().pause();
		try {
			Thread.sleep(300);
		} catch (Exception e) {

		}
		this.competition.removeCompetitionEventListener(this);
		this.competition.removeMemoryEventLister(this);
		this.competition.getCurrentWar().resume();

		try {
			this.cpuFrame.dispose();
		} catch (Exception e) {
		}
		// restoring maximum speed
		competition.getCurrentWar().resume();
		competition.setSpeed(Competition.MAXIMUM_SPEED);
		super.dispose();
	}

	@Override
	public void addressAtMouseLocationRequested(int address) {
		RealModeAddress tmp = new RealModeAddress(War.ARENA_SEGMENT, (short) address);
		byte data = this.competition.getCurrentWar().getMemory().readByte(tmp);

		// Warrior w = this.competition.getCurrentWar().getNumWarriors()

		this.addressFiled.setText(Integer.toHexString(address).toUpperCase()
				+ ": " + String.format("%02X", data).toUpperCase());
		
		if (memoryFrame == null || !memoryFrame.isVisible()) {
			memoryFrame = new MemoryFrame(competition, tmp.getLinearAddress());
			this.competition.addCompetitionEventListener(memoryFrame);
		}
		else
			memoryFrame.refresh(tmp.getLinearAddress());
	}
 
}
