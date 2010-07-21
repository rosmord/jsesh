package jsesh.editorApplication_old;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.MenubarFactory;

/**
 * New Application for JSesh 3.0 (and later).
 * <p>
 * Compared with the old (lack of structure), we want a light and clean
 * structure. Actions will be created from a configuration file, and kept in a
 * dictionary, instead of being accessible through a huge number of accessors.
 * 
 * <p> Think a bit more. The application now has too parts
 * a) create the various objects
 * b) Swing related steps.
 * @author rosmord
 * 
 */

public class MDCEditorApplication {

	private static final String MENU_DESCRIPTOR_RESOURCE = "MenuDescriptor.txt";

	/**
	 * File with the codes for "basic" Gardiner signs.
	 */
	
	private static final String STANDARD_CODES_RESOURCE = "StandardCodes.txt";

	/**
	 * The map of all actions. The key used is the "ActionCommandKey", which is
	 * a technical name for the action.
	 */

	HashMap actionCatalog = new HashMap();

	private MDCEditorApplicationWindow mainWindow;

	private JMenuBar menuBar;

	private AppDefaults appDefaults;

	/**
	 * A (more or less) abstract view of the application.
	 */

	private MDCEditorApplicationWorkflow workflow;

	public MDCEditorApplication()  {
		
	}

	/**
	 * Setup which doesn't require access to the screen, and which can be done transparently.
	 *
	 */
	public void initData() {
		buildProperties();
	}

	private void prepareMainWindow() {
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/jseshResources/images/hiboux.png"));

		mainWindow.setIconImage(icon.getImage());
		mainWindow.setJMenuBar(menuBar);
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				workflow.exit();
			}
		});
		workflow.updateActionsStatus();
	}

	private void buildProperties() {
		appDefaults = new AppDefaults();
		appDefaults
				.addResourceBundle("jsesh.editorApplication_old.editorI8n");
	}

	private void buildMenu() throws IOException {
		MenubarFactory menubarFactory = new MenubarFactory(actionCatalog);
		InputStream menuDescription = this.getClass().getResourceAsStream(MENU_DESCRIPTOR_RESOURCE);
		
		menuBar = menubarFactory.buildMenuBar(new InputStreamReader(menuDescription, "UTF-8"));
		// Fill a map with menus for hieroglyphs.
		//JMenu menu = (JMenu)menubarFactory.getItem("hieroglyphsAMenu");
		//menubarFactory.fillJMenu(menu, new InputStreamReader(this.getClass().getResourceAsStream("A.txt"), "UTF-8"));
		
	}

	/**
	 * 
	 */

	public void quit() {
		System.exit(0);
	}

	public void show() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					mainWindow.setVisible(true);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the abstract view of the application.
	 * 
	 * @return
	 */
	public MDCEditorApplicationWorkflow getWorkflow() {
		return workflow;
	}


	public void createSwingUI() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					try {
						mainWindow = new MDCEditorApplicationWindow(appDefaults);
						workflow = new MDCEditorApplicationWorkflow(mainWindow,
								appDefaults);
						Reader actionNamesSource= new InputStreamReader(getClass().getResourceAsStream(MENU_DESCRIPTOR_RESOURCE),"UTF-8");
						new JSeshActionFactory(workflow, actionCatalog, appDefaults).buildActionsFromText(actionNamesSource);
						buildHieroglyphActions();
						buildMenu();
						prepareMainWindow();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Build basic actions for inserting hieroglyphs.
	 */
	protected void buildHieroglyphActions() {
		
	}

	/**
	 * Build the menus for hieroglyphs.
	 */
	private void buildHieroglyphicMenus() {
		
	}
	
	/**
	 * Thread safe method which displays a splash screen.
	 */
	public void showSplash() {
		try {
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
