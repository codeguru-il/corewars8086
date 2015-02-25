package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.war.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

/**
 * @author BS
 */
public class CompetitionWindow extends JFrame
    implements ScoreEventListener, ActionListener, CompetitionEventListener {
	private static final long serialVersionUID = 1L;
	
	private Competition competition;
    private ColumnGraph columnGraph;

    // widgets
    private JButton showAdvancedButton;
    private JButton runWarButton;
    private JButton showBattleButton;
    private JLabel warCounterDisplay;
    private JTextField battlesPerGroupField;
    private JTextField warriorsPerGroupField;
    private WarFrame battleFrame;
    
    // advanced panel
    private JPanel advancedPanel;
    private JCheckBox tillEndCheckbox;
    private JSlider speedSlider;
    private JTextField groupToRunField;
    private JTextField seedField;
    
    private int advancedHeight;
    
    private int warCounter;
    private int totalWars;
    private Thread warThread;
    private boolean showBattleFrame;
	private boolean competitionRunning;

    public CompetitionWindow() throws IOException {
        super("CodeGuru Extreme - Competition Viewer");
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        competition = new Competition(false);
        competition.addCompetitionEventListener(this);
        WarriorRepository warriorRepository = competition
                        .getWarriorRepository();
        warriorRepository.addScoreEventListener(this);
        columnGraph = new ColumnGraph(warriorRepository.getGroupNames());
        getContentPane().add(columnGraph, BorderLayout.CENTER);
        // -------------
        JPanel controlArea = new JPanel();
        controlArea.setLayout(new BoxLayout(controlArea, BoxLayout.Y_AXIS));
        // -------------- Button Panel
        JPanel buttonPanel = new JPanel();
        showAdvancedButton = new JButton("Advanced Options");
        showAdvancedButton.addActionListener(this);
        buttonPanel.add(showAdvancedButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        runWarButton = new JButton("<html><font color=red>Start!</font></html>");
        runWarButton.addActionListener(this);
        buttonPanel.add(runWarButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        showBattleButton = new JButton("Show session");
        showBattleButton.addActionListener(this);
        buttonPanel.add(showBattleButton);
        controlArea.add(buttonPanel);
        // -------------
        controlArea.add(new JSeparator(JSeparator.HORIZONTAL));
        // ------------- Counter panel, for better graphics
        JPanel counterPanel = new JPanel();
        counterPanel.setLayout(new FlowLayout());
        warCounterDisplay = new JLabel("No sessions were run.");
        counterPanel.add(warCounterDisplay);
        controlArea.add(counterPanel);
        // -------------
        controlArea.add(new JSeparator(JSeparator.HORIZONTAL));
        // ------------ Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(new JLabel("Survivor groups per session:"));
        
        // If total number of teams is more then four, make it the default number
		int numberOfGropus = Math.min(4,
            competition.getWarriorRepository().getNumberOfGroups());
		
		warriorsPerGroupField = new JTextField(String.format("%d", numberOfGropus), 3);
		controlPanel.add(warriorsPerGroupField);
		controlPanel.add(new JLabel("Sessions per groups combination:"));
		battlesPerGroupField = new JTextField("10", 4);
		controlPanel.add(battlesPerGroupField);
        controlArea.add(controlPanel);
        // -------------
        controlArea.add(new JSeparator(JSeparator.HORIZONTAL));
        // ------------- Advanced options panel
        advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
        
        tillEndCheckbox = new JCheckBox("Always run 200,000 rounds", competition.getTillEnd());
        tillEndCheckbox.addActionListener(this);
        tillEndCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
        advancedPanel.add(tillEndCheckbox);
        
        JPanel speedPanel = new JPanel();
        speedPanel.add(new JLabel("Default speed:"));
        speedSlider = new JSlider(1, Competition.MAXIMUM_SPEED, Competition.MAXIMUM_SPEED * 9 / 10);
        speedPanel.add(speedSlider);
        advancedPanel.add(speedPanel);
        
        JPanel groupToRunPanel = new JPanel();
        groupToRunPanel.add(new JLabel("Only with specific group:"));
        groupToRunField = new JTextField(10);
        groupToRunPanel.add(groupToRunField);
        groupToRunPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        advancedPanel.add(groupToRunPanel);
        
        JPanel seedPanel = new JPanel(new FlowLayout());
        seedPanel.add(new JLabel("Custom seed (a number):"));
        seedField = new JTextField(10);
        seedPanel.add(seedField);
        seedPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        advancedPanel.add(seedPanel);
        
        controlArea.add(advancedPanel);
        // -------------
        getContentPane().add(controlArea, BorderLayout.SOUTH);
        
        pack();
        advancedHeight = advancedPanel.getHeight();
        advancedPanel.setVisible(false);
        
        addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {}
            public void windowClosing(WindowEvent e) {
                if (warThread != null)
                    competition.setAbort();
                    
                if (battleFrame != null)
                	battleFrame.dispose();
            }

            public void windowClosed(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowActivated(WindowEvent e) {}
            public void windowDeactivated(WindowEvent e) {}
        });
    }
    
    /**
     * Starts a new war.
     * @return whether or not a new war was started.
     */
    public boolean runWar() {
        try {
        	if (!seedField.getText().equals(""))
        		Long.parseLong(seedField.getText()); //to catch configuration problem early on
        	final int battlesPerGroup = Integer.parseInt(
                battlesPerGroupField.getText().trim());
            final int warriorsPerGroup = Integer.parseInt(
                warriorsPerGroupField.getText().trim());
            if (competition.getWarriorRepository().getNumberOfGroups() < warriorsPerGroup) {
                JOptionPane.showMessageDialog(this,
                    "Not enough survivors (got " +
                    competition.getWarriorRepository().getNumberOfGroups() +
                    " but " + warriorsPerGroup + " are needed)");
                return false;
            }
            warThread = new Thread("CompetitionThread") {
                @Override
                public void run() {
                    try {
                    	if (!seedField.getText().equals("")) competition.setSeed(Long.parseLong(seedField.getText()));
                        competition.runAndSaveCompetition(battlesPerGroup, warriorsPerGroup, groupToRunField.getText().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
            };
            if (!competitionRunning) {
            	warThread.start();
            	return true;
            }
        } catch (NumberFormatException e2) {
            JOptionPane.showMessageDialog(this, "Error in configuration");
        }
        return false;
    }

    public void scoreChanged(String name, float addedValue, int groupIndex, int subIndex) {
        columnGraph.addToValue(groupIndex, subIndex, addedValue);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == runWarButton) {
            if (runWarButton.getText().trim().equals("<html><font color=red>Start!</font></html>"))
            {
            	if (runWar())
            	{
            		competitionRunning = true;
    				runWarButton.setText("<html><font color=red>Stop!</font></html>");
            	}
            }
            else if (runWarButton.getText().trim().equals("<html><font color=red>Stop!</font></html>"))
            {
            	competition.setAbort();
            	runWarButton.setEnabled(false);
            }
        } else if (e.getSource() == showBattleButton) {
            if (battleFrame == null) {
            	showBattleButton.setEnabled(false);
                if (warThread == null) {
                    // war hasn't started yet
                    showBattleRoom();
                } else {
                    // show the battle frame when the next battle starts
                    showBattleFrame = true;
                }
            }
        } else if (e.getSource() == showAdvancedButton) {
        	setSize(getWidth(), getHeight() + advancedHeight - 2*advancedPanel.getHeight());
        	advancedPanel.setVisible(!advancedPanel.isVisible());
        } else if (e.getSource() == tillEndCheckbox) {
        	competition.setTillEnd(tillEndCheckbox.isSelected());
        }
    }

    public void onWarStart() {
        if (showBattleFrame == true) {
            showBattleRoom();
            showBattleFrame = false;
        }
    }

    private void showBattleRoom() {
        competition.setSpeed(speedSlider.getValue());
        battleFrame = new WarFrame(competition);
        battleFrame.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
            }

            public void windowClosed(WindowEvent e) {
                //System.out.println("BattleFrame=null");
                battleFrame = null;
                competition.setSpeed(Competition.MAXIMUM_SPEED);
                if (competition.getCurrentWar() != null)
                	competition.getCurrentWar().setPausedFlag(false);
                competition.setPausedFlag(false);
                showBattleButton.setEnabled(true);
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }
        });
        
        competition.addMemoryEventLister(battleFrame);
        competition.addCompetitionEventListener(battleFrame);
        
        Rectangle battleFrameRect = new Rectangle(0, getY(), 754, 725);
        Rectangle screen = getGraphicsConfiguration().getBounds(); //for multiple monitors
        
        if (getX() + getWidth() <= screen.getX() + screen.getWidth()
        		- battleFrameRect.width)
        {
        	battleFrameRect.x = getX() + getWidth();
        }
        else if (screen.getX() + screen.getWidth() - battleFrameRect.width
        		- getWidth() >= screen.getX())
        {
        	setLocation((int) (screen.getX() + screen.getWidth() - battleFrameRect.width
            		- getWidth()), getY());
        	battleFrameRect.x = getX() + getWidth();
        }
        else
        {
        	setLocation((int)screen.getX(), getY());
        	battleFrameRect.x = getWidth();
        }
        
        battleFrame.setBounds(battleFrameRect);
        battleFrame.setVisible(true);
    }

    public void onWarEnd(int reason, String winners) {
        warCounter++;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                warCounterDisplay.setText("Sessions so far:" + warCounter +
                    " (out of " + totalWars + ")");
            };
        });
    }

    public void onRound(int round) {
    }

    public void onWarriorBirth(String warriorName) {
    }

    public void onWarriorDeath(String warriorName, String reason) {
    }

    public void onCompetitionStart() {
        totalWars = competition.getTotalNumberOfWars() + warCounter;
        groupToRunField.setEnabled(false);
        seedField.setEnabled(false);
    }

    public void onCompetitionEnd() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                warCounterDisplay.setText("The competition is over. " +
                    warCounter + " sessions were run.");
            };
        });
        warThread = null;
        
        if (battleFrame == null)
        {
        	showBattleButton.setEnabled(true);
        	showBattleFrame = false;
        }
        
        groupToRunField.setEnabled(true);
        runWarButton.setText("<html><font color=red>Start!</font></html>");
        runWarButton.setEnabled(true);
		competitionRunning = false;
		seedField.setEnabled(true);
    }
    
	@Override
	public void onEndRound() {
	}
	
}