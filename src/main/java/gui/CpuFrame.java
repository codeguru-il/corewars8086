package il.co.codeguru.corewars8086.gui;

import il.co.codeguru.corewars8086.memory.RealModeAddress;
import il.co.codeguru.corewars8086.utils.Disassembler;
import il.co.codeguru.corewars8086.war.Competition;
import il.co.codeguru.corewars8086.war.CompetitionEventListener;
import il.co.codeguru.corewars8086.war.War;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class CpuFrame extends JFrame implements CompetitionEventListener {
	
	private War currentWar;
	
	private Competition competition;
	
	private JPanel menuPanel;
	
	private JComboBox<String> dropMenu;
	
	private JPanel regPanel,flagPanel;
	
	private RegisterField regAX,regBX,regCX,regDX,
						regSI,regDI,regBP,regSP,
						 regIP,regCS,regDS,regSS,
						 regES,regE;
	
	private FlagFields flagOF,flagDF,flagIF,flagTF,
						flagSF,flagZF,flagAF,flagPF,
						flagCF;

	private final JButton btnRefresh;
  private final JButton btnSave;

	private JTextArea instructionArea;
					
	
	public CpuFrame(Competition c){
		super("CPU state viewer - CodeGuru");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(550, 400);
		this.competition = c;
		this.currentWar = c.getCurrentWar();
		
		GridLayout l = new GridLayout(0,2);
		l.setVgap(5);
		l.setHgap(5);
		
		//registerFileds
		regPanel = new JPanel(l);
		
		this.setAlwaysOnTop(true);
		
		regAX = new RegisterField("AX"); regPanel.add(regAX);
		regBX = new RegisterField("BX"); regPanel.add(regBX);	
		regCX = new RegisterField("CX"); regPanel.add(regCX);
		regDX = new RegisterField("DX"); regPanel.add(regDX);
		regSI = new RegisterField("SI"); regPanel.add(regSI);
		regDI = new RegisterField("DI"); regPanel.add(regDI);
		regBP = new RegisterField("BP"); regPanel.add(regBP);
		regSP = new RegisterField("SP"); regPanel.add(regSP);
		regIP = new RegisterField("IP"); regPanel.add(regIP);
		regCS = new RegisterField("CS"); regPanel.add(regCS);
		regDS = new RegisterField("DS"); regPanel.add(regDS);
		regSS = new RegisterField("SS"); regPanel.add(regSS);
		regES = new RegisterField("ES"); regPanel.add(regES);
		regE = new RegisterField("Energy"); regPanel.add(regE);
		
		//Flags
		flagPanel = new JPanel(l);
		
		flagOF = new FlagFields("OF"); flagPanel.add(flagOF);
		flagDF = new FlagFields("DF"); flagPanel.add(flagDF);
		flagIF = new FlagFields("IF"); flagPanel.add(flagIF);
		flagTF = new FlagFields("TF"); flagPanel.add(flagTF);
		flagSF = new FlagFields("SF"); flagPanel.add(flagSF);
		flagZF = new FlagFields("ZF"); flagPanel.add(flagZF);
		flagAF = new FlagFields("AF"); flagPanel.add(flagAF);
		flagPF = new FlagFields("PF"); flagPanel.add(flagPF);
		flagCF = new FlagFields("CF"); flagPanel.add(flagCF);
		
		//menu panel
		
		menuPanel = new JPanel();
		
		dropMenu = new JComboBox<>();
		for( int i = 0 ; i < this.currentWar.getNumWarriors() ; i++ )
			dropMenu.addItem(this.currentWar.getWarrior(i).getName());
		
		dropMenu.addActionListener(e -> updateFields());
		
		btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(e -> updateFields());
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(e -> saveFields());
		
		instructionArea = new JTextArea(); 
		instructionArea.setFont(new Font("Monospaced",Font.PLAIN,15));
		instructionArea.setSize(50, 100);
		instructionArea.setLineWrap(true);
		instructionArea.setWrapStyleWord(true);
		
		this.updateFields();
		
		menuPanel.add(dropMenu);
		menuPanel.add(btnRefresh);
		menuPanel.add(btnSave);
		
		JPanel cpuPanel = new JPanel(new GridLayout(1, 3));
		cpuPanel.add(menuPanel);
		cpuPanel.add(flagPanel);
		cpuPanel.add(regPanel);
		
		this.add(cpuPanel);
		this.add(instructionArea);
		
		this.setLayout(new GridLayout(2,1,10,10));
		this.setResizable(false);
		this.setVisible(true);
	}
	
	public void updateFields(){
		regAX.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getAX());
		regBX.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getBX());
		regCX.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getCX());
		regDX.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getDX());
		regSI.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getSI());
		regDI.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getDI());
		regBP.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getBP());
		regSP.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getSP());
		regIP.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getIP());
		regCS.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getCS());
		regDS.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getDS());
		regSS.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getSS());
		regES.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getES());
		regE.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getEnergy());
		
		flagOF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getOverflowFlag());
		flagDF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getDirectionFlag() );
		flagIF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getInterruptFlag() );
		flagTF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getTrapFlag() );
		flagSF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getSignFlag() );
		flagZF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getZeroFlag() );
		flagAF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getAuxFlag() );
		flagPF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getParityFlag() );
		flagCF.setValue( currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getCarryFlag() );
		
		byte[] bytes = new byte[30];
		
		for (short i = 0; i < 30; i++) {
			short ip = currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getIP();
			short cs = currentWar.getWarrior(dropMenu.getSelectedIndex()).getCpuState().getCS();
			short vs = currentWar.getMemory().readByte(new RealModeAddress(cs, (short) (ip + i)));
			bytes[i] = (byte) vs;
		}
		
		try {
			instructionArea.setText(Disassembler.disassembler(bytes));
		} catch (Exception e) {
			instructionArea.setText(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void saveFields(){
		try {
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setAX(regAX.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setBX(regBX.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setCX(regCX.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setDX(regDX.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setSI(regSI.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setDI(regDI.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setBP(regBP.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setSP(regSP.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setIP(regIP.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setCS(regCS.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setDS(regDS.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setSS(regSS.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setES(regES.getValue());
			this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setEnergy(regE.getValue());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "You are trying to save invalid value!");
		}
		
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setOverflowFlag(this.flagOF.getValue());
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setDirectionFlag(this.flagDF.getValue());
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setInterruptFlag(this.flagIF.getValue());
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setTrapFlag(this.flagTF.getValue());
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setSignFlag(this.flagSF.getValue());
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setZeroFlag(this.flagZF.getValue());
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setAuxFlag(this.flagAF.getValue());
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setParityFlag(this.flagPF.getValue());
		this.currentWar.getWarrior(this.dropMenu.getSelectedIndex()).getCpuState().setCarryFlag(this.flagCF.getValue());
	}

	@Override
	public void onWarStart(long seed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWarEnd(int reason, String winners) {
		// TODO Auto-generated method stub
		this.dispose();
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

	@Override
	public void onEndRound() {
		// TODO Auto-generated method stub
		this.updateFields();
	}

	@Override
	public void dispose() {
		
		//bug fix - event casted while window is being disposed FIXME find a better solution
		this.competition.getCurrentWar().pause();
		
		try{
			Thread.sleep(300);
		} catch (Exception e) {
			
		}
		this.competition.removeCompetitionEventListener(this);
		this.competition.getCurrentWar().resume();
		
		super.dispose();
	}
	
}
