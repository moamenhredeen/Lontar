package app.orgx.desktop.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {
    record TestEvent(String message) {}
    record OtherEvent(int value) {}
    private EventBus bus;

    @BeforeEach
    void setUp() { bus = new EventBus(); }

    @Test
    void subscriberReceivesPublishedEvent() {
        var received = new ArrayList<TestEvent>();
        bus.subscribe(TestEvent.class, received::add);
        bus.publish(new TestEvent("hello"));
        assertEquals(1, received.size());
        assertEquals("hello", received.getFirst().message());
    }

    @Test
    void subscriberDoesNotReceiveUnrelatedEvents() {
        var received = new ArrayList<TestEvent>();
        bus.subscribe(TestEvent.class, received::add);
        bus.publish(new OtherEvent(42));
        assertTrue(received.isEmpty());
    }

    @Test
    void multipleSubscribersAllReceiveEvent() {
        var list1 = new ArrayList<TestEvent>();
        var list2 = new ArrayList<TestEvent>();
        bus.subscribe(TestEvent.class, list1::add);
        bus.subscribe(TestEvent.class, list2::add);
        bus.publish(new TestEvent("hi"));
        assertEquals(1, list1.size());
        assertEquals(1, list2.size());
    }

    @Test
    void unsubscribedHandlerDoesNotReceiveEvents() {
        var received = new ArrayList<TestEvent>();
        Consumer<TestEvent> handler = received::add;
        bus.subscribe(TestEvent.class, handler);
        bus.unsubscribe(TestEvent.class, handler);
        bus.publish(new TestEvent("ignored"));
        assertTrue(received.isEmpty());
    }
}
