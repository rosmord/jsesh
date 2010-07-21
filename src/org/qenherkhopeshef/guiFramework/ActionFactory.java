package org.qenherkhopeshef.guiFramework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;


/**
 * Automatically build actions from a specification in the ActionNames file and properties files.
 * Register them with an activation manager.
 * @author rosmord
 */

public class ActionFactory {

	private PropertyHolder facade;
	private AppDefaults defaults;
	private Map actionCatalog;
	private ActionActivationManager actionActivationManager;

    /**
     * NOT IMPLEMENTED YET.
     * If this is true, actions without a proxy will be active only when there is a document to work on.
     * More specifically, they will depend on the "DocumentEditor" property.
     */
    private boolean preconditionIfNoProxy= false;

    
	/**
	 * Initialize an action factory for the application.
	 * <p> action descriptions will be read from various sources.
	 * <p> Once created, the actions will be stored in actionCatalog.
	 * @param facade the application facade on which the action will be applied.
	 * @param actionCatalog a map (<String,BundleAction>) which links action id to actions. 
	 * @param defaults
	 */

	public ActionFactory(PropertyHolder facade, Map actionCatalog, AppDefaults defaults) {
		super();
		this.facade= facade;
		this.defaults = defaults;
		this.actionCatalog= actionCatalog;
	}


	/**
	 * Create actions from a text file.
	 * 
	 * <p>The file should contain action names, one on each line.
	 * empty lines and "#" comments lines are also possible, as well as "separators".
	 * Leading spaces are ignored.
	 * (well, the rule is that we can read menu description as action lists).
	 * 
	 * @param source
	 * @throws IOException 
	 */
	
	public void buildActionsFromText(Reader source) throws IOException 
		{
		
		BufferedReader reader= new BufferedReader(source); 
		
		String line;
		
		try {
			while ((line= reader.readLine()) != null) {
					String actionName= line.trim();
					if ("".equals(actionName) || actionName.startsWith("#") || actionName.startsWith("-"))
						continue;

					// Create the corresponding action, using the available data in
					// system resources.
					BundledAction action= createAction(actionName,defaults); 
					actionCatalog.put(action.getActionName(), action);
					// Register the action in order to know if it's enabled or not.
					registerAction(action);
			}
		} finally {
			reader.close();
		}	
	}

	
	private void registerAction(BundledAction action) {
		if (action.getPreconditions() != null) {
			getActionActivationManager().registerAction(action);
		}
		
	}


	private ActionActivationManager getActionActivationManager() {
		if (actionActivationManager == null)
			actionActivationManager= new ActionActivationManager();
		return actionActivationManager;
	}
	/**
	 * Factory method for creating the actual BundleActions. 
	 * If one wants to use a subclass of BundleAction, it's always possible to redefine this method.
	 * @param actionName
	 * @param defaults
	 * @return
	 */
	
	protected BundledAction createAction(String actionName, AppDefaults defaults) {
		return new BundledAction(facade,
				actionName, defaults);
	}


	/**
	 * Return the object on which the action will be called.
	 * @return the facade
	 */
	public PropertyHolder getFacade() {
		return facade;
	}
	
	

}
