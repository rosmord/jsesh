package org.qenherkhopeshef.guiFramework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.UIDefaults;

/**
 * Automatically build actions from a specification in the ActionNames file and
 * preconditions files. Register them with an activation manager.
 * 
 * <p>
 * It is possible to loop through all action names using the iterator or the
 * foreach loop.
 * 
 * <pre>
 * for (String actionName: actionCatalogue) {
 * 	  Action a= actionCatalogue.getAction(actionName);
 *   ...
 * }
 * </pre>
 * 
 * @author rosmord
 */

public class ActionCatalogue implements Iterable<String> {
	private PropertyHolder actionDelegate;
	private Map<String, Action> actionCatalog = new HashMap<String, Action>();
	private ActionActivationManager actionActivationManager;
	private AppDefaults defaults = new AppDefaults();

	/**
	 * Initialize an action factory for the application.
	 * <p>
	 * action descriptions will be read from various sources.
	 * <p>
	 * Once created, the actions will be stored in actionCatalog.
	 */

	public ActionCatalogue(PropertyHolder actionDelegate) {
		this.actionDelegate = actionDelegate;
	}

	/**
	 * Returns the object responsible for both performing the actions and
	 * triggering actions activation.
	 * 
	 * @return
	 */
	public PropertyHolder getActionDelegate() {
		return actionDelegate;
	}

	/**
	 * Returns the catalogue of actions as an action map.
	 * 
	 * @return
	 */
	public Map<String, Action> getActionMap() {
		return new HashMap<String, Action>(actionCatalog);
	}

	/**
	 * Add all actions in this catalogue to a specific swing action map.
	 * 
	 * @param swingActionMap
	 */
	public void addToActionMap(ActionMap swingActionMap) {
		for (Map.Entry<String, Action> entry : actionCatalog.entrySet()) {
			swingActionMap.put(entry.getKey(), entry.getValue());
		}
	}

	public ActionActivationManager getActionActivationManager() {
		if (actionActivationManager == null)
			this.actionActivationManager = new ActionActivationManager();
		return actionActivationManager;
	}

	public void putAction(String actionName, Action action) {
		actionCatalog.put(actionName, action);
	}

	public Action getAction(String s) {
		return actionCatalog.get(s);
	}

	/**
	 * Add a I8n resource description to the application. Issues a (written)
	 * warning if it supposes the bundle can't be found. If the path lacks the
	 * package, assume it's in the same place as the delegate.
	 * <p>
	 * <strong>Important</strong> the resource should be in a package.
	 * <p>
	 * The resource might be relative to the action delegate class, or given as
	 * an absolute resource path.
	 * 
	 * @param path
	 *            : a path for I8n resource of the form "package.resourceI8n".
	 */
	public void addResourceBundle(String path) {
		// We try to be as user-friendly as possible. 
		// We complete the path with the action delegate package name if needed.
		if (path.indexOf('.') == -1) {
			path = getActionDelegateClass().getPackage().getName() + "." + path;
		}
		// Then, we check if the corresponding basic resource (the default properties file) exists.
		// (the reason is that addResourceBundle silently fails if the bundle is not available).
		String filePath = "/" + path.replace('.', '/');
		if (this.getClass().getResource(filePath + ".properties") == null) {
			System.err.println("Bundle " + path
					+ ".properties seems not to be there.");
			System.err
					.println("Make sure you used the full name with packages.");
		}
		// Now, we have used the filePath to check the resource existence.
		// We use the bundle name (with "." and not "/") to proceed.
		defaults.addResourceBundle(path);
	}

	public AppDefaults getDefaults() {
		return defaults;
	}

	/*
	 * NOT IMPLEMENTED YET. If this is true, actions without a proxy will be
	 * active only when there is a document to work on. More specifically, they
	 * will depend on the "DocumentEditor" property.
	 */
	// private boolean preconditionIfNoProxy= false;

	/**
	 * Create actions from a text file.
	 * 
	 * <p>
	 * The file should contain action names, one on each line. empty lines and
	 * "#" comments lines are also possible, as well as "separators". Leading
	 * spaces are ignored. (well, the rule is that we can read menu description
	 * as action lists).
	 * 
	 * @param source
	 * @throws IOException
	 */

	public void buildActionsFromText(Reader source) throws IOException {

		BufferedReader reader = new BufferedReader(source);

		String line;

		try {
			while ((line = reader.readLine()) != null) {
				String actionName = line.trim();
				if ("".equals(actionName) || actionName.startsWith("#")
						|| actionName.startsWith("-"))
					continue;

				// Create the corresponding action, using the available data in
				// system resources.
				BundledAction action = createAction(actionName, getDefaults());
				putAction(action.getActionName(), action);
				// Register the action in order to know if it's enabled or not.
				registerAction(action);
			}
		} finally {
			reader.close();
		}
	}

	private void registerAction(BundledAction action) {
		if (action.getPreconditions() != null
				&& action.getPreconditions().length != 0) {
			getActionActivationManager().registerAction(action);
		}

	}

	/**
	 * Factory method for creating the actual BundleActions. If one wants to use
	 * a subclass of BundleAction, it's always possible to redefine this method.
	 * 
	 * @param actionName
	 * @param defaults
	 * @return
	 */

	protected BundledAction createAction(String actionName, AppDefaults defaults) {
		return new BundledAction(getActionDelegate(), actionName, defaults);
	}

	/**
	 * Return the object on which the action will be called.
	 * 
	 * @return the facade
	 */
	public PropertyHolder getFacade() {
		return getActionDelegate();
	}

	/**
	 * Returns an iterator to all actions names.
	 */
	public Iterator<String> iterator() {
		return actionCatalog.keySet().iterator();
	}

	private Class<? extends PropertyHolder> getActionDelegateClass() {
		return getActionDelegate().getClass();
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
	void addActionList(InputStream stream) throws IOException {
		Reader actionNamesSource = new InputStreamReader(stream, "UTF-8");
		// Use the action factory to build the actions.
		buildActionsFromText(actionNamesSource);
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
		InputStream stream = getActionDelegateClass().getResourceAsStream(
				actionListRessourceName);
		if (stream == null)
			System.err.println("Could not read " + actionListRessourceName);
		addActionList(stream);
	}

}
