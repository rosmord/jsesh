package jsesh.mdcDisplayer.swing.editorApplication;



/**
 * @deprecated
 * @author rosmord
 */
public class Main {
	public static void main(String[] args)  {
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
