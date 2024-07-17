package net.openhft.ticker;

import junit.framework.TestCase;
import net.openhft.ticker.impl.JNIClock;
import net.openhft.ticker.impl.SystemClock;

public class TickerTest extends TestCase {

    public void testSingletonInstance() {
        ITicker ticker = Ticker.INSTANCE;
        if (JNIClock.LOADED) {
            assertSame(JNIClock.INSTANCE, ticker);
        } else {
            assertSame(SystemClock.INSTANCE, ticker);
        }
    }

    public void testTicks() {
        long ticks = Ticker.ticks();
        assertTrue(ticks > 0);
    }

    public void testNanoTime() {
        long nanoTime = Ticker.nanoTime();
        assertTrue(nanoTime > 0);
    }

    public void testToNanos() {
        long ticks = Ticker.ticks();
        long nanos = Ticker.toNanos(ticks);
        assertTrue(nanos > 0);
    }

    public void testToMicros() {
        long ticks = Ticker.ticks();
        double micros = Ticker.toMicros(ticks);
        assertTrue(micros > 0);
    }
}
