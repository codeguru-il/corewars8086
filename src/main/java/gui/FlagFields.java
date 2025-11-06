package il.co.codeguru.corewars8086.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FlagFields extends JPanel {
	
	private JCheckBox checkBox;
	
	public FlagFields(String name) {
		
		super.setLayout(new GridLayout(1,2));
		super.setSize(20,20);
		super.add(new JLabel(name + ":"),BorderLayout.LINE_START);
		
		checkBox = new JCheckBox(); 
		
		super.add(checkBox,BorderLayout.LINE_START);
	}
	
	public void setValue(boolean value){
		checkBox.setSelected(value);
	}
	
	public boolean getValue(){
		return checkBox.isSelected();
	}
	
}
