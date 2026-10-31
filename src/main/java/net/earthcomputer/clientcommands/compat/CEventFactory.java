package net.earthcomputer.clientcommands.compat;

import java.util.function.Function;

public final class CEventFactory {
    private CEventFactory() {
    }

    public static <T> CEvent<T> createArrayBacked(Class<T> type, Function<T[], T> invokerFactory) {
        return new CEvent<>(type, invokerFactory);
    }
}
