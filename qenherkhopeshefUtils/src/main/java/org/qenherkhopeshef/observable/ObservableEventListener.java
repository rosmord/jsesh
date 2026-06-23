package org.qenherkhopeshef.observable;

/**
 * A listener for events.
 * 
 * <p>Note that some event emitters can work <b>outside</b> of the Swing Event Dispatch thread.
 * For example, if we listen for folder content changes, the events will not occur in the EDT.
 * <p>In that case, the listener should take care of calling SwingUtilities.invokeLater() 
 * if it needs to update Swing components. In all cases, for Swing components, this kind of
 * precautions are relatively safe.
 * @FunctionalInterface
 */
@FunctionalInterface
public interface ObservableEventListener<T> {
    public void eventOccurred(T event);

}
