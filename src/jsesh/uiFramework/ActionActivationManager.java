package jsesh.uiFramework;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Manage the enabled status of BundleActions.
 * @author rosmord
 *
 */

public class ActionActivationManager implements PropertyChangeListener {
	
	/**
	 * We start with a simple system.
	 */
	private List managedActions;
	
	public ActionActivationManager() {
		managedActions= new ArrayList();
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
		action.getTarget().getPropertyChangeSupport().addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		Iterator it= managedActions.iterator();
		
		while (it.hasNext()) {
			BundledAction action= (BundledAction) it.next();
			updateAction(action);
		}
	}
}
