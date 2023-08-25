package org.qenherkhopeshef.properties.base.events;

/**
 * Base specification for events.
 * @param <M> the modified model.
 */
public interface ModelEvent<M> {
	M getSource();
}
