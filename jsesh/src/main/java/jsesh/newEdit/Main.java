package jsesh.newEdit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;

public class Main {
	private final JFrame frame = new JFrame("display");
	private final NewMDCEditor newMDCEditor;
	private final JTextField mdcTextField = new JTextField();

	public Main() {
		HieroglyphShapeRepository fontManager = HieroglyphShapeRepository.getStandardShapeRepository();
		newMDCEditor = new NewMDCEditor(fontManager);
		Container panel = frame.getContentPane();
		panel.setLayout(new BorderLayout());
		JScrollPane scroll = new JScrollPane(newMDCEditor);
		panel.setPreferredSize(new Dimension(640, 480));
		panel.add(scroll, BorderLayout.CENTER);
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
		String mdc = mdcTextField.getText();
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
