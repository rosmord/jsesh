package org.qenherkhopeshef.guiFramework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Very simple facade for building applications menus for one specific window.
 * (Former SimpleApplicationFramework).
 */

public class SimpleApplicationFactory {

	private ActionCatalogue actionCatalogue;

	/**
	 * The menu bar (used only for simple applications).
	 */
	private JMenuBar jMenuBar;

	/**
	 * Map associating action names to the corresponding menu element.
	 */
	private Map<String, JMenuItem> menuMap = new HashMap<String, JMenuItem>();

	/**
	 * A basic constructor to build a "raw" application. All actions and menus
	 * must be added afterwards.
	 * 
	 * @param actionDelegate
	 */
	public SimpleApplicationFactory(PropertyHolder actionDelegate) {
		this.actionCatalogue = new ActionCatalogue(actionDelegate);
		;
	}

	/**
	 * All in one simple constructor for the action and menu system. Once
	 * called, you can retrieve the menu bar and the actions.
	 * 
	 * @param ressourceI8n
	 *            a complete I18n preconditions ressource path, e.g.
	 *            org.toto.actionsI18n
	 * @param menuPath
	 *            the menu definition (a ressource relative to the current
	 *            path).
	 * @param actionDelegate
	 *            the class which will be used for the actions.
	 * @throws IOException
	 */
	public SimpleApplicationFactory(String ressourceI8n, String menuPath,
			PropertyHolder actionDelegate) {
		try {
			this.actionCatalogue = new ActionCatalogue(actionDelegate);
			addRessourceBundle(ressourceI8n);
			addActionList(menuPath);
			buildMenuBar(menuPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add a I8n resource description to the application. Issues a (written)
	 * warning if it supposes the bundle can't be found. If the path lacks the
	 * package, assume it's in the same place as the delegate.
	 * <p>
	 * <strong>Important</strong> the resource should be in a package.
	 * 
	 * @param path
	 *            : a complete path for I8n resource of the form
	 *            "package.resourceI8n".
	 */
	public void addRessourceBundle(String path) {
		actionCatalogue.addResourceBundle(path);
	}

	/**
	 * Gets the application defaults
	 * 
	 * @return
	 */
	public AppDefaults getAppDefaults() {
		return actionCatalogue.getDefaults();
	}

	/**
	 * Load a list of action id and build the actions. To avoid unnecessary
	 * work, the format used to describe the action IDs is the same as the one
	 * used to describe the menus.
	 * 
	 * <p>
	 * Note that creating a menu will automatically create the corresponding
	 * actions, so this method will only be called for actions not linked to a
	 * menu.
	 * 
	 * @param actionListRessourceName
	 * @throws IOException
	 */
	public void addActionList(String actionListRessourceName)
			throws IOException {
		actionCatalogue.addActionList(actionListRessourceName);
	}

	/**
	 * Add a list of actions to create.
	 * <p>
	 * precondition: an action delegate must be set as well as action
	 * descriptions.
	 * 
	 * @param stream
	 * @throws IOException
	 */
	private void addActionList(InputStream stream) throws IOException {
		actionCatalogue.addActionList(stream);
	}

	/**
	 * Returns a copy of the catalog of actions (indexed by action ids) as  map.
	 * 
	 * @return
	 */
	public Map<String, Action> getActionCatalog() {
		return actionCatalogue.getActionMap();
	}

	/**
	 * build a menubar according to a description.
	 * 
	 * <p>
	 * Precondition: A resource bundle and an action delegate must be set.
	 * 
	 * @param menuPath a path to a file resource describing the menu.
	 * @return
	 * @throws IOException
	 */

	public JMenuBar buildMenuBar(String menuPath) throws IOException {
		// Create the menu.
		MenuFactory menuFactory = new MenuFactory(actionCatalogue
				.getActionMap());
		InputStream menuDescription = getActionDelegateClass()
				.getResourceAsStream(menuPath);
		if (menuDescription == null)
			System.err.println("Could not read " + menuDescription);
		jMenuBar = menuFactory.buildMenuBar(new InputStreamReader(
				menuDescription, "UTF-8"));
		menuMap = menuFactory.getActionNamesToMenuMap();
		return jMenuBar;
	}

	
	private Class<? extends PropertyHolder> getActionDelegateClass() {
		return actionCatalogue.getActionDelegate().getClass();
	}

	public JMenuBar getJMenuBar() {
		return jMenuBar;
	}

	/**
	 * Returns the menu corresponding to a given result, or null.
	 * 
	 * @param menuName
	 * @return
	 */
	public JMenu getMenu(String menuName) {
		for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
			JMenu jMenu = getJMenuBar().getMenu(i);
			if (menuName.equals(jMenu.getName()))
				return jMenu;
		}
		return null;
	}

	/**
	 * Build an action map to be used with a custom swing component. The file
	 * containing the list of actions should be in the same place as the action
	 * delegate class.
	 * <p>
	 * keys ???
	 * 
	 * @param actionList
	 *            the name of a file which contains the list of actions to put
	 *            in the map.
	 * @return
	 * @throws IOException
	 */
	public ActionMap buildActionMap(String actionList) throws IOException {
		ActionMap actionMap = new ActionMap();
		InputStream actionInput = getActionDelegateClass().getResourceAsStream(
				actionList);
		if (actionInput == null) {
			System.err.println("Could not read " + actionInput);
			return actionMap;
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(
				actionInput, "UTF-8"));
		String s;
		while ((s = r.readLine()) != null) {
			s = s.trim();
			final Action action = actionCatalogue.getAction(s);
			actionMap.put(s, action);
		}
		return actionMap;
	}

	public Action getAction(String actionName) {
		return actionCatalogue.getAction(actionName);
	}

	public JMenuItem getMenuItemFor(String actionName) {
		return menuMap.get(actionName);
	}
	
	public ActionCatalogue getApplicationSkeleton() {
		return actionCatalogue;
	}
}
