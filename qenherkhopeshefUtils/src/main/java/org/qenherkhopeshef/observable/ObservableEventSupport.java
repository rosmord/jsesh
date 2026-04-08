package org.qenherkhopeshef.observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Actual implementation to use for {@link ObservableEventPublisher}. 
 * 
 * Typically, this will be a private instance variable of the observable object, which will delegate to it the management of events.
 */
public class ObservableEventSupport<EVENT> {
    private final List<ObservableEventListener<? super EVENT>> listeners = new ArrayList<>();

    /**
     * Add a listener for this event.
     * In theory, listeners might be able to handle other events too.
     * @param listener
     */
    public void addListener(ObservableEventListener<? super EVENT> listener) {
        listeners.add(listener);
    }

    public void removeListener(ObservableEventListener<? super EVENT> listener) {
        listeners.remove(listener);
    }

    public void fireEvent(EVENT event) {
        for (ObservableEventListener<? super EVENT> listener : listeners) {
            listener.eventOccurred(event);
        }
    }
}
