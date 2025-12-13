package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.gui.effects.*;
import il.co.codeguru.corewars8086.memory.MemoryEventListener;
import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.utils.Unsigned;
import il.co.codeguru.corewars8086.war.*;

import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
    
    private JCheckBox enableEffectsCheckBox;

    private final Competition competition;

	private MemoryFrame memoryFrame;
	
	private GameEventDetector eventDetector;
	private Map<String, Integer> warriorNameToIndex;
	private Map<String, Long> recentDeathEvents; // Track recently triggered death events to prevent duplicates

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
        
        // Initialize event detector
        eventDetector = null;
        warriorNameToIndex = new HashMap<>();
        recentDeathEvents = new HashMap<>();

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
        buttonPanel.add(Box.createHorizontalStrut(20));
        enableEffectsCheckBox = new JCheckBox("Enable Effects", true);
        enableEffectsCheckBox.setForeground(Color.BLACK);
        enableEffectsCheckBox.setOpaque(false);
        buttonPanel.add(enableEffectsCheckBox);
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
        	
        	// Detect special events
        	if (eventDetector != null && competition.getCurrentWarrior() >= 0) {
        		GameEventDetector.GameEvent event = eventDetector.detectMemoryWrite(
        			address, 
        			competition.getCurrentWarrior()
        		);
        		
        		if (event != null) {
        			triggerEffectForEvent(event);
        		}
        	}
        }
    }
    
    /**
     * Triggers visual effects based on detected game events.
     */
    private void triggerEffectForEvent(GameEventDetector.GameEvent event) {
        // Check if effects are enabled
        if (!enableEffectsCheckBox.isSelected()) {
            return;
        }
        
        int[] screenPos = GameEventDetector.arenaOffsetToScreen(
            event.arenaOffset,
            Canvas.BOARD_SIZE,
            Canvas.DOT_SIZE
        );
        int x = screenPos[0];
        int y = screenPos[1];
        
        Color warriorColor = warCanvas.getColorForWarrior(
            event.warriorIndex >= 0 ? event.warriorIndex : 0
        );
        
        int canvasWidth = Canvas.BOARD_SIZE * Canvas.DOT_SIZE;
        int canvasHeight = Canvas.BOARD_SIZE * Canvas.DOT_SIZE;
        
        switch (event.type) {
            case BOMB_USED:
                // Explosion + particles
                warCanvas.addVisualEffect(new ExplosionEffect(x, y, 30, Color.CYAN, 800));
                warCanvas.addVisualEffect(new ParticleEffect(x, y, Color.ORANGE, 20, 1000));
                break;
                
            case ZOMBIE_CAPTURED:
                // Get capturing warrior's info from the event
                String captorName = "Unknown";
                Color captorColor = Color.YELLOW;
                if (competition.getCurrentWar() != null && event.captorIndex >= 0) {
                    Warrior captor = competition.getCurrentWar().getWarrior(event.captorIndex);
                    if (captor != null) {
                        captorName = captor.getName();
                        captorColor = warCanvas.getColorForWarrior(event.captorIndex);
                    }
                }
                
                // BIG celebration for zombie capture!
                // Multiple fireworks bursts
                warCanvas.addVisualEffect(new FireworksEffect(x, y, captorColor, 5, 2000));
                warCanvas.addVisualEffect(new FireworksEffect(x - 20, y - 15, captorColor, 4, 1800));
                warCanvas.addVisualEffect(new FireworksEffect(x + 20, y - 15, captorColor, 4, 1800));
                
                // Particle explosion
                warCanvas.addVisualEffect(new ParticleEffect(x, y, captorColor, 25, 1500));
                
                // Pulsing glow
                warCanvas.addVisualEffect(new GlowEffect(x, y, 50, captorColor, 5, 1500));
                
                // Big bold floating text - goes all the way up
                warCanvas.addVisualEffect(new FloatingTextEffect(
                    "ZOMBIE CAPTURED!", x, y - 20, new Color(255, 215, 0),
                    new Font("Arial", Font.BOLD, 28), 250, 10000,
                    canvasWidth, canvasHeight
                ));
                warCanvas.addVisualEffect(new FloatingTextEffect(
                    captorName + " controls " + event.warriorName, x, y + 10, captorColor,
                    new Font("Arial", Font.BOLD, 18), 220, 10000,
                    canvasWidth, canvasHeight
                ));
                break;
                
            case SURVIVOR_OVERWRITTEN:
                // Effect removed
                break;
                
            case WARRIOR_DEATH:
                // Prevent duplicate death events (within 2 seconds)
                String deathKey = event.warriorName + "_death";
                long currentTime = System.currentTimeMillis();
                Long lastDeathTime = recentDeathEvents.get(deathKey);
                if (lastDeathTime != null && (currentTime - lastDeathTime) < 2000) {
                    // Skip duplicate death event
                    return;
                }
                recentDeathEvents.put(deathKey, currentTime);
                
                // Check if dead warrior is a zombie
                boolean isZombie = false;
                if (competition.getCurrentWar() != null && event.warriorIndex >= 0) {
                    Warrior deadWarrior = competition.getCurrentWar().getWarrior(event.warriorIndex);
                    if (deadWarrior != null && deadWarrior.isZombie()) {
                        isZombie = true;
                    }
                }
                
                // Glow blink effect
                warCanvas.addVisualEffect(new GlowEffect(x, y, 35, warriorColor, 4, 1000));
                
                // Permanent grave marker
                warCanvas.addVisualEffect(new GraveEffect(x, y, event.warriorName, warriorColor, isZombie));
                
                if (isZombie) {
                    // Zombie killed - show specific message (bigger, longer, floats up)
                    warCanvas.addVisualEffect(new FloatingTextEffect(
                        event.warriorName + " DESTROYED!", x, y, new Color(150, 150, 150),
                        new Font("Arial", Font.BOLD, 20), 200, 8000,
                        canvasWidth, canvasHeight
                    ));
                } else {
                    // Regular warrior death (bigger, longer, floats up)
                    warCanvas.addVisualEffect(new FloatingTextEffect(
                        event.message, x, y, warriorColor,
                        new Font("Arial", Font.BOLD, 24), 250, 10000,
                        canvasWidth, canvasHeight
                    ));
                }
                break;
                
            case WARRIOR_BIRTH:
                // Entrance effect removed - no visual effects
                break;
        }
    }

    /** @see CompetitionEventListener#onWarStart(long) */
    public void onWarStart(long seed) {
        addMessage("=== Session started ===");
        nameListModel.clear();
        warCanvas.clear();
        warCanvas.clearEffects();
        recentDeathEvents.clear(); // Clear recent death events for new war
        
        // Initialize event detector
        if (competition.getCurrentWar() != null) {
            eventDetector = new GameEventDetector(competition.getCurrentWar());
            eventDetector.updateWarriorRegions();
            warriorNameToIndex.clear();
            
            // Build name to index mapping
            for (int i = 0; i < competition.getCurrentWar().getNumWarriors(); i++) {
                Warrior warrior = competition.getCurrentWar().getWarrior(i);
                if (warrior != null) {
                    warriorNameToIndex.put(warrior.getName(), i);
                }
            }
        }
        
        if (competition.getCurrentWar().isPaused()){
			btnPause.setText("Resume");
			btnSingleRound.setEnabled(true);
        }
    }

    /** @see CompetitionEventListener#onWarEnd(int, String) */
    public void onWarEnd(int reason, String winners) {
        roundNumber.setText(Integer.toString(nRoundNumber));
        roundNumber.repaint();		

        // War end effect
        int centerX = Canvas.BOARD_SIZE * Canvas.DOT_SIZE / 2;
        int centerY = Canvas.BOARD_SIZE * Canvas.DOT_SIZE / 2;
        int canvasWidth = Canvas.BOARD_SIZE * Canvas.DOT_SIZE;
        int canvasHeight = Canvas.BOARD_SIZE * Canvas.DOT_SIZE;
        String victoryText;
        Color winnerColor = Color.YELLOW;
        
        // Try to get winner's color
        if (competition.getCurrentWar() != null && winners != null && !winners.isEmpty()) {
            String[] winnerNames = winners.split(",");
            if (winnerNames.length > 0) {
                String firstWinner = winnerNames[0].trim();
                // Find winner's index
                for (int i = 0; i < competition.getCurrentWar().getNumWarriors(); i++) {
                    Warrior w = competition.getCurrentWar().getWarrior(i);
                    if (w != null && w.getName().equals(firstWinner)) {
                        winnerColor = warCanvas.getColorForWarrior(i);
                        break;
                    }
                }
            }
        }
        
        switch (reason) {
            case SINGLE_WINNER:
                addMessage(nRoundNumber,
                    "Session over: The winner is " + winners + "!");
                victoryText = "WINNER: " + winners + "!";
                
                if (enableEffectsCheckBox.isSelected()) {
                    // Dramatic winner announcement with multiple effects
                    // Gold color for winner effects
                    Color goldColor = new Color(255, 215, 0);
                    
                    // Celebration particles from center
                    warCanvas.addVisualEffect(new ParticleEffect(
                        centerX, centerY, goldColor, 30, 2000
                    ));
                    
                    // Multiple pulse rings
                    warCanvas.addVisualEffect(new PulseEffect(
                        centerX, centerY, 100, goldColor, 5, 2000
                    ));
                    
                    // Large prominent winner text (bigger, stays longer)
                    warCanvas.addVisualEffect(new FloatingTextEffect(
                        "VICTORY!", centerX, centerY - 40, goldColor,
                        new Font("Arial", Font.BOLD, 42), 100, 12000,
                        canvasWidth, canvasHeight
                    ));
                    
                    // Winner name text (bigger, stays longer)
                    warCanvas.addVisualEffect(new FloatingTextEffect(
                        victoryText, centerX, centerY + 30, winnerColor,
                        new Font("Arial", Font.BOLD, 38), 80, 12000,
                        canvasWidth, canvasHeight
                    ));
                }
                break;
                
            case MAX_ROUND_REACHED:
                addMessage(nRoundNumber,
                    "Maximum round reached: The winners are " + winners + "!");
                victoryText = "Winners: " + winners;
                
                if (enableEffectsCheckBox.isSelected()) {
                    // Less dramatic but still celebratory
                    warCanvas.addVisualEffect(new ParticleEffect(
                        centerX, centerY, new Color(150, 150, 255), 20, 1500
                    ));
                    
                    warCanvas.addVisualEffect(new FloatingTextEffect(
                        "TIME UP", centerX, centerY - 20, new Color(200, 200, 255),
                        new Font("Arial", Font.BOLD, 36), 80, 10000,
                        canvasWidth, canvasHeight
                    ));
                    
                    warCanvas.addVisualEffect(new FloatingTextEffect(
                        victoryText, centerX, centerY + 30, winnerColor,
                        new Font("Arial", Font.BOLD, 26), 60, 10000,
                        canvasWidth, canvasHeight
                    ));
                }
                break;
                
            case ABORTED:
                addMessage(nRoundNumber,
                    "Session aborted: The winners are " + winners + "!");
                victoryText = "Aborted";
                break;
            default:
                throw new RuntimeException();			
        }
    }	

    /** @see CompetitionEventListener#onRound(int) */
    public void onRound(int round) {
        nRoundNumber = round;
        
        // Update event detector with current round
        // This enables bomb detection after a grace period to avoid false positives
        // from rapid round execution in early game
        if (eventDetector != null) {
            eventDetector.onRound(round);
            
            // Check for zombie capture (zombie executing code written by another warrior)
            if (enableEffectsCheckBox.isSelected()) {
                GameEventDetector.GameEvent zombieEvent = eventDetector.checkZombieCapture();
                if (zombieEvent != null) {
                    triggerEffectForEvent(zombieEvent);
                }
            }
        }
        
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
        
        // Update event detector
        if (eventDetector != null) {
            eventDetector.updateWarriorRegions();
        }
        
        // Trigger birth effect
        if (eventDetector != null) {
            Integer warriorIndex = warriorNameToIndex.get(warriorName);
            if (warriorIndex == null) {
                // Find index by name
                if (competition.getCurrentWar() != null) {
                    for (int i = 0; i < competition.getCurrentWar().getNumWarriors(); i++) {
                        Warrior w = competition.getCurrentWar().getWarrior(i);
                        if (w != null && w.getName().equals(warriorName)) {
                            warriorIndex = i;
                            warriorNameToIndex.put(warriorName, i);
                            break;
                        }
                    }
                }
            }
            
            if (warriorIndex != null) {
                GameEventDetector.GameEvent event = eventDetector.createBirthEvent(
                    warriorName, warriorIndex
                );
                triggerEffectForEvent(event);
            }
        }
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
        
        // Trigger death effect only once - check if warrior is already dead to avoid duplicates
        if (eventDetector != null && competition.getCurrentWar() != null) {
            Integer warriorIndex = warriorNameToIndex.get(warriorName);
            if (warriorIndex == null) {
                // Find index by name
                for (int i = 0; i < competition.getCurrentWar().getNumWarriors(); i++) {
                    Warrior w = competition.getCurrentWar().getWarrior(i);
                    if (w != null && w.getName().equals(warriorName)) {
                        warriorIndex = i;
                        warriorNameToIndex.put(warriorName, i);
                        break;
                    }
                }
            }
            
            // Only trigger death effect if warrior was alive (to prevent duplicates)
            if (warriorIndex != null) {
                Warrior warrior = competition.getCurrentWar().getWarrior(warriorIndex);
                // Check if this is a real death event (warrior should be dead now, but we check to avoid duplicates)
                // We'll use a simple approach: only trigger if we haven't already processed this death
                // The event detector will handle checking if warrior is actually dead
                GameEventDetector.GameEvent event = eventDetector.createDeathEvent(
                    warriorName, warriorIndex
                );
                triggerEffectForEvent(event);
            }
        }
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
		
		// Update event detector warrior regions in case they've changed
		if (eventDetector != null && competition.getCurrentWar() != null) {
			eventDetector.updateWarriorRegions();
		}
		
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
