package jsesh.newEdit;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Main {
	private JFrame frame= new JFrame("display");
	private NewMDCEditor newMDCEditor= new  NewMDCEditor();
	private JTextField mdcTextField= new JTextField();
	
	public Main() {		
		Container panel = frame.getContentPane();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c= new GridBagConstraints();
		JScrollPane scroll= new JScrollPane(newMDCEditor);
		panel.setPreferredSize(new Dimension(640, 480));
		c.fill= GridBagConstraints.BOTH;
		c.weightx= 1;
		c.weighty= 1;
		panel.add(scroll,c);
		c= new GridBagConstraints();
		c.gridy= 1;
		mdcTextField.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setMdC();
			}
		});
		panel.add(mdcTextField);		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	protected void setMdC() {
		String mdc= mdcTextField.getText();
		newMDCEditor.setMdC(mdc);
	}

	public static void main(String[] args) {
		
	}
}
