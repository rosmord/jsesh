package org.qenherkhopeshef.properties.base.events;

import org.qenherkhopeshef.properties.base.ObjectProperty;

public interface PropertyChangeEvent<M,V> extends ModelEvent<M>{
		ObjectProperty<V> getProperty();
		
		default V getValue() {
			return getProperty().getValue();
		}
		
		V getOldValue();
}
