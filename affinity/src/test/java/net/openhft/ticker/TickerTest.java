package net.openhft.ticker;

import net.openhft.ticker.impl.JNIClock;
import net.openhft.ticker.impl.SystemClock;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertSame;

public class TickerTest {
    @Test
    public void whenJniClockUnavailableTickerUsesSystemClock() throws Exception {
        // Force JNIClock.LOADED to false
        Field loadedField = JNIClock.class.getDeclaredField("LOADED");
        loadedField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(loadedField, loadedField.getModifiers() & ~Modifier.FINAL);
        loadedField.setBoolean(null, false);

        // Load Ticker after modifying JNIClock.LOADED
        Class<?> tickerClass = Class.forName("net.openhft.ticker.Ticker");
        Field instanceField = tickerClass.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        Object instance = instanceField.get(null);
        assertSame(SystemClock.INSTANCE, instance);
    }
}
