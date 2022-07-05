package jsesh.jhotdraw.actions;

import java.lang.reflect.Field;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import jsesh.resources.JSeshMessages;

import jsesh.swing.utils.ImageIconFactory;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Singleton Bundle for resources.
 * 
 * @author rosmord
 * 
 */
public class BundleHelper {

	private static final BundleHelper instance = new BundleHelper();

	private final ResourceBundleUtil resourceBundleUtil;

	private BundleHelper() {
		resourceBundleUtil = ResourceBundleUtil
				.getBundle(JSeshMessages.getBundleName());                
	}

	/**
	 * Configures an action.
	 * <p>
	 * Dev note: it would be cool for actions to have a getID method, specified
	 * in an interface. This way, it would be possible to extract the ID from
	 * the action object.
	 * 
	 * @param action
	 *            the action object
	 * @param ID
	 *            the action class unique string id. Usually a static field in
	 *            the action class.
	 * @return the configured action.
	 */
	public Action configure(Action action, String ID) {
		resourceBundleUtil.configureAction(action, ID);
		// Mdc Icon if available :
		String mdcIcon= resourceBundleUtil.getString(ID + ".mdcIcon");
		if (mdcIcon != null && ! "".equals(mdcIcon) && ! (ID+".mdcIcon").equals(mdcIcon)) {
			action.putValue(Action.SMALL_ICON, ImageIconFactory.getInstance().buildImage(mdcIcon));
		}
		return action;
	}

	/**
	 * Configure a menu item. To ease on-the flight creation of menu items, this
	 * methods retuns the item. One can thus write things like:
	 * 
	 * <pre>
	 * menu.addAll(BundleHelper.configure(new JMenuItem(), &quot;file.send&quot;));
	 * </pre>
	 * 
	 * @param menu
	 *            the menu item to configure
	 * @param argument
	 *            a key string identifying the menu.
	 */
	public JMenuItem configure(JMenuItem menu, String argument) {
		resourceBundleUtil.configureMenu(menu, argument);
		return menu;
	}

	/**
	 * Configure a menu . To ease on-the flight creation of menu items, this
	 * methods retuns the item. One can thus write things like:
	 * 
	 * <pre>
	 * menu.addAll(BundleHelper.configure(new JMenuItem(), &quot;file.send&quot;));
	 * </pre>
	 * 
	 * @param menu
	 *            the menu item to configure
	 * @param argument
	 *            a key string identifying the menu.
	 */
	public JMenu configure(JMenu menu, String argument) {
		resourceBundleUtil.configureMenu(menu, argument);
		return menu;
	}

	/**
	 * Configures an action for the syntax editor, using the action ID field to
	 * identify it.
	 * <p>
	 * the action class <b>must</b> have a static String field called ID.
	 * 
	 * @param action
         * @return the action which has just been configured.
	 */
	public Action configure(Action action) {
		try {
			Class<? extends Action> clazz = action.getClass();
			Field field = clazz.getField("ID");
			String id = (String) field.get(null);
			configure(action, id);
			return action;
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Action class needs an ID "
					+ action.getClass(), e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Action class needs a STRING ID "
					+ action.getClass(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Can't access field ID for "
					+ action.getClass(), e);
		}
	}

	public String getLabel(String code) {
		return resourceBundleUtil.getString(code);
	}
	
	public String getFormatedLabel(String code, String ... strings ) {
		return resourceBundleUtil.getFormatted(code, (Object[])strings);
	}
	
	public static BundleHelper getInstance() {
		return instance;
	}
}
