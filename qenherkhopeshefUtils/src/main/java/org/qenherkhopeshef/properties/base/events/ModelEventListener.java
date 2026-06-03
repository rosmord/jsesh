package org.qenherkhopeshef.properties.base.events;

public interface ModelEventListener<E extends ModelEvent<?>> {
	void ModelChanged(E event);
}
