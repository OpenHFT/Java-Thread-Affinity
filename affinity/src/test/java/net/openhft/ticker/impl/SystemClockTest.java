package net.openhft.ticker.impl;

import junit.framework.TestCase;

public class SystemClockTest extends TestCase {

    public void testNanoTime() {
        long nanoTime = SystemClock.INSTANCE.nanoTime();
        assertTrue(nanoTime > 0);
    }

    public void testTicks() {
        long ticks = SystemClock.INSTANCE.ticks();
        assertTrue(ticks > 0);
    }

    public void testToNanos() {
        long ticks = SystemClock.INSTANCE.ticks();
        long nanos = SystemClock.INSTANCE.toNanos(ticks);
        assertEquals(ticks, nanos);
    }

    public void testToMicros() {
        long ticks = SystemClock.INSTANCE.ticks();
        double micros = SystemClock.INSTANCE.toMicros(ticks);
        assertEquals(ticks / 1e3, micros);
    }
}
