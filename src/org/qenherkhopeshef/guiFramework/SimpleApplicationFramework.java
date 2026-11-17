package org.qenherkhopeshef.guiFramework;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JMenuBar;

/**
 * Very simple facade for building applications menus
 */
public class SimpleApplicationFramework {

	/**
	 * Application default data.
	 */
	private AppDefaults appDefaults = new AppDefaults();

	/**
	 * Actions catalog.
	 */
	private HashMap actionCatalog = new HashMap();

	private JMenuBar jMenuBar;

	private PropertyHolder actionDelegate;

	public SimpleApplicationFramework(PropertyHolder actionDelegate) {
		this.actionDelegate= actionDelegate;
	}
	

	/**
	 * All in one simple constructor for the action and menu system. Once called, you
	 * can retrieve the menu bar and the actions.
	 * 
	 * @param ressourceI8n
	 * @param menuPath
	 * @param actionDelegate
	 * @throws IOException
	 */
	public SimpleApplicationFramework(String ressourceI8n,
			String menuPath,
			PropertyHolder actionDelegate) {
		try {
			this.actionDelegate = actionDelegate;
			addRessourceBundle(ressourceI8n);
			addActionList(menuPath);
			buildMenu(menuPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add a I8n resource description to the application.
	 * Issues a (written) warning if it supposes the bundle can't be found.
	 * If the path lacks the package, assume it's in the same place as the delegate.
	 * <p><strong>Important</strong> the resource should be in a package.
	 * @param path : a complete path for I8n resource of the form "package.resourceI8n".
	 */
	public void addRessourceBundle(String path) {
		// We try to be as user-friendly as possible.
		if (path.indexOf('.') == -1) {
			path= actionDelegate.getClass().getPackage().getName() + "." + path;
		}
		String filePath= "/" + path.replace('.', '/');
		if (this.getClass().getResource(filePath+ ".properties") == null) {
			System.err.println("Bundle " + path+ ".properties seems not to be there.");
			System.err.println("Make sure you used the full name with packages.");
		}
		appDefaults.addResourceBundle(path);
	}

	/**
	 * Gets the application defaults
	 * @return
	 */
	public AppDefaults getAppDefaults() {
		return appDefaults;
	}
	
	/**
	 * Load a list of action id and build the actions.
	 * To avoid unnecessary work, the format used to describe the action IDs is 
	 * the same as the one used to describe the menus.
	 * 
	 * <p> Note that creating a menu will automatically create the corresponding actions,
	 * so this method will only be called for actions not linked to a menu.
	 *
	 * @param actionListRessourceName
	 * @throws IOException
	 */
	public void addActionList(String actionListRessourceName) throws IOException {
		InputStream stream = actionDelegate
				.getClass().getResourceAsStream(actionListRessourceName);
		if (stream == null)
			System.err.println("Could not read "+ actionListRessourceName);
		addActionList(stream);
	}


	private void addActionList(InputStream stream)
			throws  IOException {
		Reader actionNamesSource = new InputStreamReader(stream,
				"UTF-8");		
		// Use the action factory to build the actions.
		new ActionFactory(actionDelegate, actionCatalog, appDefaults)
				.buildActionsFromText(actionNamesSource);
	}
	
	


	/**
	 * Returns the catalog of actions (indexed by action ids).
	 * 
	 * @return
	 */
	public HashMap getActionCatalog() {
		return actionCatalog;
	}

	/**
	 * build a menu according to a description.
	 * 
	 * @param menuPath
	 * @return
	 * @throws IOException
	 * 
	 */

	public JMenuBar buildMenu(String menuPath) throws IOException {
		// Create the menu.
		MenubarFactory menubarFactory = new MenubarFactory(actionCatalog);
		InputStream menuDescription = actionDelegate.getClass()
				.getResourceAsStream(menuPath);
		if (menuDescription == null)
			System.err.println("Could not read " + menuDescription);
		jMenuBar = menubarFactory.buildMenuBar(new InputStreamReader(
				menuDescription, "UTF-8"));
		return jMenuBar;
	}


	public JMenuBar getJMenuBar() {
		return jMenuBar;
	}



	public Action getAction(String string) {
		return (Action) actionCatalog.get(string);
	}
}
