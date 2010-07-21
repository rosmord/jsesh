package org.qenherkhopeshef.guiFramework;

import java.beans.PropertyChangeSupport;

/**
 * Any object which holds properties and act as an Observable for them.
 * (well, a bean).
 * @author rosmord
 */
public interface PropertyHolder {
	PropertyChangeSupport getPropertyChangeSupport();
}
