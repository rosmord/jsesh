package org.qenherkhopeshef.observable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ObservableEventSupportTest {

    @Test
    public void listenerReceivesEvent() {
        ObservableEventSupport<String> support = new ObservableEventSupport<>();
        List<String> received = new ArrayList<>();
        support.addListener(received::add);
        support.fireEvent("hello");
        assertEquals(List.of("hello"), received);
    }

    @Test
    public void removedListenerDoesNotReceiveEvent() {
        ObservableEventSupport<String> support = new ObservableEventSupport<>();
        List<String> received = new ArrayList<>();
        ObservableEventListener<String> listener = received::add;
        support.addListener(listener);
        support.removeListener(listener);
        support.fireEvent("hello");
        assertTrue(received.isEmpty());
    }

    @Test
    public void multipleListenersAllReceiveEvent() {
        ObservableEventSupport<String> support = new ObservableEventSupport<>();
        List<String> received1 = new ArrayList<>();
        List<String> received2 = new ArrayList<>();
        support.addListener(received1::add);
        support.addListener(received2::add);
        support.fireEvent("event");
        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
    }

    @Test
    public void duplicateListenerAddedOnce() {
        ObservableEventSupport<String> support = new ObservableEventSupport<>();
        List<String> received = new ArrayList<>();
        ObservableEventListener<String> listener = received::add;
        support.addListener(listener);
        support.addListener(listener);
        support.fireEvent("event");
        assertEquals(1, received.size());
    }

    @Test
    public void noListenersDoesNotThrow() {
        ObservableEventSupport<String> support = new ObservableEventSupport<>();
        assertDoesNotThrow(() -> support.fireEvent("event"));
    }

    @Test
    public void eventValueIsCorrect() {
        ObservableEventSupport<Integer> support = new ObservableEventSupport<>();
        List<Integer> received = new ArrayList<>();
        support.addListener(received::add);
        support.fireEvent(42);
        assertEquals(42, received.get(0));
    }

    @Test
    public void multipleEventsDeliveredInOrder() {
        ObservableEventSupport<Integer> support = new ObservableEventSupport<>();
        List<Integer> received = new ArrayList<>();
        support.addListener(received::add);
        support.fireEvent(1);
        support.fireEvent(2);
        support.fireEvent(3);
        assertEquals(3, received.size());
        assertTrue(received.containsAll(List.of(1, 2, 3)));
    }
}
