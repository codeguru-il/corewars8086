package gui;

import memory.MemoryEventListener;
import memory.RealModeAddress;
import utils.Unsigned;
import war.*;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;

public class WarFrame extends JFrame
    implements MemoryEventListener, CompetitionEventListener, MouseAddressRequest {
    private static final long serialVersionUID = 1L;

    private final Canvas warCanvas;
    private final JTextArea messagesArea;
    private final JList<WarriorInfo> nameList;
    private final DefaultListModel<WarriorInfo> nameListModel;
    private int nRoundNumber;
    private final JTextField roundNumber;

    private final JLabel addressFiled;
    private final JButton btnCpuState;
    private CpuFrame cpuFrame;
    private final JButton btnPause;
    private final JButton btnSingleRound;
    private final JSlider speedSlider;

    private Competition competition;
    private MemoryFrame memoryFrame;

    // --- Replay Mode members ---
    private final boolean isReplayMode;
    private ReplayManager replayManager;
    private int currentReplayRound = 0;
    private Timer replayTimer;

    /**
     * CONSTRUCTOR FOR LIVE MODE
     */
    public WarFrame(final Competition competition) {
        super("CodeGuru Extreme - Session Viewer");
        this.isReplayMode = false;
        this.competition = competition;
        
        // --- UI Initialization ---
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel warZone = new JPanel(new BorderLayout());
        warZone.setBackground(Color.BLACK);
        JPanel canvasPanel = new JPanel();
        canvasPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(169, 154, 133), 3),
            BorderFactory.createEmptyBorder(10, 10, 20, 10)));
        canvasPanel.setBackground(Color.BLACK);
        warCanvas = new Canvas();
        canvasPanel.add(warCanvas);
        warZone.add(canvasPanel, BorderLayout.CENTER);
        mainPanel.add(warZone, BorderLayout.CENTER);

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
        speedSlider = new JSlider(1, 100, competition.getSpeed());
        speedSlider.addChangeListener(e -> {
            if (!isReplayMode) {
                WarFrame.this.competition.setSpeed((int) Math.pow(1.2, speedSlider.getValue()));
            } else {
                updateReplaySpeed();
            }
        });
        buttonPanel.add(speedSlider);
        infoZone.add(buttonPanel, BorderLayout.SOUTH);

        addressFiled = new JLabel("Click on the arena to see the memory");
        warCanvas.addListener(this);
        
        // Initialize buttons BEFORE adding listeners that reference them
        btnCpuState = new JButton("View CPU");
        btnPause = new JButton("Pause");
        btnSingleRound = new JButton("Single Round");

        btnCpuState.setEnabled(false);
        btnCpuState.addActionListener(event -> {
            cpuFrame = new CpuFrame(competition);
            WarFrame.this.competition.addCompetitionEventListener(cpuFrame);
        });
        
        btnPause.setEnabled(false);
        btnPause.addActionListener(event -> {
            if (competition.getCurrentWar().isPaused()) {
                competition.getCurrentWar().resume();
                btnPause.setText("Pause");
                btnSingleRound.setEnabled(false); // This reference is now safe
            } else {
                competition.getCurrentWar().pause();
                btnPause.setText("Resume");
                btnSingleRound.setEnabled(true); // This reference is now safe
            }
        });
        
        btnSingleRound.setEnabled(false);
        btnSingleRound.addActionListener(event -> competition.getCurrentWar().runSingleRound());
        
        buttonPanel.add(btnCpuState);
        buttonPanel.add(btnPause);
        buttonPanel.add(btnSingleRound);
        buttonPanel.add(addressFiled);
        
        competition.addCompetitionEventListener(this);

        JPanel warriorZone = new JPanel(new BorderLayout());
        warriorZone.setBackground(Color.BLACK);
        nameListModel = new DefaultListModel<>();
        nameList = new JList<>(nameListModel);
        nameList.setPreferredSize(new Dimension(200, 0));
        nameList.setCellRenderer(new NameCellRenderer());
        nameList.setOpaque(false);
        nameList.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(169, 154, 133), 3),
            BorderFactory.createEmptyBorder(10, 10, 20, 10)));
        warriorZone.add(nameList, BorderLayout.CENTER);
        warriorZone.add(Box.createHorizontalStrut(20), BorderLayout.WEST);
        mainPanel.add(warriorZone, BorderLayout.EAST);

        getContentPane().setBackground(Color.BLACK);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(infoZone, BorderLayout.SOUTH);
    }

    /**
     * CONSTRUCTOR FOR REPLAY MODE
     */
    public WarFrame(ReplayManager manager) {
        super("CodeGuru Extreme - Replay Viewer");
        this.isReplayMode = true;
        this.replayManager = manager;

        // --- UI Initialization (similar to live mode) ---
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel warZone = new JPanel(new BorderLayout());
        warZone.setBackground(Color.BLACK);
        JPanel canvasPanel = new JPanel();
        canvasPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(169, 154, 133), 3),
            BorderFactory.createEmptyBorder(10, 10, 20, 10)));
        canvasPanel.setBackground(Color.BLACK);
        warCanvas = new Canvas();
        canvasPanel.add(warCanvas);
        warZone.add(canvasPanel, BorderLayout.CENTER);
        mainPanel.add(warZone, BorderLayout.CENTER);

        JPanel infoZone = new JPanel(new BorderLayout());
        messagesArea = new JTextArea(5, 60);
        messagesArea.setFont(new Font("Tahoma", Font.PLAIN, 12));
        infoZone.add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JLabel("Round:"));
        roundNumber = new JTextField(8);
        roundNumber.setEditable(false);
        buttonPanel.add(roundNumber);
        buttonPanel.add(Box.createHorizontalStrut(20));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(new JLabel("Speed:"));
        speedSlider = new JSlider(1, 100, 50);
        speedSlider.addChangeListener(e -> updateReplaySpeed());
        buttonPanel.add(speedSlider);
        infoZone.add(buttonPanel, BorderLayout.SOUTH);

        addressFiled = new JLabel("Replay Mode");
        btnCpuState = new JButton("View CPU");
        btnPause = new JButton("Play");
        btnSingleRound = new JButton("Step");
        buttonPanel.add(btnCpuState);
        buttonPanel.add(btnPause);
        buttonPanel.add(btnSingleRound);
        buttonPanel.add(addressFiled);

        JPanel warriorZone = new JPanel(new BorderLayout());
        warriorZone.setBackground(Color.BLACK);
        nameListModel = new DefaultListModel<>();
        nameList = new JList<>(nameListModel);
        nameList.setPreferredSize(new Dimension(200, 0));
        nameList.setCellRenderer(new NameCellRenderer());
        warriorZone.add(nameList, BorderLayout.CENTER);
        mainPanel.add(warriorZone, BorderLayout.EAST);

        getContentPane().setBackground(Color.BLACK);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(infoZone, BorderLayout.SOUTH);

        // --- Replay Specific Logic ---
        btnCpuState.setEnabled(false); // CPU state not available in replay
        warCanvas.removeMouseListener(warCanvas); // Disable memory inspection
        warCanvas.removeMouseMotionListener(warCanvas);

        replayTimer = new Timer(100, e -> advanceReplayRound());
        
        btnPause.addActionListener(e -> toggleReplay());
        btnSingleRound.addActionListener(e -> advanceReplayRound());
        
        initializeReplayState();
    }

    // --- REPLAY METHODS ---

    private void initializeReplayState() {
        JSONObject startData = replayManager.getWarStartData().getJSONObject("payload");
        JSONArray warriors = startData.getJSONArray("warriors");
        warCanvas.clear();
        nameListModel.clear();
        addMessage("Replay loaded. Press Play to start.");

        for (int i = 0; i < warriors.length(); i++) {
            JSONObject w = warriors.getJSONObject(i);
            String name = w.getString("name");
            nameListModel.addElement(new WarriorInfo(name));
        }
        updateRoundDisplay();
    }

    private void advanceReplayRound() {
        if (currentReplayRound > replayManager.getTotalRounds()) {
            if (replayTimer.isRunning()) {
                toggleReplay(); // Stop playback
            }
            return;
        }

        // Redraw previous state to remove old pointers
        if (currentReplayRound > 0) {
            List<ReplayEvent> prevEvents = replayManager.getEventsInRound(currentReplayRound - 1);
            applyReplayMemoryEvents(prevEvents);
        }

        List<ReplayEvent> roundEvents = replayManager.getEventsInRound(currentReplayRound);
        applyReplayMemoryEvents(roundEvents);

        ReplayEvent roundUpdateEvent = roundEvents.stream().filter(e -> "ROUND_UPDATE".equals(e.type)).findFirst().orElse(null);
        if (roundUpdateEvent != null) {
            warCanvas.deletePointers();
            JSONArray warriorStates = roundUpdateEvent.data.getJSONObject("payload").getJSONArray("warriorStates");
            for (int i = 0; i < warriorStates.length(); i++) {
                JSONObject state = warriorStates.getJSONObject(i);
                int arenaOffset = state.getInt("ip") - 0x10000;
                warCanvas.paintPointer(arenaOffset, (byte)state.getInt("id"));
            }
        }

        roundEvents.stream().filter(e -> "WARRIOR_DEATH".equals(e.type)).forEach(this::processReplayDeathEvent);
        
        updateRoundDisplay();
        currentReplayRound++;
    }

    private void applyReplayMemoryEvents(List<ReplayEvent> events) {
        for (ReplayEvent event : events) {
            if ("MEMORY_WRITE".equals(event.type)) {
                JSONObject payload = event.data.getJSONObject("payload");
                int arenaOffset = payload.getInt("address") - 0x10000;
                warCanvas.paintPixel(arenaOffset, (byte)payload.getInt("actorId"));
            }
        }
    }

    private void processReplayDeathEvent(ReplayEvent event) {
        JSONObject payload = event.data.getJSONObject("payload");
        String warriorName = payload.getString("warriorName");
        addMessage(currentReplayRound, warriorName + " died due to " + payload.getString("reason"));
        for (int i = 0; i < nameListModel.getSize(); i++) {
            WarriorInfo info = nameListModel.getElementAt(i);
            if (info.name.equals(warriorName)) {
                info.alive = false;
                nameList.repaint();
                break;
            }
        }
    }

    private void toggleReplay() {
        if (replayTimer.isRunning()) {
            replayTimer.stop();
            btnPause.setText("Play");
        } else {
            replayTimer.start();
            btnPause.setText("Pause");
        }
    }

    private void updateReplaySpeed() {
        int value = speedSlider.getValue();
        if (value == 0) return;
        int delay = 1000 / value; // delay in ms
        replayTimer.setDelay(Math.max(1, delay));
    }
    
    private void updateRoundDisplay() {
        roundNumber.setText(currentReplayRound + " / " + replayManager.getTotalRounds());
    }

    // --- SHARED AND LIVE-MODE METHODS ---
    
    public void addMessage(String message) {
        messagesArea.append(message + "\n");
        SwingUtilities.invokeLater(() -> messagesArea.setCaretPosition(messagesArea.getDocument().getLength()));
    }
    
    public void addMessage(int round, String message) {
        addMessage("[" + round + "] " + message);
    }

    @Override
    public void onMemoryWrite(RealModeAddress address) {
        if (isReplayMode) return;
        int ipInsideArena = address.getLinearAddress() - 0x10000;
        if (address.getSegment() == War.ARENA_SEGMENT) {
            warCanvas.paintPixel(
                Unsigned.unsignedShort(ipInsideArena),
                (byte) competition.getCurrentWarrior());
        }
    }

    @Override
    public void onWarStart(long seed) {
        if (isReplayMode) return;
        addMessage("=== Session started ===");
        nameListModel.clear();
        warCanvas.clear();
        if (competition.getCurrentWar().isPaused()) {
            btnPause.setText("Resume");
            btnSingleRound.setEnabled(true);
        }
    }

    @Override
    public void onWarEnd(int reason, String winners) {
        if (isReplayMode) return;
        roundNumber.setText(Integer.toString(nRoundNumber));
        switch (reason) {
            case CompetitionEventListener.SINGLE_WINNER:
                addMessage(nRoundNumber, "Session over: The winner is " + winners + "!");
                break;
            case CompetitionEventListener.MAX_ROUND_REACHED:
                addMessage(nRoundNumber, "Maximum round reached: The winners are " + winners + "!");
                break;
            case CompetitionEventListener.ABORTED:
                addMessage(nRoundNumber, "Session aborted: The winners are " + winners + "!");
                break;
        }
    }

    @Override
    public void onRound(int round) {
        if (isReplayMode) return;
        nRoundNumber = round;
        if ((nRoundNumber % 1000) == 0) {
            roundNumber.setText(Integer.toString(nRoundNumber));
        }
        btnCpuState.setEnabled(true);
        btnPause.setEnabled(true);
    }

    @Override
    public void onWarriorBirth(String warriorName) {
        if (isReplayMode) return;
        addMessage(nRoundNumber, warriorName + " enters the arena.");
        nameListModel.addElement(new WarriorInfo(warriorName));
    }

    @Override
    public void onWarriorDeath(String warriorName, String reason) {
        if (isReplayMode) return;
        addMessage(nRoundNumber, warriorName + " died due to " + reason + ".");
        Enumeration<WarriorInfo> namesListElements = nameListModel.elements();
        while (namesListElements.hasMoreElements()) {
            WarriorInfo info = namesListElements.nextElement();
            if (info.name.equals(warriorName)) {
                info.alive = false;
                nameList.repaint();
                break;
            }
        }
    }

    @Override
    public void onCompetitionStart() {
        if (isReplayMode) return;
        btnCpuState.setEnabled(true);
        btnPause.setEnabled(true);
    }
    
    @Override
    public void onCompetitionEnd() {
        if (isReplayMode) return;
        btnCpuState.setEnabled(false);
        btnPause.setEnabled(false);
    }

    @Override
    public void onEndRound() {
        if (isReplayMode) return;
        warCanvas.deletePointers();
        for (int i = 0; i < competition.getCurrentWar().getNumWarriors(); i++) {
            Warrior warrior = competition.getCurrentWar().getWarrior(i);
            if (warrior.isAlive()) {
                short ip = warrior.getCpuState().getIP();
                short cs = warrior.getCpuState().getCS();
                int ipInsideArena = new RealModeAddress(cs, ip).getLinearAddress() - 0x10000;
                warCanvas.paintPointer(ipInsideArena, (byte) i);
            }
        }
    }

    @Override
    public void dispose() {
        if (!isReplayMode && competition != null && competition.getCurrentWar() != null) {
            competition.getCurrentWar().pause();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) { /* ignore */ }
            competition.removeCompetitionEventListener(this);
            competition.removeMemoryEventLister(this);
            competition.getCurrentWar().resume();
            competition.setSpeed(Competition.MAXIMUM_SPEED);
        }
        if (replayTimer != null) {
            replayTimer.stop();
        }
        super.dispose();
    }

    @Override
    public void addressAtMouseLocationRequested(int address) {
        if (isReplayMode) return;
        RealModeAddress tmp = new RealModeAddress(War.ARENA_SEGMENT, (short) address);
        byte data = this.competition.getCurrentWar().getMemory().readByte(tmp);
        addressFiled.setText(Integer.toHexString(address).toUpperCase() + ": " + String.format("%02X", data).toUpperCase());
        if (memoryFrame == null || !memoryFrame.isVisible()) {
            memoryFrame = new MemoryFrame(competition, tmp.getLinearAddress());
            this.competition.addCompetitionEventListener(memoryFrame);
        } else {
            memoryFrame.refresh(tmp.getLinearAddress());
        }
    }

    // --- INNER CLASSES ---
    
    static class WarriorInfo {
        String name;
        boolean alive;
        WarriorInfo(String name) { this.name = name; this.alive = true; }
        @Override public String toString() { return name; }
    }
    
    class NameCellRenderer extends JLabel implements ListCellRenderer<WarriorInfo> {
        private static final long serialVersionUID = 1L;
        NameCellRenderer() { setFont(new Font("Tahoma", Font.PLAIN, 20)); }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends WarriorInfo> list, WarriorInfo value, int index, boolean isSelected, boolean cellHasFocus) {
            String text = value.name;
            if (!value.alive) {
                text = "<html><S>" + text + "</S></html>";
            }
            setText(text);
            setForeground(warCanvas.getColorForWarrior(index));
            return this;
        }
    }
}