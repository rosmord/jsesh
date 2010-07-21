package jsesh.macSpecific;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppli;
import jsesh.mdcDisplayer.swing.splash.SplashScreen;
import ch.randelshofer.quaqua.QuaquaManager;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * Sets up and start a mac-aware version of JSesh. This should be compiled and
 * saved separately, so that jsesh can be built on any computer.
 * 
 * @author rosmord
 */

public class JSeshMacApplication {

	private class JSeshApplicationListener extends ApplicationAdapter {
		public void handleQuit(ApplicationEvent arg0) {
			standardJSeshAppli.quit();
		}

		/**
		 * Open a file. thread secure way.
		 */
		public void handleOpenFile(ApplicationEvent arg0) {
			System.err.println("Opening " + arg0.getFilename());
			if (arg0.getFilename() == null)
				return;
			// Beware: jsesh might not be running, and in any case we
			// are not running in the graphic manager thread.
			//
			// hence, we must
			// a) get sure the file name is stored somewhere
			// b) get sure we send the "openfile" order in due time.
			// to do this, we use a thread which will wait for the application
			// to be
			// running.

			new FileOpener(new File(arg0.getFilename())).start();
			/**
			 * 
			 * This would more or less work in the future. MDCDisplayerAppli
			 * appli= new MDCDisplayerAppli(); File f= new
			 * File(arg0.getFilename()); appli.setInitialFile(f);
			 * appli.setVisible(true); arg0.isHandled();
			 */
		}

	}

	private MDCDisplayerAppli standardJSeshAppli;

	// volatile évite les optimisations mal venues.
	private volatile boolean applicationRunning = false;

	public JSeshMacApplication() {
		Application application = Application.getApplication();
		application.addApplicationListener(new JSeshApplicationListener());
		final SplashScreen splash = new SplashScreen();
		splash.display();

		// Quick hack.
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
		}

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					standardJSeshAppli = MDCDisplayerAppli
							.startAppli(new String[0]);
					standardJSeshAppli.setSplash(splash);
				}
			});
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		setApplicationRunning(true);
	}

	public synchronized void setApplicationRunning(boolean applicationRunning) {
		this.applicationRunning = applicationRunning;
		notifyAll();
	}

	public static void main(String[] args) {
		System.out.println(System.getProperty("java.runtime.version"));
		if (!System.getProperty("java.runtime.version").startsWith("1.4."))
			try {
				// System.setProperty("apple.laf.useScreenMenuBar", "true");
				//System.setProperty("Quaqua.requestFocusEnabled", "true");

				// I don't like Quaqua/Apple guidelines behaviour with combobox.
				// shortly: apparently, when
				// a combobox is selected with the keyboard, you can navigate it
				// with the keyboard (because
				// it gets the focus). Not when it was selected with the mouse.
				// Quaqua.requestFocusEnabled disables this.
				// However, the value of the combobox is changed as soon as the
				// key moves, which is slow in my case (draws hieroglyphs for each move), 

				Set includes = buildUIDelegateList();

				QuaquaManager.setIncludedUIs(includes);
				
				/*QuaquaManager.setExcludedUIs(new HashSet(Arrays
						.asList(new String[] { "Combobox", "PopupMenu",
								"TabbedPane" })));*/
				
				//Set includes = QuaquaManager.getIncludedUIs();
				//System.out.println("Includes " + includes);

				// QuaquaManager.setExcludedUIs(new HashSet(Arrays.asList(new
				// String[]{"Combobox"})));

				UIManager
						.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");

			} catch (Exception e) {
				e.printStackTrace();
			}
		MDCDisplayerAppli.prepareApplication();
		new JSeshMacApplication();
	}

	/**
	 * Build the list of UI delegates to use in Quaqua. See my problems with
	 * comboboxes.
	 * 
	 * @return
	 */
	private static Set buildUIDelegateList() {
		Set includes = new HashSet();

		includes.add("Browser");
		includes.add("Button");
		includes.add("CheckBox");
		includes.add("ColorChooser");
		includes.add("ColorSlider");
		// includes.add("ComboBox");
		includes.add("DesktopPane");
		includes.add("EditorPane");
		includes.add("FileChooser");
		includes.add("FormattedTextField");
		includes.add("Label");
		includes.add("List");
		includes.add("MenuBar");
		includes.add("MenuItem");
		includes.add("Menu");
		includes.add("NavigatableTabbedPane");
		includes.add("OptionPane");
		includes.add("Panel");
		includes.add("PasswordField");
		// includes.add("PopupMenu");
		includes.add("RadioButton");
		includes.add("RootPane");
		includes.add("ScrollBar");
		includes.add("ScrollPane");
		includes.add("ScrollTabbedPane");
		includes.add("Separator");
		includes.add("Slider");
		includes.add("Spinner");
		includes.add("SplitPane");
		// includes.add("TabbedPane");
		includes.add("TableHeader");
		includes.add("Table");
		includes.add("TextArea");
		includes.add("TextField");
		includes.add("TextPane");
		includes.add("ToggleButton");
		includes.add("ToolBarSeparator");
		includes.add("ToolBar");
		includes.add("Tree");
		includes.add("Viewport");
		includes.add("BasicBrowser");
		includes.add("Component");
		return includes;
	}

	synchronized boolean isApplicationRunning() {
		return applicationRunning;
	}

	synchronized void openFileWhenApplicationIsRunning(File file) {
		while (!applicationRunning) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		final File fileToOpen = file;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					standardJSeshAppli.openFile(fileToOpen);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	class FileOpener extends Thread {
		File fileToOpen;

		public FileOpener(File fileToOpen) {
			super();
			this.fileToOpen = fileToOpen;
		}

		public void run() {
			openFileWhenApplicationIsRunning(fileToOpen);
		}
	}
}
