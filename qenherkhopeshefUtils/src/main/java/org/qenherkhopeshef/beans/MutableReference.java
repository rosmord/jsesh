package org.qenherkhopeshef.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A mutable reference for an object.
 *  <p> Ideally, the object itself would be immutable.
 *   
 * @author rosmord
 *
 * @param <T> the type of the referenced value.
 */
public class MutableReference<T> {
	private T value;
	
	private PropertyChangeSupport changeSupport ;

	public MutableReference(T reference) {
		this.value = reference;
		this.changeSupport = new PropertyChangeSupport(this);
	}

	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}


	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}


	public boolean hasListeners(String propertyName) {
		return changeSupport.hasListeners(propertyName);
	}


	public T getValue() {
		return value;
	}
	
	public void setValue(T newValue) {
		if (! this.value.equals(newValue)) {
			T oldValue = this.value;
			this.value = newValue;
			this.changeSupport.firePropertyChange("value", oldValue, newValue);
		}
	}
		
}
