package org.qenherkhopeshef.properties.base;

public interface ObjectProperty<T> {
	T getValue();
	void addPropertyObserver();
	void removePropertyObserver();

}
