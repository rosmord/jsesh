package org.qenherkhopeshef.observable;

@FunctionalInterface
public interface ObservableEventListener<T> {
    public void eventOccurred(T event);

}
