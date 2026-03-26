package app.orgx.desktop.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    public <T> void subscribe(Class<T> type, Consumer<T> handler) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public <T> void unsubscribe(Class<T> type, Consumer<T> handler) {
        var handlers = subscribers.get(type);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        var handlers = subscribers.get(event.getClass());
        if (handlers != null) {
            for (var handler : handlers) {
                ((Consumer<T>) handler).accept(event);
            }
        }
    }
}
