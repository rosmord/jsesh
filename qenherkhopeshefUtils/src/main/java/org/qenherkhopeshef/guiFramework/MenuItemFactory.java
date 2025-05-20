package org.qenherkhopeshef.guiFramework;

import java.util.HashMap;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;



/**
 * Factory for building menu items from actions.
 * Depending on action preconditions, the correct menu items will be created.
 * @author rosmord
 *
 */
public class MenuItemFactory {
	
	/**
	 * Map from group names to groups.
	 * <String,ButtonGroup>
	 */
	private HashMap<String,ButtonGroup> groupMap= new HashMap<>();
	
	/**
	 * Create a menu item for a given action.
	 * The exact kind of menu item will depend on the action preconditions.
	 * @param action
	 * @return a menu item.
	 */
	public JMenuItem buildItem(Action action) {
		if (action.getValue(BundledAction.GROUP_PROPERTY) != null && action instanceof BundledAction) {
			return buildRadioButtonMenuItem((BundledAction) action);
		} else if (action.getValue(BundledAction.BOOLEAN_PROPERTY) != null && action instanceof BundledAction) {
			return buildCheckBoxMenuItem((BundledAction) action);
		}
		return new JMenuItem(action);
	}

	/**
	 * Called when the menu item should be a check box.
	 * @param action
	 * @return
	 */
	protected JMenuItem buildCheckBoxMenuItem(BundledAction action) {
		JCheckBoxMenuItem result= new JCheckBoxMenuItem(action);
		PropertyButtonModel model= new PropertyButtonModel(action.getTarget(), (String)action.getValue(BundledAction.PROPERTY_NAME), Boolean.TRUE);
		result.setModel(model);
		return result;
	}

	/**
	 * Called when the menu item should be a radio button.
	 * @param action
	 * @return
	 */
	
	protected JMenuItem buildRadioButtonMenuItem(BundledAction action) {
		// Create menu item.
		JRadioButtonMenuItem result= new JRadioButtonMenuItem(action);
		PropertyButtonModel model= new PropertyButtonModel(action.getTarget(), (String)action.getValue(BundledAction.PROPERTY_NAME), (String)action.getValue(BundledAction.PROPERTY_VALUE));
		result.setModel(model);
		// Add it to group
		String groupName= (String)action.getValue(BundledAction.GROUP_PROPERTY);
		
		if (! groupMap.containsKey(groupName))
			groupMap.put(groupName, new ButtonGroup());
		ButtonGroup group= groupMap.get(groupName);
		group.add(result);
		return result;
	}
	
}
