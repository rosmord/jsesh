package org.qenherkhopeshef.observable;

/**
 * Typically, an Observable in Observer/Observable pattern.
 * 
 * <p> Usually, implemented using {@link ObservableEventSupport}, which will be a private instance variable of the observable object, which will delegate to it the management of events.
 * 
 * <p>Note that some event emitters can work <b>outside</b> of the Swing Event Dispatch thread.
 * For example, if we listen for folder content changes, the events will not occur in the EDT.
 * <p>In that case, the listener should take care of calling SwingUtilities.invokeLater() 
 * if it needs to update Swing components. In all cases, for Swing components, this kind of
 * precautions are relatively safe.

 */
public interface ObservableEventPublisher<EVENT> {
    public void addListener(ObservableEventListener<EVENT> listener);
    public void removeListener(ObservableEventListener<EVENT> listener);    
}
