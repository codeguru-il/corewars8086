package il.co.codeguru.corewars8086.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RegisterField extends JPanel {

	private JTextField textField;

	public RegisterField(String name) {

		super.setLayout(new GridLayout(1, 2));
		super.setSize(50, 20);
		super.add(new JLabel(name + ":"), BorderLayout.LINE_START);

		textField = new JTextField(2);

		super.add(textField, BorderLayout.LINE_START);
	}

	public void setValue(short value) {
		textField.setText(String.format("%04X", value).toUpperCase());
	}

	public short getValue() throws Exception {
		return (short) Integer.parseInt(textField.getText(), 16);
	}

}