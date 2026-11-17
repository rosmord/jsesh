package jsesh.mdcDisplayer.swing.editorApplication;

import javax.swing.UIManager;

import org.qenherkhopeshef.utils.PlatformDetection;


/**
 * @deprecated
 * @author rosmord
 *
 */
public class Main {
	public static void main(String[] args)  {
		// We should have a more flexible look and feel system.
		// However, for Linux, I like liquid.
		try {
			if (PlatformDetection.getPlatform() == PlatformDetection.UNIX) {
				//UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");		
			} else if (PlatformDetection.getPlatform() == PlatformDetection.WINDOWS) {
	
				UIManager
						.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
				//UIManager.setLookAndFeel("net.java.plaf.windows.WindowsLookAndFeel;");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MDCEditorApplication application= new MDCEditorApplication();
		// Show Splash screen
		application.showSplash();
		// Prepare application (non-swing related stuff).
		application.initData();
		// Show application.
		application.createSwingUI();
		application.show();
	}
}
