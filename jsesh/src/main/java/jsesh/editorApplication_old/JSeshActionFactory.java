package jsesh.editorApplication_old;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.ActionCatalogue;
import org.qenherkhopeshef.guiFramework.BundledAction;
import org.qenherkhopeshef.guiFramework.PropertyHolder;


public class JSeshActionFactory extends ActionCatalogue {

	public JSeshActionFactory(PropertyHolder facade) {
		super(facade);
	}

	/* (non-Javadoc)
	 * @see jsesh.uiFramework.ActionFactory#createAction(java.lang.String, jsesh.uiFramework.AppDefaults)
	 */
	protected BundledAction createAction(String actionName, AppDefaults defaults) {
		return new JSeshBundledAction(getFacade(), actionName, defaults);
	}

	
}
