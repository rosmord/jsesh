package org.qenherkhopeshef.guiFramework;

import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * A class to initialize action data from a given bundle.
 * <p>
 * This class is inspired by Werner Randelshofer's JHotdraw framework.
 * <p>
 * The "old" Qenherkhopeshef framework relies too much on the BundleAction
 * class, and this is not a very "agile" decision.
 * 
 * <p>
 * P.S. We use this class instead of W. Randelshofer's one, because we need this
 * part of JSesh to be independent from JHotdraw.
 * 
 * @author rosmord
 * 
 */
public class BundledActionFiller {
	/**
	 * Boolean conditions that should be met so that the action is possible.
	 */
	public static final String PRECONDITIONS = "Preconditions";
	/**
	 * For Menu actions, optionally, the number of columns.
	 */
	public static final String NUMBER_OF_COLUMNS = "NumberOfColumns";
	/**
	 * Property to control the content of a menu depending on the platform. Some
	 * actions won't appear on certain menus.
	 */
	public final static String actionKeys[] = { Action.NAME,
			Action.SHORT_DESCRIPTION, Action.LONG_DESCRIPTION,
			Action.SMALL_ICON, Action.ACTION_COMMAND_KEY,
			Action.ACCELERATOR_KEY, Action.MNEMONIC_KEY,
			BundledAction.TEAR_OFF, PRECONDITIONS,
			BundledAction.GROUP_PROPERTY, BundledAction.BOOLEAN_PROPERTY,
			BundledAction.METHOD_ARGUMENT, NUMBER_OF_COLUMNS,
			BundledAction.IS_LABELLED, BundledAction.PROXY_METHOD };

	/**
	 * Init an action using its static ID attribute as action key.
	 * <p>
	 * Note ID attribute is a static String attribute which is read using
	 * introspection.
	 * <p>
	 * This method can fail if the ID doesn't exist.
	 * 
	 * @param action
	 * @param appDefaults
	 */
	public static void initAction(Action action, AppDefaults appDefaults) {
		try {
			Class<? extends Action> clazz = action.getClass();
			Field field = clazz.getField("ID");
			String id = (String) field.get(null);
			initActionProperties(action, id, appDefaults);
		} catch (NoSuchFieldException e) {
			throw (new RuntimeException(e));
		} catch (IllegalAccessException e) {
			throw (new RuntimeException(e));
		}
	}

	/**
	 * fill the action information using the default data
	 * 
	 * @param action
	 *            the action object to initialize.
	 * @param id
	 *            the action name (a key).
	 * @param defaults
	 * @throws java.lang.NumberFormatException
	 * @throws java.awt.HeadlessException
	 */
	public static void initActionProperties(Action action, String id,
			AppDefaults defaults) throws NumberFormatException,
			HeadlessException {
		// Loop the possible properties of an action...
		// The loop is used, because most properties are processed in the same
		// way
		// (see last "else")
		for (String propertyName : BundledActionFiller.actionKeys) {
			// The key in the property file for this particular action for this
			// particular entry.
			String actionPropertyKey = id + "." + propertyName;
			if (propertyName == Action.MNEMONIC_KEY) {
				action.putValue(propertyName,
						defaults.getKeyCode(actionPropertyKey));
			} else if (propertyName == Action.ACCELERATOR_KEY) {
				// Now, we want to deal with accelerator keys
				// differently on macs and on other system.
				// for this we introduce the "shortcut"
				String keyStroke = defaults.getString(actionPropertyKey);
				if (keyStroke != null && keyStroke.startsWith("shortcut")) {
					int shortCutMask = java.awt.Toolkit.getDefaultToolkit()
							.getMenuShortcutKeyMask();
					String replaceString = "control";
					switch (shortCutMask) {
					case KeyEvent.META_MASK:
						replaceString = "meta";
						break;
					case KeyEvent.ALT_MASK:
						replaceString = "alt"; // Not likely at all...
						break;
					}
					String actualShortcut = keyStroke.replaceFirst("shortcut",
							replaceString);
					action.putValue(propertyName,
							KeyStroke.getKeyStroke(actualShortcut));
				} else {
					action.putValue(propertyName,
							defaults.getKeyStroke(actionPropertyKey));
				}
			} else if (propertyName == Action.SMALL_ICON) {
				// If we don't get an icon straight from the UIDefault..
				if (defaults.getIcon(actionPropertyKey) == null) {
					// we consider the data as a string...
					if (defaults.get(actionPropertyKey) instanceof String) {
						String iconPath = (String) defaults
								.get(actionPropertyKey);
						if (iconPath != null) {
							// Normally always true
							Icon icon = new ImageIcon(action.getClass()
									.getResource(iconPath));
							action.putValue(propertyName, icon);
						}
					}
				} else {
					action.putValue(propertyName,
							defaults.getIcon(actionPropertyKey));
				}
			} else if (propertyName == PRECONDITIONS) {
				if (defaults.getString(actionPropertyKey) != null) {
					// if precondition string is empty, remove existing ones.
					if ("".equals(defaults.getString(actionPropertyKey))) {
						action.putValue(propertyName, null);
					} else {
						String[] s = defaults.getString(actionPropertyKey)
								.split(", *");
						action.putValue(propertyName, s);
					}
				}
			} else if (propertyName == NUMBER_OF_COLUMNS) {
				if (defaults.getString(actionPropertyKey) != null) {
					int ncols = Integer.parseInt(defaults
							.getString(actionPropertyKey));
					action.putValue(NUMBER_OF_COLUMNS, new Integer(ncols));
				}
			} else {
				// GENERIC CASE (and the reason why we loop) :
				action.putValue(propertyName, defaults.get(actionPropertyKey));
			}
		}
	}
}
