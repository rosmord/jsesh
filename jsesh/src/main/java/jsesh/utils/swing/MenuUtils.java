/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.utils.swing;

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
