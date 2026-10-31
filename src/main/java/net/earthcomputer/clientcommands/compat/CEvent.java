package net.earthcomputer.clientcommands.compat;

import java.lang.reflect.Array;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Minimal replacement for Fabric's {@code Event}, keeping the same array-backed
 * invocation semantics so that upstream code can be reused unchanged.
 */
public final class CEvent<T> {
    private final Class<T> type;
    private final List<T> listeners = new CopyOnWriteArrayList<>();
    private final Function<T[], T> invokerFactory;
    private volatile T invoker;

    CEvent(Class<T> type, Function<T[], T> invokerFactory) {
        this.type = type;
        this.invokerFactory = invokerFactory;
    }

    public void register(T listener) {
        this.listeners.add(listener);
        this.invoker = null;
    }

    @SuppressWarnings("unchecked")
    public T invoker() {
        T result = this.invoker;
        if (result == null) {
            List<T> current = List.copyOf(this.listeners);
            T[] array = (T[]) Array.newInstance(this.type, current.size());
            result = this.invokerFactory.apply(current.toArray(array));
            this.invoker = result;
        }
        return result;
    }
}
