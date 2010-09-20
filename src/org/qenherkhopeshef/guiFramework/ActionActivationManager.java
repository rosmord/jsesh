package org.qenherkhopeshef.guiFramework;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

/**
 * Manage the enabled status of Actions.
 * 
 * @author rosmord
 */

public class ActionActivationManager implements PropertyChangeListener {

	/**
	 * We start with a simple system.
	 */
	private List<ActionActivator> actionActivatorList;

	/**
	 * Inner object responsible for maintaining an action.
	 * 
	 * @author rosmord
	 * 
	 */
	private static class ActionActivator {
		Action action;
		String[] preconditions;
		Object actionTarget;

		public ActionActivator(Action action, String[] properties,
				Object actionTarget) {
			this.action = action;
			this.preconditions = properties;
			this.actionTarget = actionTarget;
		}

		/**
		 * Activate or inactivate an action depending on the needs.
		 */
		public void testActivation() {
			boolean enabled = true;
			for (int i = 0; enabled && i < preconditions.length; i++) {
				Boolean available = Boolean.FALSE;
				try {
					Method method = actionTarget.getClass().getMethod(
							"is" + preconditions[i], (Class[]) new Class[0]);
					available = (Boolean) method.invoke(actionTarget,
							new Object[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				enabled = enabled && available.booleanValue();
			}
			action.setEnabled(enabled);
		}
	}

	public ActionActivationManager() {
		actionActivatorList = new ArrayList<ActionActivator>();
	}
	

	/**
	 * Adds an action which will be enabled or disabled when the application's
	 * preconditions change.
	 * 
	 * @param action
	 *            the action to register
	 * @param propertyHolder
	 *            the objet whose properties action activation depends on.
	 * @param properties
	 *            the list of properties which should be enabled for this action
	 *            to be activated.
	 */
	public void registerAction(Action action, PropertyHolder propertyHolder,
			String[] properties) {

		ActionActivator activator = new ActionActivator(action, properties,
				propertyHolder);
		actionActivatorList.add(activator);
		PropertyChangeSupport pcs = propertyHolder.getPropertyChangeSupport();
		if (pcs != null)
			pcs.addPropertyChangeListener(this);
		else
			throw new RuntimeException(
					"Bug: the target object should provide a property change support.");
		// Should the action be initially activated ?
		activator.testActivation();
	}

	/**
	 * Adds an action which will be enabled or disabled when the application's
	 * preconditions change.
	 * 
	 * @param action
	 */
	public void registerAction(BundledAction action) {
		ActionActivator activator = new ActionActivator(action, action.getPreconditions(),
				action.getTarget());
		actionActivatorList.add(activator);
		PropertyChangeSupport pcs = action.getTarget()
				.getPropertyChangeSupport();
		if (pcs != null)
			pcs.addPropertyChangeListener(this);
		else
			throw new RuntimeException(
					"Bug: the target object should provide a property change support.");
		activator.testActivation();
	}

	/**
	 * Currently : update everybody. We might decide to update actions depending
	 * on the event property...
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		for (ActionActivator activator : actionActivatorList) {
			activator.testActivation();
		}
	}
}
