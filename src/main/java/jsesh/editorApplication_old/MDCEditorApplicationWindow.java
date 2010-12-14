package jsesh.editorApplication_old;

import java.awt.BorderLayout;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import org.qenherkhopeshef.guiFramework.AppDefaults;

//import jsesh.swing.signPalette.HieroglyphicPalette;

/**
 * 
 * @author rosmord
 *
 */
public class MDCEditorApplicationWindow extends JFrame {

	private JDesktopPane desktopPane;

	//private HieroglyphicPalette hieroglyphicPalette;
	
	
	public MDCEditorApplicationWindow(AppDefaults defaults) {
		super(defaults.getString("JSESH.LABEL"));
		desktopPane= new JDesktopPane();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(desktopPane,BorderLayout.CENTER);
		//buildPalette(defaults);
		//getContentPane().add(hieroglyphicPalette.getPanel(), BorderLayout.EAST);
		setSize(800,600);
	}

//	private void buildPalette(AppDefaults defaults) {
//		hieroglyphicPalette = new HieroglyphicPalette();
//	}
//	
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
	
//	public HieroglyphicPalette getHieroglyphicPalette() {
//		return hieroglyphicPalette;
//	}
//	
//	public void showPalette(boolean b) {
//		boolean bo= hieroglyphicPalette.getPanel().isVisible();
//		hieroglyphicPalette.getPanel().setVisible(! bo);
//	}
}
