package jsesh.newEdit;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;

public class Main {
	private final JFrame frame= new JFrame("display");
	private final NewMDCEditor newMDCEditor; 
	private final JTextField mdcTextField= new JTextField();
	
	public Main() {	
		HieroglyphicFontManager fontManager = new DefaultHieroglyphicFontManager();
		newMDCEditor = new NewMDCEditor(fontManager);
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
