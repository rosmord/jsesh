package jsesh.mdcDisplayer.swing.editorApplication;

import java.awt.BorderLayout;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

public class MDCEditorApplicationWindow extends JFrame {

	private JDesktopPane desktopPane;

	public MDCEditorApplicationWindow(String title) {
		super(title);
		desktopPane= new JDesktopPane();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(desktopPane,BorderLayout.CENTER);
		setSize(800,600);
	}
	
	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}

	public void addSession(JInternalFrame frame) {
		desktopPane.add(frame);
		frame.setVisible(true);
		frame.moveToFront();
	}
	
	public JInternalFrame getCurrentSession() {
		return desktopPane.getSelectedFrame();
	}
}
