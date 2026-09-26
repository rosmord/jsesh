package org.qenherkhopeshef.guiFramework;

import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.qenherkhopeshef.utils.PlatformDetection;

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
	private static final String[] ACTION_KEYS = { Action.NAME,
			Action.SHORT_DESCRIPTION, Action.LONG_DESCRIPTION,
			Action.SMALL_ICON, Action.ACTION_COMMAND_KEY,
			Action.ACCELERATOR_KEY, Action.MNEMONIC_KEY,
			BundledAction.TEAR_OFF, PRECONDITIONS,
			BundledAction.GROUP_PROPERTY, BundledAction.BOOLEAN_PROPERTY,
			BundledAction.METHOD_ARGUMENT, NUMBER_OF_COLUMNS,
			BundledAction.IS_LABELLED, BundledAction.PROXY_METHOD };

	/**
	 * Maps each legacy, PascalCase property suffix to the lowerCamelCase
	 * suffix now preferred for new or edited entries. The legacy suffixes
	 * keep working indefinitely: {@link #initActionProperties} prefers the
	 * new suffix when it is defined, and falls back to the legacy one
	 * otherwise.
	 * <p>
	 * Five of these (NAME, SHORT_DESCRIPTION, ACCELERATOR_KEY, MNEMONIC_KEY,
	 * SMALL_ICON) are shared with {@code javax.swing.Action}, and their new
	 * spelling is chosen to match {@code org.jhotdraw_7_6.util.ResourceBundleUtil}'s
	 * own vocabulary, so both of JSesh's property-loading mechanisms converge
	 * on one naming scheme. The rest (Preconditions, ProxyMethod, ...) are
	 * {@code guiFramework}-only extensions with no JHotDraw equivalent; their
	 * new spelling is simply the legacy PascalCase name decapitalized, to
	 * bring them under the same casing rule without inventing new vocabulary.
	 * `.MenuPlatform` is deliberately absent from this map: it has no
	 * backing constant and is never read by {@link #initActionProperties} (see
	 * {@code quitApplication}'s hardcoded platform check in {@code MenuFactory}),
	 * so there's nothing here to alias yet.
	 */
	private static final Map<String, String> NEW_SUFFIX_FOR_LEGACY = Map.ofEntries(
			Map.entry(Action.NAME,                    "text"),
			Map.entry(Action.SHORT_DESCRIPTION,       "toolTipText"),
			Map.entry(Action.ACCELERATOR_KEY,         "accelerator"),
			Map.entry(Action.MNEMONIC_KEY,            "mnemonic"),
			Map.entry(Action.SMALL_ICON,              "icon"),
			Map.entry(PRECONDITIONS,                  "preconditions"),
			Map.entry(NUMBER_OF_COLUMNS,               "numberOfColumns"),
			Map.entry(BundledAction.TEAR_OFF,          "tearOff"),
			Map.entry(BundledAction.GROUP_PROPERTY,    "groupProperty"),
			Map.entry(BundledAction.BOOLEAN_PROPERTY,  "booleanProperty"),
			Map.entry(BundledAction.METHOD_ARGUMENT,   "argument"),
			Map.entry(BundledAction.IS_LABELLED,       "isLabelled"),
			Map.entry(BundledAction.PROXY_METHOD,      "proxyMethod"));

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
		} catch (NoSuchFieldException|IllegalAccessException e) {
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
		// way (see last "else")
		// An hashmap-oriented system would be better.
		
		
		for (String propertyName : BundledActionFiller.ACTION_KEYS) {
			// The key in the property file for this particular action for this
			// particular entry. Prefer the new lowerCamelCase suffix when it's
			// defined, falling back to the legacy PascalCase one otherwise.
			String legacyKey = id + "." + propertyName;
			String newSuffix = NEW_SUFFIX_FOR_LEGACY.get(propertyName);
			String actionPropertyKey = (newSuffix == null)
					? legacyKey
					: defaults.firstDefinedKey(id + "." + newSuffix, legacyKey);
			if (propertyName.equals(Action.MNEMONIC_KEY)) {
				action.putValue(propertyName,
						defaults.getKeyCode(actionPropertyKey));
			} else if (propertyName.equals(Action.ACCELERATOR_KEY) || propertyName.equals(Action.ACCELERATOR_KEY + "[mac]")) {
				// Now, we want to deal with accelerator keys
				// differently on macs and on other system.
				// for this we introduce the "shortcut"
				String keyStroke = null;
				// On the mac, try to see if a [mac] version of the property is defined.
				if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
					keyStroke= defaults.getString(actionPropertyKey + "[mac]");
				}
				// If not found, or not on the mac, try the plain "AcceleratorKey" keyword.
				if (keyStroke == null)
					keyStroke= defaults.getString(actionPropertyKey);
				// Now, process the result (if any).				
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
				} else if (keyStroke != null) {
					// (use keyStroke, which may come from the [mac] variant)
					action.putValue(propertyName,
							KeyStroke.getKeyStroke(keyStroke));
				}
			} else if (propertyName.equals(Action.SMALL_ICON)) {
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
			} else if (propertyName.equals(PRECONDITIONS)) {
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
			} else if (propertyName.equals(NUMBER_OF_COLUMNS)) {
				if (defaults.getString(actionPropertyKey) != null) {
					int ncols = Integer.parseInt(defaults
							.getString(actionPropertyKey));
					action.putValue(NUMBER_OF_COLUMNS, ncols);
				}
			} else {
				// GENERIC CASE (and the reason why we loop) :
				action.putValue(propertyName, defaults.get(actionPropertyKey));
			}
		}
	}
}
