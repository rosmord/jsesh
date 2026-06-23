package org.qenherkhopeshef.observable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Actual implementation to use for {@link ObservableEventPublisher}. 
 * 
 * Typically, this will be a private instance variable of the observable object, which will delegate to it the management of events.
 * 
 */
public class ObservableEventSupport<EVENT> {

    /**
     * A set of listeners, using the identity of the listeners 
     * to avoid duplicates, and possible, if unlikely problems with equals().
     */
    private final Set<IdHolder<ObservableEventListener<? super EVENT>>> listeners =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Add a listener for this event.
     * In theory, listeners might be able to handle other events too.
     * @param listener
     */
    public void addListener(ObservableEventListener<? super EVENT> listener) {
        listeners.add(new IdHolder<>(listener));
    }

    public void removeListener(ObservableEventListener<? super EVENT> listener) {
        listeners.remove(new IdHolder<>(listener));
    }

    public void fireEvent(EVENT event) {
        for (IdHolder<ObservableEventListener<? super EVENT>> listener : listeners) {
            listener.getObject().eventOccurred(event);            
        }
    }

    private static class IdHolder<T> {
        private final T object;
        
        public IdHolder(T object) {
            this.object = object;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof IdHolder) {
                return object == ((IdHolder<?>) other).object;
            } else {
                return false;
            }
        }

        /**
         * @return the object
         */
        public T getObject() {
            return object;
        }

        public int hashCode() {
            return System.identityHashCode(object);
        }
    }
}
