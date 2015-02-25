package il.co.codeguru.corewars8086.gui;

import java.awt.Color;
import java.awt.GridLayout;

import il.co.codeguru.corewars8086.cpu.CpuState;
import il.co.codeguru.corewars8086.utils.Disassembler;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CpuPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	/** flags */
	private JCheckBox flagOF,flagDF,flagIF,flagTF,flagSF,flagZF,flagAF,flagPF,flagCF;
	
	/** registers */
	private JTextField regAX, regBX, regCX, regDX, regDS, regES, regSI, regDI, regSS, regSP, regBP, regNRG, regCS, regIP;
	
	/** bombs count */
	private JTextField bomb1, bomb2;
	
	public CpuPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		
		JPanel regs = new JPanel(new GridLayout(4, 8));;
		
		regs.add(new JLabel("AX:"));
		regAX = new JTextField();
		regAX.setEditable(false);
		regs.add(regAX);
		
		regs.add(new JLabel("BX:"));
		regBX = new JTextField();
		regBX.setEditable(false);
		regs.add(regBX);
		
		regs.add(new JLabel("CX:"));
		regCX = new JTextField();
		regCX.setEditable(false);
		regs.add(regCX);
		
		regs.add(new JLabel("DX:"));
		regDX = new JTextField();
		regDX.setEditable(false);
		regs.add(regDX);
		
		regs.add(new JLabel("DS:"));
		regDS = new JTextField();
		regDS.setEditable(false);
		regs.add(regDS);
		
		regs.add(new JLabel("ES:"));
		regES = new JTextField();
		regES.setEditable(false);
		regs.add(regES);
		
		regs.add(new JLabel("SI:"));
		regSI = new JTextField();
		regSI.setEditable(false);
		regs.add(regSI);
		
		regs.add(new JLabel("DI:"));
		regDI = new JTextField();
		regDI.setEditable(false);
		regs.add(regDI);
		
		regs.add(new JLabel("SS:"));
		regSS = new JTextField();
		regSS.setEditable(false);
		regs.add(regSS);
		
		regs.add(new JLabel("SP:"));
		regSP = new JTextField();
		regSP.setEditable(false);
		regs.add(regSP);
		
		regs.add(new JLabel("BP:"));
		regBP = new JTextField();
		regBP.setEditable(false);
		regs.add(regBP);
		
		regs.add(new JLabel("NRG:"));
		regNRG = new JTextField();
		regNRG.setEditable(false);
		regs.add(regNRG);
		
		regs.add(new JLabel("CS:"));
		regCS = new JTextField();
		regCS.setEditable(false);
		regs.add(regCS);
		
		regs.add(new JLabel("IP:"));
		regIP = new JTextField();
		regIP.setEditable(false);
		regs.add(regIP);
		
		regs.add(new JLabel("INT 86:"));
		bomb1 = new JTextField();
		bomb1.setEditable(false);
		regs.add(bomb1);
		
		regs.add(new JLabel("INT 87:"));
		bomb2 = new JTextField();
		bomb2.setEditable(false);
		regs.add(bomb2);
		
		add(regs);
		
		
		
		JPanel flags = new JPanel(new GridLayout(0, 18));
		
		flags.add(new JLabel("OF:"));
		flagOF = new JCheckBox();
		flagOF.setEnabled(false);
		flags.add(flagOF);
		
		flags.add(new JLabel("DF:"));
		flagDF = new JCheckBox();
		flagDF.setEnabled(false);
		flags.add(flagDF);
		
		flags.add(new JLabel("IF:"));
		flagIF = new JCheckBox();
		flagIF.setEnabled(false);
		flags.add(flagIF);
		
		flags.add(new JLabel("TF:"));
		flagTF = new JCheckBox();
		flagTF.setEnabled(false);
		flags.add(flagTF);
		
		flags.add(new JLabel("SF:"));
		flagSF = new JCheckBox();
		flagSF.setEnabled(false);
		flags.add(flagSF);
		
		flags.add(new JLabel("ZF:"));
		flagZF = new JCheckBox();
		flagZF.setEnabled(false);
		flags.add(flagZF);
		
		flags.add(new JLabel("AF:"));
		flagAF = new JCheckBox();
		flagAF.setEnabled(false);
		flags.add(flagAF);
		
		flags.add(new JLabel("PF:"));
		flagPF = new JCheckBox();
		flagPF.setEnabled(false);
		flags.add(flagPF);
		
		flags.add(new JLabel("CF:"));
		flagCF = new JCheckBox();
		flagCF.setEnabled(false);
		flags.add(flagCF);
		
		add(flags);
	}
	
	public void updateState(CpuState state)
	{
		if (state == null)
		{
			regAX.setText("");
			regBX.setText("");
			regCX.setText("");
			regDX.setText("");
			
			regDS.setText("");
			regES.setText("");
			regSI.setText("");
			regDI.setText("");
			
			regSS.setText("");
			regSP.setText("");
			regBP.setText("");
			regNRG.setText("");
			
			regCS.setText("");
			regIP.setText("");
			bomb1.setText("");
			bomb2.setText("");
			
			flagOF.setSelected(false);
			flagDF.setSelected(false);
			flagIF.setSelected(false);
			flagTF.setSelected(false);
			flagSF.setSelected(false);
			flagZF.setSelected(false);
			flagAF.setSelected(false);
			flagPF.setSelected(false);
			flagCF.setSelected(false);
		}
		else
		{
			regAX.setText(Disassembler.toString(state.getAX()));
			regBX.setText(Disassembler.toString(state.getBX()));
			regCX.setText(Disassembler.toString(state.getCX()));
			regDX.setText(Disassembler.toString(state.getDX()));
			
			regDS.setText(Disassembler.toString(state.getDS()));
			regES.setText(Disassembler.toString(state.getES()));
			regSI.setText(Disassembler.toString(state.getSI()));
			regDI.setText(Disassembler.toString(state.getDI()));
			
			regSS.setText(Disassembler.toString(state.getSS()));
			regSP.setText(Disassembler.toString(state.getSP()));
			regBP.setText(Disassembler.toString(state.getBP()));
			regNRG.setText(Disassembler.toString(state.getEnergy()));
			
			regCS.setText(Disassembler.toString(state.getCS()));
			regIP.setText(Disassembler.toString(state.getIP()));
			bomb1.setText(state.getBomb1Count() + "");
			bomb2.setText(state.getBomb2Count() + "");
			
			flagOF.setSelected(state.getOverflowFlag());
			flagDF.setSelected(state.getDirectionFlag());
			flagIF.setSelected(state.getInterruptFlag());
			flagTF.setSelected(state.getTrapFlag());
			flagSF.setSelected(state.getSignFlag());
			flagZF.setSelected(state.getZeroFlag());
			flagAF.setSelected(state.getAuxFlag());
			flagPF.setSelected(state.getParityFlag());
			flagCF.setSelected(state.getCarryFlag());
		}
	}
	
	public void setAllBackground(Color c)
	{
		regAX.setBackground(c);
		regBX.setBackground(c);
		regCX.setBackground(c);
		regDX.setBackground(c);
		
		regDS.setBackground(c);
		regES.setBackground(c);
		regSI.setBackground(c);
		regDI.setBackground(c);
		
		regSS.setBackground(c);
		regSP.setBackground(c);
		regBP.setBackground(c);
		regNRG.setBackground(c);
		
		regCS.setBackground(c);
		regIP.setBackground(c);
		bomb1.setBackground(c);
		bomb2.setBackground(c);
		
		flagOF.setBackground(c);
		flagDF.setBackground(c);
		flagIF.setBackground(c);
		flagTF.setBackground(c);
		flagSF.setBackground(c);
		flagZF.setBackground(c);
		flagAF.setBackground(c);
		flagPF.setBackground(c);
		flagCF.setBackground(c);
	}
}