package org.qenherkhopeshef.guiFramework;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.UIDefaults;

/**
 * Application data and actions.
 * 
 * @author rosmord
 * 
 */
public class ApplicationSkeleton {
	private PropertyHolder actionDelegate;
	private Map<String, Action> actionCatalog = new HashMap<String, Action>();
	private ActionActivationManager actionActivationManager;
	private AppDefaults defaults = new AppDefaults();

	public ApplicationSkeleton(PropertyHolder actionDelegate) {
		this.actionDelegate = actionDelegate;
	}

	public PropertyHolder getActionDelegate() {
		return actionDelegate;
	}

	public Map<String, Action> getActionCatalog() {
		return actionCatalog;
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
	 * adds a resource bundle of internationalization.
	 * @see UIDefaults#addResourceBundle(String)
	 * @param bundleName
	 */
	public void addResourceBundle(String bundleName) {
		defaults.addResourceBundle(bundleName);
	}

	public AppDefaults getDefaults() {
		return defaults;
	}
}
