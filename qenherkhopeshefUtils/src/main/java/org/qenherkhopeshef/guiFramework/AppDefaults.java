package org.qenherkhopeshef.guiFramework;

import javax.swing.KeyStroke;
import javax.swing.UIDefaults;

/**
 * Application defaults.
 * <p>
 * from <em>Asserting Control Over the GUI: Commands, Defaults, and Resource Bundles</em>
 * by Hans Muller
 * 
 */
public class AppDefaults extends UIDefaults {
	
	private static final long serialVersionUID = 7289206292792859095L;

	public KeyStroke getKeyStroke(String key) {
		return KeyStroke.getKeyStroke(getString(key));
	}

	public Integer getKeyCode(String key) {
		KeyStroke ks = getKeyStroke(key);
		return (ks != null) ? ks.getKeyCode() : null;
	}

	/**
	 * Returns the first of the given keys whose value in this table is
	 * non-null, in the given preference order, or the last key if none of
	 * them resolves to a value.
	 * <p>
	 * This only decides which key name to use; it performs no type
	 * coercion of its own — callers still do their own typed lookup
	 * ({@code getString}, {@code getIcon}, {@code getKeyStroke}, ...) on the
	 * returned key. Used to let a newer preferred key silently take over
	 * from an older one without changing any value-format semantics.
	 *
	 * @param keys candidate keys, most preferred first; must contain at least one key
	 * @return the first key with a non-null value, or the last key if none matched
	 */
	public String firstDefinedKey(String... keys) {
		for (int i = 0; i < keys.length - 1; i++) {
			if (get(keys[i]) != null) {
				return keys[i];
			}
		}
		return keys[keys.length - 1];
	}
}