package org.qenherkhopeshef.guiFramework;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;



/**
 * Manage the enabled status of BundleActions.
 * @author rosmord
 *
 */

public class ActionActivationManager implements PropertyChangeListener {
	
	/**
	 * We start with a simple system.
	 */
	private List<Action> managedActions;
	
	public ActionActivationManager() {
		managedActions= new ArrayList<Action>();
	}

	/**
	 * Test if actions should be enabled
	 * @param target
	 * @param action
	 */
	
	private void updateAction(BundledAction action) {
		String preconditions []= (String[]) action.getValue(BundledAction.PRECONDITIONS);
		boolean enabled= true;
		for (int i= 0; enabled && i < preconditions.length; i++) {
			Boolean available= Boolean.FALSE;
			try {
				Method method= action.getTarget().getClass().getMethod("is" + preconditions[i], (Class[]) new Class[0]);
				available= (Boolean) method.invoke(action.getTarget(), new Object[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			enabled= enabled && available.booleanValue();
		}
		action.setEnabled(enabled);
	}

	
	public void registerAction(BundledAction action) {
		managedActions.add(action);
		PropertyChangeSupport pcs = action.getTarget().getPropertyChangeSupport();
		if (pcs != null)
			pcs.addPropertyChangeListener(this);
		else
			throw new RuntimeException("Bug: the target object should provide a property change support.");
       updateAction(action);
    }

	public void propertyChange(PropertyChangeEvent evt) {
		Iterator<Action> it= managedActions.iterator();
		
		while (it.hasNext()) {
			BundledAction action= (BundledAction) it.next();
			updateAction(action);
		}
	}
}
