package org.qenherkhopeshef.guiFramework;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;

import javax.swing.JToggleButton.ToggleButtonModel;



/**
 * A model linking a button to an object's property.
 * 
 * The button will be linked to a specific value of an object's property.
 * 
 * <p>Makes heavy use of reflection.
 * @author rosmord
 * 
 */

public class PropertyButtonModel extends ToggleButtonModel {

	private PropertyHolder target;

	private String propertyName;

	private String propertyNameCapitalized;
	
	private Object propertyValue;

	private boolean isBoolean = false;

	private MyPropertyChangeListener myPropertyChangeListener;
	/**
	 * 
	 */
	private static final long serialVersionUID = -8230841055188603227L;

	/**
	 * Create a model which will be linked to a specific value of an object's property.
	 * <p>The button will be selected when the property has the said value,
	 * and selecting the button will indeed set the property to this value.
	 * @param target the object whose value will control the model
	 * @param propertyName the object's property.
	 * @param propertyValue the value of the property which will sets this model to "selected". Must not be null.
	 */
	
	public PropertyButtonModel(PropertyHolder target, String propertyName, Object propertyValue) {
		super();
		this.target = target;
		this.propertyName = propertyName;
		this.propertyNameCapitalized= capitalizePropertyName();
		this.propertyValue = propertyValue;
		this.isBoolean = propertyValue instanceof Boolean;
		this.myPropertyChangeListener = new MyPropertyChangeListener();
		// Let's register as property change listeners.
		this.target.getPropertyChangeSupport().addPropertyChangeListener(propertyName, myPropertyChangeListener);
	}

	private String capitalizePropertyName() {
		return Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1); 
	}
	
	public boolean isSelected() {
		boolean r= false;
		Object result = getPropertyValue();
		r= propertyValue.equals(result);
		return r;
	}

	private Object getPropertyValue() {
		try {
			String prefix = "get";
			if (isBoolean)
				prefix = "is";
			Method method = target.getClass().getMethod(prefix + propertyNameCapitalized,
					(Class[]) new Class[0]);
			Object result = method.invoke(target, new Object[0]);
			return result;
		} catch (Exception e) {
			// Rethrows
			throw new RuntimeException(e);
		}
	}

	public void setSelected(boolean b) {
		super.setSelected(b);
		try {
			Class argClass = propertyValue.getClass();
			if (isBoolean)
				argClass = Boolean.TYPE;
			Method setMethod = target.getClass().getMethod("set" + propertyNameCapitalized,
					new Class[] { argClass });
			if (isSelected()) {					
				if (!b) {
					Object unSetValue= null;
					if (isBoolean)
						unSetValue= Boolean.FALSE;
					setMethod.invoke(target, new Object [] {unSetValue});
					fireStateChanged();
				}
			} else {
				if (b) {
					setMethod.invoke(target, new Object [] {propertyValue});
					fireStateChanged();
				} // else : NO OP.
			}
		} catch (Exception e) {
			// Rethrows
			throw new RuntimeException(e);
		}
	}
	
	private class MyPropertyChangeListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			if (propertyName.equals(evt.getPropertyName())) {
				fireStateChanged();
			}
		}
		
	}
}
