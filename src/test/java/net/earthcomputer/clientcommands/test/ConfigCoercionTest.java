package net.earthcomputer.clientcommands.test;

import net.earthcomputer.clientcommands.Configs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ConfigCoercionTest {
    @Test
    public void testDoubleToFloat() {
        Object value = Configs.coerce(float.class, Double.valueOf(1.5));
        assertEquals(Float.valueOf(1.5f), value);
    }

    @Test
    public void testDoubleToInt() {
        assertEquals(Integer.valueOf(42), Configs.coerce(int.class, Double.valueOf(42.0)));
    }

    @Test
    public void testPassthrough() {
        assertEquals(Boolean.TRUE, Configs.coerce(boolean.class, Boolean.TRUE));
        assertEquals("x", Configs.coerce(String.class, "x"));
    }
}
