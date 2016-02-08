package org.qenherkhopeshef.jhotdrawChanges;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for selecting only the parts of Quaqua we want.
 * <p> Alas, this doesn't work anymore with java 8 and quaqua.
 * <p> As a user, I want to have a number of keyboard-oriented features 
 * which are removed by Quaqua...
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class QuaquaSelectiveSetupHelper {

	/**
	 * Build the list of UI delegates to use in Quaqua. See my problems with
	 * comboboxes. (ok, solved  by now with 			System.setProperty("Quaqua.requestFocusEnabled", "true");
	 * 
	 * There are however problems with dialogs, plus no automatic key binding for popup menus.
	 * 
	 * @return
	 */
	private static Set<String> buildUIDelegateList() {
		Set<String> includes = new HashSet<String>();

		includes.add("Browser");
		//includes.add("Button"); // if we don't have comboboxes and we have buttons, the cb buttons don't look good.
		includes.add("CheckBox");
		includes.add("ColorChooser");
		includes.add("ColorSlider");
		//includes.add("ComboBox");
		includes.add("DesktopPane");
		includes.add("EditorPane");
		includes.add("FileChooser");
		includes.add("FormattedTextField");
		includes.add("Label");
		includes.add("List");
		includes.add("MenuBar");
		includes.add("MenuItem");
		includes.add("Menu");
		includes.add("NavigatableTabbedPane");
		includes.add("OptionPane");
		includes.add("Panel");
		includes.add("PasswordField");
		// includes.add("PopupMenu");
		includes.add("RadioButton");
		includes.add("RootPane");
		includes.add("ScrollBar");
		includes.add("ScrollPane");
		includes.add("ScrollTabbedPane");
		includes.add("Separator");
		includes.add("Slider");
		includes.add("Spinner");
		includes.add("SplitPane");
		// includes.add("TabbedPane");
		includes.add("TableHeader");
		includes.add("Table");
		includes.add("TextArea");
		includes.add("TextField");
		includes.add("TextPane");
		includes.add("ToggleButton");
		includes.add("ToolBarSeparator");
		includes.add("ToolBar");
		includes.add("Tree");
		includes.add("Viewport");
		includes.add("BasicBrowser");
		includes.add("Component");
		return includes;
	}

	public static void selectJSeshSpecificUI() {
		System.setProperty("Quaqua.requestFocusEnabled", "true");
		
		//Set<String> includes = buildUIDelegateList();
		//QuaquaManager.setIncludedUIs(includes);
		// Exclude doesn't work...
		//HashSet<String> set= new HashSet<String>();
		//set.add("PopupMenu");
		//QuaquaManager.setExcludedUIs(set);
	}

}
