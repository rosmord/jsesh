package jsesh.swingUtils;

import java.awt.Toolkit;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MenuUtils {

	/**
	 * Build a menu.
	 * 
	 * @param menu
	 * @param action
	 * @param vk
	 * @param mask
	 *            additional mask
	 */
	public static void addWithShortCut(JMenu menu, Action action, int vk, int mask) {
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(KeyUtils.buildCommandShortCut(vk, mask));
		menu.add(item);
	
	}

	public static void addWithShortCut(JMenu menu, Action action, int vk) {
		JMenuItem item = new JMenuItem(action);
		
		item.setAccelerator(KeyUtils.buildCommandShortCut(vk));
		menu.add(item);
	
	}

	/**
	 * Add a menu item with a specific mnemonic. On the mac, use a shortcut
	 * instead.
	 * 
	 * @param menu
	 * @param action
	 * @param keyCode
	 */
	public static void addWithMnemonics(JMenu menu, Action action, Integer keyCode) {
		action.putValue(Action.MNEMONIC_KEY, keyCode);
		menu.add(action);
	}

}
