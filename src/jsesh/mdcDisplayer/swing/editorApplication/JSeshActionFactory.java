package jsesh.mdcDisplayer.swing.editorApplication;

import java.util.Map;

import org.qenherkhopeshef.guiFramework.ActionFactory;
import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledAction;
import org.qenherkhopeshef.guiFramework.PropertyHolder;


public class JSeshActionFactory extends ActionFactory {

	public JSeshActionFactory(PropertyHolder facade, Map actionCatalog, AppDefaults defaults) {
		super(facade, actionCatalog, defaults);
	}

	/* (non-Javadoc)
	 * @see jsesh.uiFramework.ActionFactory#createAction(java.lang.String, jsesh.uiFramework.AppDefaults)
	 */
	protected BundledAction createAction(String actionName, AppDefaults defaults) {
		return new JSeshBundledAction(getFacade(), actionName, defaults);
	}

	
}
