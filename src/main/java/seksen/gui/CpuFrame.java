/*
 * CpuFrame.java
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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.MaskFormatter;

import seksen.Main;
import seksen.debug.DebugData;
import seksen.debug.SourceLine;
import seksen.debug.SymbolList;
import seksen.hardware.Address;
import seksen.hardware.BreakpointManager;
import seksen.hardware.Machine;
import seksen.hardware.cpu.CpuException;
import seksen.hardware.cpu.CpuRunner;
import seksen.hardware.cpu.CpuState;
import seksen.hardware.memory.MemoryException;
import seksen.hardware.memory.RealModeMemory;
import seksen.software.loaders.BinaryLoader;
import seksen.util.Hex;


public class CpuFrame extends JFrame implements ActionListener, PropertyChangeListener {
	final Machine machine;
	final DebugData debugData;
	SymbolList symbolList;
	private CpuRunner cpuRunner;

	BreakpointManager breakpointMan = new BreakpointManager();

	private static final int MAXFRAME = 100;
	private Address frameAddress;
	private int frameDepth=0;
	private int[] framePositions = new int[MAXFRAME];

	final Font monoFont = new Font("Monospaced",Font.PLAIN,12);
	private JFileChooser fileDialog;

	private DisasmPanel disasmPanel;
	private TextViewer sourceViewer;
	private MemoryPanel memoryPanel;
	private SymbolPanel symbolPanel;
	private StackPanel stackPanel;
	private ProtectionPanel protectionPanel;

	private JTextArea logArea;
	private JTextArea outputArea;
	private JLabel statusLabel;
	private JLabel speedLabel;
	private JLabel timeLabel;

	private final JFormattedTextField regFields[];
	private static final String regNames[] = new String[]{
		"CS","IP","AX","BX","CX","DX","DS",
    	"SI","ES","DI","Fl","SS","SP","BP"};

	private Updater updater;

	public CpuFrame(Machine machine) {
        //Create and set up the window.
        super("Seksen");

		this.machine = machine;
		machine.memory.addAccessListener(breakpointMan);

		debugData = new DebugData();
		symbolList = debugData.symbolList;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        fileDialog = new JFileChooser();
        fileDialog.addChoosableFileFilter(SFileFilter.FF_BIN);
        fileDialog.addChoosableFileFilter(SFileFilter.FF_ELKS);
        fileDialog.addChoosableFileFilter(SFileFilter.FF_HEX);
        fileDialog.addChoosableFileFilter(SFileFilter.FF_LOC);
        fileDialog.addChoosableFileFilter(SFileFilter.FF_LST);
        fileDialog.addChoosableFileFilter(SFileFilter.FF_MAC);

        Container cpane = getContentPane();

        {	// Add the menu bar
        	JMenuBar menuBar;
        	JMenu menu;
        	JMenuItem menuItem;

        	// Create the menu bar.
        	menuBar = new JMenuBar();

        	// Build the File menu.
        	menu = new JMenu("File");
        	menu.setMnemonic(KeyEvent.VK_F);
        	menuBar.add(menu);

        	menuItem = new JMenuItem("Reset");
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menu.addSeparator();

        	menuItem = new JMenuItem("Load elks", KeyEvent.VK_O);
        	menuItem.setAccelerator(KeyStroke.getKeyStroke(
        			KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menuItem = new JMenuItem("Load binary", KeyEvent.VK_L);
        	menuItem.setAccelerator(KeyStroke.getKeyStroke(
        			KeyEvent.VK_L, ActionEvent.ALT_MASK));
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menuItem = new JMenuItem("Load Intel hex", KeyEvent.VK_H);
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menu.addSeparator();

        	menuItem = new JMenuItem("Load .loc file");
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menuItem = menu.add("Load .lst file");
        	menuItem.addActionListener(this);

        	menu.addSeparator();

        	menuItem = new JMenuItem("Save machine");
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menuItem = new JMenuItem("Load machine");
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menu.addSeparator();

        	menuItem = new JMenuItem("New Machine");
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	// Build the Run menu.
        	menu = new JMenu("Run");
        	menu.setMnemonic(KeyEvent.VK_R);
        	menuBar.add(menu);

        	menuItem = new JMenuItem("Step", KeyEvent.VK_F5);
        	menuItem.setAccelerator(KeyStroke.getKeyStroke(
        			KeyEvent.VK_F5,0));
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menuItem = new JMenuItem("Run", KeyEvent.VK_F8);
        	menuItem.setAccelerator(KeyStroke.getKeyStroke(
        			KeyEvent.VK_F8,0));
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menuItem = new JMenuItem("Stop", KeyEvent.VK_F9);
        	menuItem.setAccelerator(KeyStroke.getKeyStroke(
        			KeyEvent.VK_F9,0));
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	// Build the Tools menu.
        	menu = new JMenu("Tools");
        	menu.setMnemonic(KeyEvent.VK_T);
        	menuBar.add(menu);

        	menuItem = new JMenuItem("New Access Graph");
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menuItem = new JMenuItem("New Function Call");
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	// Build the Tools menu.
        	menu = new JMenu("Help");
        	menu.setMnemonic(KeyEvent.VK_H);
        	menuBar.add(menu);

        	menuItem = new JMenuItem("About");
        	menuItem.addActionListener(this);
        	menu.add(menuItem);

        	menuBar.add(Box.createHorizontalGlue());

        	Dimension prefsize = new Dimension(70,10);

        	speedLabel = new JLabel("0",JLabel.RIGHT);
        	speedLabel.setPreferredSize(prefsize);
        	menuBar.add(speedLabel);

        	timeLabel = new JLabel("0",JLabel.RIGHT);
        	timeLabel.setPreferredSize(prefsize);
        	menuBar.add(timeLabel);

        	statusLabel = new JLabel("Halt",JLabel.CENTER);
        	statusLabel.setPreferredSize(prefsize);
        	menuBar.add(statusLabel);

        	setJMenuBar(menuBar);
        }

        {	// Add disasm and log views
        	JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            cpane.add(split,BorderLayout.CENTER);
            split.setResizeWeight(0.8);
            split.setOneTouchExpandable(true);

            JTabbedPane tabbedpane = new JTabbedPane();

            disasmPanel = new DisasmPanel(this);
            disasmPanel.setPreferredSize(new Dimension(450,300));
            tabbedpane.add("Disassembler",disasmPanel);

            sourceViewer = new TextEditor();
            sourceViewer.setPreferredSize(new Dimension(450,300));
            sourceViewer.text_area.setFont(monoFont);
            tabbedpane.add("Source",sourceViewer);

            split.setTopComponent(tabbedpane);

            tabbedpane = new JTabbedPane();

            logArea = new JTextArea(10,30);
            logArea.setEditable(false);
            logArea.setLineWrap(true);
            logArea.setFont(monoFont);
            machine.setLogWriter(new TextWriter(logArea));

            tabbedpane.add("Log",new JScrollPane(logArea,
            		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

            outputArea = new JTextArea(10,30);
            outputArea.setEditable(false);
            outputArea.setLineWrap(true);
            outputArea.setFont(monoFont);
            machine.setOutWriter(new TextWriter(outputArea));

            tabbedpane.add("Output",new JScrollPane(outputArea,
            		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

            memoryPanel = new MemoryPanel(this);
            memoryPanel.setPreferredSize(new Dimension(600,200));
            tabbedpane.add("Memory",memoryPanel);

            symbolPanel = new SymbolPanel(this);
            symbolPanel.setPreferredSize(new Dimension(600,200));
            tabbedpane.add("Symbols",symbolPanel);

            tabbedpane.add("Command",new CommandPrompt(this));

			try {
				protectionPanel = new ProtectionPanel(this);
				protectionPanel.setPreferredSize(new Dimension(600,200));
				tabbedpane.add("Protection",protectionPanel);
			} catch(Error e) {
			}

            split.setBottomComponent(tabbedpane);
        }

        {//Left bar
        	Box rpanel = new Box(BoxLayout.Y_AXIS);
            cpane.add(rpanel,BorderLayout.WEST);

        	// Add reg panel
        	JPanel panel = new JPanel(new GridLayout(regNames.length,2));
            rpanel.add(panel);

			regFields = new JFormattedTextField[regNames.length];
        	for(int a=0; a<regNames.length;a++){
        		JLabel label = new JLabel(regNames[a]);
        		/*label.setFont(monoFont);*/
        		panel.add(label);

        		try {
        			JFormattedTextField field = new JFormattedTextField(new MaskFormatter("HHHH"));
        			field.setValue("0000");
        			field.addPropertyChangeListener("value",this);
					field.setFont(monoFont);
					regFields[a] = field;
				} catch (ParseException e) {
					e.printStackTrace();
				}
				panel.add(regFields[a]);
        	}

        	{
        		Box box = new Box(BoxLayout.X_AXIS);
        		Insets insets = new Insets(0,0,0,0);
        		JButton button = new JButton("UP");
        		button.setMargin(insets);
        		button.addActionListener(this);
        		box.add(button);

        		button = new JButton("DOWN");
        		button.setMargin(insets);
        		button.addActionListener(this);
        		box.add(button);
        		rpanel.add(box);
        	}

        	stackPanel = new StackPanel(this);
            rpanel.add(stackPanel);
        }

        update();

        //Display the window.
        pack();
        setVisible(true);

        updater = new Updater();
    }

	@Override
	public void dispose() {
		stop();
		super.dispose();
	}

	public void actionPerformed(ActionEvent e) {
		{
			String cmd = e.getActionCommand();
			log(cmd);

			if(cmd.equals("Reset")){
				reset();
			} else if(cmd.equals("Load elks")){
				loadElks();
			} else if(cmd.equals("Load binary")){
				loadBinary();
			} else if(cmd.equals("Load Intel hex")){
				loadIntelHex();
			} else if(cmd.equals("Load .loc file")){
				loadLoc();
			} else if(cmd.equals("Load .lst file")){
				loadLst();
			} else if(cmd.equals("Save machine")){
				saveMachine();
			} else if(cmd.equals("Load machine")){
				loadMachine();
			} else if(cmd.equals("Step")){
				step();
			} else if(cmd.equals("Run")){
				run();
			} else if(cmd.equals("Stop")){
				stop();
			} else if(cmd.equals("UP")){
				upFrame();
			} else if(cmd.equals("DOWN")){
				downFrame();
			} else if(cmd.equals("New Machine")){
				Main.newMachine();
			} else if(cmd.equals("New Access Graph")){
				new MemoryAccessGraph(this);
			} else if(cmd.equals("New Function Call")){
				new FunctionCallList(this);
			} else if(cmd.equals("About")){
				/*JButton but = new JButton("<html>visit <b>seksen.sourceforge.net</b></html>");
				but.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							java.awt.Desktop.getDesktop().browse(new URI("http://seksen.sourceforge.net"));
						} catch (IOException ex) {
						} catch (URISyntaxException ex) {
						}
					}
				});
				JPanel pan = new JPanel();
				pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
				pan.add(new JLabel("Seksen 1.0 Beta"));
				pan.add(but);*/
				JOptionPane.showMessageDialog(this, new AboutPanel(), "About",
					JOptionPane.PLAIN_MESSAGE);
			}
		}
	}

	void reset()
	{
		machine.reset();

		update();

		try {
			logArea.getDocument().remove(0, logArea.getDocument().getLength());
			outputArea.getDocument().remove(0, outputArea.getDocument().getLength());
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	private void loadElks()
	{
		fileDialog.setDialogTitle("Load ELKS file");
        fileDialog.setFileFilter(SFileFilter.FF_ELKS);

    	int returnVal = fileDialog.showOpenDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
        	return;
        }
        File file = fileDialog.getSelectedFile();

		reset();

		try {
			BinaryLoader.loadElks(machine, file);
			setTitle("Seksen "+file.getPath());
		} catch (IOException e1) {
			log("Load error: " + file.getName());
			return;
		}

		{
			String locpath = file.getPath();
			locpath = locpath.substring(0,locpath.lastIndexOf('.'))+".loc";
			File locfile = new File(locpath);
			if(locfile.exists()){
				try {
					debugData.loadParadigmLocFile(machine.address,locfile);
					symbolList = debugData.symbolList;
					disasmPanel.updateSymbolList();
					memoryPanel.updateSymbolList();
					symbolPanel.update();
				} catch (IOException e1) {
					log("Load symbol file error: " + locfile.getName());
				}
			}
		}

		update();
		updater.reset();
    }

	void loadBinary()
	{
		fileDialog.setDialogTitle("Load binary file");
        fileDialog.setFileFilter(SFileFilter.FF_BIN);

    	int returnVal = fileDialog.showOpenDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
        	return;
        }
        File file = fileDialog.getSelectedFile();

        Address address;

        while(true){
        	String addstr = (String)JOptionPane.showInputDialog(
        			this,null,
        			"Enter address to load binary",
        			JOptionPane.PLAIN_MESSAGE,
        			null,null,"7800:0000");

        	if ((addstr != null) && (addstr.length() > 0)) {
        		int delim = addstr.indexOf(':');
        		if(delim>=0){
        			try{
        				int seg = Integer.parseInt(addstr.substring(0, delim),16);
        				int off = Integer.parseInt(addstr.substring(delim+1),16);
        				address = machine.newAddress(seg, off);
        				break;
        			} catch (NumberFormatException ne){
        			}
        		}
        	}
        }

        log("Opening: " + file.getName() +" at "+address);

		try {
			BinaryLoader.loadBin(machine, file, address);
		} catch (IOException e1) {
			log("Load error: " + file.getName());
			return;
		}


		update();
    }

	private void loadIntelHex()
	{
		fileDialog.setDialogTitle("Load Intel Hex file");
        fileDialog.setFileFilter(SFileFilter.FF_HEX);

    	int returnVal = fileDialog.showOpenDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
        	return;
        }
        File file = fileDialog.getSelectedFile();

		try {
			BinaryLoader.loadIntelHex(machine, file);
		} catch (Exception e1) {
			log("Load error: " + file.getName());
			log(e1.toString());
			return;
		}

    	update();
    }

	private void loadLoc()
	{
		fileDialog.setDialogTitle("Load Locations file");
        fileDialog.setFileFilter(SFileFilter.FF_LOC);
        if (fileDialog.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
        	return;
        }

        File locfile = fileDialog.getSelectedFile();

		if(locfile.exists()){
			fileDialog.setDialogTitle("Choose source base directory");
			fileDialog.setFileFilter(fileDialog.getAcceptAllFileFilter());
	        fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        int returnVal = fileDialog.showOpenDialog(this);
	        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
	        if (returnVal != JFileChooser.APPROVE_OPTION) {
	        	return;
	        }

	        File srcdir = fileDialog.getSelectedFile();
	        debugData.setSourceDir(srcdir);

			try {
				debugData.loadParadigmLocFile(machine.address,locfile);
				symbolList = debugData.symbolList;
				disasmPanel.updateSymbolList();
				memoryPanel.updateSymbolList();
				symbolPanel.update();
			} catch (IOException e1) {
				log("Load symbol file error: " + locfile.getName());
			}
		} else {
			log("File doesn't exist: " + locfile.getName());
			return;
		}

		update();
    }

	private void loadLst()
	{
		fileDialog.setDialogTitle("Load lst file");
        fileDialog.setFileFilter(SFileFilter.FF_LST);

    	int returnVal = fileDialog.showOpenDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
        	return;
        }
        File locfile = fileDialog.getSelectedFile();

		if(locfile.exists()){
			try {
				debugData.loadIdaLstFile(machine.address,locfile);
			} catch (IOException e1) {
				log("Load symbol file error: " + locfile.getName());
			}
		} else {
			log("File doesn't exist: " + locfile.getName());
			return;
		}

		update();
    }

	private void saveMachine() {
		fileDialog.setDialogTitle("Save Machine File");
        fileDialog.setFileFilter(SFileFilter.FF_MAC);

    	int returnVal = fileDialog.showSaveDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
        	return;
        }
        File file = fileDialog.getSelectedFile();

        try {
        	FileOutputStream filestream = new FileOutputStream(file);
			machine.save(filestream);
			filestream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private void loadMachine() {
		fileDialog.setDialogTitle("Load Machine file");
        fileDialog.setFileFilter(SFileFilter.FF_MAC);

    	int returnVal = fileDialog.showOpenDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
        	return;
        }
        File file = fileDialog.getSelectedFile();

        try {
        	FileInputStream filestream = new FileInputStream(file);
			machine.load(filestream);
			filestream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

    	update();
		updater.reset();
    }

	void step()
	{
    	try {
			machine.do_cycle();
		} catch (CpuException e1) {
			log(e1.toString());
		} catch (MemoryException e1) {
			log(e1.toString());
		}

    	update();
    }

	void run()
	{
    	if(cpuRunner!=null){
    		log("Cpu is already running.");
    	}

		cpuRunner = new CpuRunner(
				machine,
				new TextWriter(logArea),
				new Runnable(){
					public void run() {
						runOver();
					};
				} );

		updater.startUpdating();

		cpuRunner.start();
		statusLabel.setText("Running");
    }

	private void runOver() {
		Address[] his = cpuRunner.getHistory();
		log("Run history:");
		for(int a=0; a<his.length && a<10; a++){
			log(his[a].toString());
		}

    	update();
    	statusLabel.setText("Halt");
    	cpuRunner=null;
	}

	void stop()
	{
    	if(cpuRunner==null){
    		log("Cpu isn't running.");
    	} else {
    		cpuRunner.stopCpu();
    	}
    }

	void update(){
		updateRegs();
		disasmPanel.update();
		memoryPanel.update();
		stackPanel.update();
		symbolPanel.update();
		if( protectionPanel != null ) {
			protectionPanel.update();
		}
	}

	private void updateRegs()
	{
		CpuState state = machine.state;

		for(int a=0;a<regNames.length;a++){
			regFields[a].setForeground(Color.BLACK);
		}

		regFields[0].setValue(Hex.toHexString(state.getCS(),4));
		regFields[1].setValue(Hex.toHexString(state.getIP(),4));
		regFields[2].setValue(Hex.toHexString(state.getAX(),4));
		regFields[3].setValue(Hex.toHexString(state.getBX(),4));
		regFields[4].setValue(Hex.toHexString(state.getCX(),4));
		regFields[5].setValue(Hex.toHexString(state.getDX(),4));
		regFields[6].setValue(Hex.toHexString(state.getDS(),4));
		regFields[7].setValue(Hex.toHexString(state.getSI(),4));
		regFields[8].setValue(Hex.toHexString(state.getES(),4));
		regFields[9].setValue(Hex.toHexString(state.getDI(),4));
		regFields[10].setValue(Hex.toHexString(state.getFlags(),4));
		regFields[11].setValue(Hex.toHexString(state.getSS(),4));
		regFields[12].setValue(Hex.toHexString(state.getSP(),4));
		regFields[13].setValue(Hex.toHexString(state.getBP(),4));
	}

	public void propertyChange(PropertyChangeEvent evt) {

		Object source = evt.getSource();

		int a;
		for(a=0;a<regNames.length;a++){
			if(source == regFields[a]){
				break;
			}
		}

		if(a>=regNames.length){
			return;
		}

		regFields[a].setForeground(Color.RED);

		CpuState state = machine.state;

		short value = (short)Integer.parseInt((String)regFields[a].getValue(),16);
		switch(a){
		case 0:
			state.setCS(value);
			gotoAddress(machine.newAddress(state.getCS(),state.getIP()));
			break;
		case 1:
			state.setIP(value);
			gotoAddress(machine.newAddress(state.getCS(),state.getIP()));
			break;
		case 2:	state.setAX(value); break;
		case 3:	state.setBX(value); break;
		case 4:	state.setCX(value); break;
		case 5:	state.setDX(value); break;
		case 6:	state.setDS(value); break;
		case 7:	state.setSI(value); break;
		case 8:	state.setES(value); break;
		case 9: state.setDI(value); break;
		case 10: state.setFlags(value); break;
		case 11: state.setSS(value); resetFrame(); break;
		case 12: state.setSP(value); resetFrame(); break;
		case 13: state.setBP(value); resetFrame(); break;
		}

		resetFrame();
	}

	public void gotoAddress(Address addr) {
		disasmPanel.gotoAddress(addr);

		SourceLine sl = debugData.sourceList.getLine(addr);
		if( sl != null ) {
			try {
				if( sourceViewer.loadFile(sl.getPath()) ) {
					sourceViewer.gotoLine(sl.getLine());
				}
			} catch (IOException e) {
			}
		}
	}

	public void log(String text)
	{
		logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	//Frame methods ************************************************************
	void resetFrame(){
		frameDepth = 0;
		CpuState state = machine.state;
		frameAddress = machine.newAddress(state.getSS(),state.getBP());
		stackPanel.gotoAddress(machine.newAddress(state.getSS(),state.getSP()));
	}

	void upFrame(){
		int bp = frameAddress.getOffset();
		if(bp>0xfffa || frameDepth>MAXFRAME-1){
			log("Can't go up");
			return;
		}

		if(setFrame(bp)){
			framePositions[frameDepth++] = bp;
		}
	}

	void downFrame(){
		if(frameDepth==0){
			log("Can't go down");
			return;
		}

		if(frameDepth==1){
			resetFrame();
			gotoAddress(
					machine.newAddress(machine.state.getCS(),machine.state.getIP()));
			return;
		}

		int bp = framePositions[frameDepth-2];
		frameDepth--;
		setFrame(bp);
	}

	boolean setFrame(int bp){
		Address frameAddr = machine.newAddress(frameAddress.getSegment(),bp);
		RealModeMemory memory = machine.memory;
		try {
			int bp2 = memory.readWord(frameAddr)&0xffff;
			if(!(bp2>bp || (bp2==0 && bp!=0))){
				return false;
			}
			bp = bp2;
			gotoAddress(machine.newAddress(
					memory.readWord(frameAddr.addOffset(4)),
					memory.readWord(frameAddr.addOffset(2))));
		} catch (MemoryException e) {
		}

		stackPanel.gotoAddress(frameAddr.addOffset(6));
		frameAddress = machine.newAddress(frameAddress.getSegment(),bp);

		log("frame bp:"+Hex.toHexString(bp, 4));
		return true;
	}

	class Updater extends Thread {
		int instCount = 0;
		int totalTime = 0;
		int startTime;

		public Updater() {
			setDaemon(true);
			start();
		}

		public void run() {
			startTime = (int)(System.currentTimeMillis()/1000);

			while(true){
				while(cpuRunner == null){
					synchronized (this) {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
					startTime = (int)(System.currentTimeMillis()/1000);
				}

				int ic = cpuRunner.instCounter;
				speedLabel.setText(Integer.toString(ic-instCount));
				instCount = ic;

				int tm = totalTime;
				StringBuffer time = new StringBuffer();
				time.append(Integer.toString(tm % 60));
				while(tm>=60){
					if((tm%60)<10){
						time.insert(0,'0');
					}
					tm /= 60;
					time.insert(0, ':').insert(0, Integer.toString(tm % 60));
				}
				timeLabel.setText(time.toString());

				synchronized (this) {
					try {
						wait(1000);
					} catch (InterruptedException e) {
					}
				}

				totalTime += (int)(System.currentTimeMillis()/1000)-startTime;
			}
		}

		void startUpdating(){
			synchronized (this) {
				notify();
			}
		}

		void reset(){
			totalTime = 0;
		}
	}

}

class TextWriter extends Writer{
	JTextArea textarea;

	TextWriter(JTextArea text){
		textarea = text;
	}

	public void close() throws IOException {
	}

	public void flush() throws IOException {
	}

	public void write(String str) throws IOException {
		textarea.append(str);
		textarea.setCaretPosition(textarea.getDocument().getLength());
	}

	public void write(char[] cbuf, int off, int len) throws IOException {
		textarea.append(new String(cbuf,off,len));
		textarea.setCaretPosition(textarea.getDocument().getLength());
	}
}
