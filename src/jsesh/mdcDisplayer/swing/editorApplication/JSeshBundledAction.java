package jsesh.mdcDisplayer.swing.editorApplication;

import javax.swing.Action;
import javax.swing.ImageIcon;

import jsesh.swingUtils.ImageIconFactory;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledAction;
import org.qenherkhopeshef.guiFramework.PropertyHolder;

/**
 * Variant of Bundled Action for JSesh.
 * <p>
 * Introduces Icons with manuel de codage description.
 * <p> It would make it possible to Localize our system for ancient Thebes.
 * @author rosmord
 * 
 */

public class JSeshBundledAction extends BundledAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2323730038649068218L;
	
	/**
	 * Action property: Manuel de codage source for an icon.
	 */
	public static final String ICON_MDC = "IconMdC";

	/**
	 * Create a JSeshBundledAction, the same as a BundledAction, but 
	 * with the possibility of a Manuel de Codage icon.
	 * 
	 * @param target
	 * @param actionName
	 * @param defaults
	 */
	public JSeshBundledAction(PropertyHolder target, String actionName,
			AppDefaults defaults) {
		super(target, actionName, defaults);
		String mk = actionName + "." + ICON_MDC;
		if (defaults.getString(mk) != null) {
			putValue(Action.SMALL_ICON, buildIcon(defaults.getString(mk)));
		}
	}

	private ImageIcon buildIcon(String string) {
		return ImageIconFactory.buildImage(string);
	}

}
