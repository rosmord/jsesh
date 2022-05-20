package jsesh.utilitysoftwares.demos.tests;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A test to exhibit a bug in focus management with jdk1.5, linux and fvwm.
 * @author S. Rosmorduc
 *
 */
public class TestFocus extends JFrame {
	
	public TestFocus() {
		TestPanel p= new TestPanel();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(p, BorderLayout.CENTER);
		//getContentPane().add(new JTextField(), BorderLayout.SOUTH);
		setSize(100,100);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new TestFocus();
	}

}

class TestPanel extends JPanel {
	/**
	 * 
	 */
	public TestPanel() {
		setFocusable(true);
		addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (requestFocusInWindow()) {
							System.out.println("success");
						} else 
							System.out.println("failure");
					}
				}
				);
		
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				System.out.println(e.getKeyChar() + " typed.");
			}
			});
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(200,200);
	}
	
}