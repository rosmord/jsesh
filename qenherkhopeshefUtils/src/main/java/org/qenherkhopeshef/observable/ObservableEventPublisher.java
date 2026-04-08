package org.qenherkhopeshef.observable;

/**
 * Typically, an Observable in Observer/Observable pattern.
 * 
 * <p> Usually, implemented using {@link ObservableEventSupport}, which will be a private instance variable of the observable object, which will delegate to it the management of events.
 */
public interface ObservableEventPublisher<EVENT> {
    public void addListener(ObservableEventListener<EVENT> listener);
    public void removeListener(ObservableEventListener<EVENT> listener);    
}
