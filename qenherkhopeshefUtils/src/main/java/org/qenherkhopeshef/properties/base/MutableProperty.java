package org.qenherkhopeshef.properties.base;

public interface MutableProperty<T> extends ObjectProperty<T>{
	void setValue(T value);
}
