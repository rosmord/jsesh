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
		panel.setLayout(new BorderLayout());
		JScrollPane scroll= new JScrollPane(newMDCEditor);
		panel.setPreferredSize(new Dimension(640, 480));
		panel.add(scroll,BorderLayout.CENTER);
		mdcTextField.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setMdC();
			}
		});
		mdcTextField.setText("p*t:pt");
		panel.add(mdcTextField, BorderLayout.SOUTH);		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	protected void setMdC() {
		String mdc= mdcTextField.getText();
		newMDCEditor.setMdC(mdc);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
                    
                    @Override
                    public void run() {
                        new Main();
                    }
                });
	}
}
