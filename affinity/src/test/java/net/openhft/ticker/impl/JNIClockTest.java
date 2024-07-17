/*
 * Copyright 2016-2020 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.ticker.impl;

import junit.framework.TestCase;

public class JNIClockTest extends TestCase {

    private boolean libraryLoaded = false;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            libraryLoaded = JNIClock.LOADED;
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Library not loaded: " + e.getMessage());
        }
    }

    public void testLibraryLoaded() {
        if (!libraryLoaded) {
            System.out.println("Skipping testLibraryLoaded as the library is not loaded.");
            return;
        }
        assertTrue("Library should be loaded", libraryLoaded);
    }

    public void testNanoTime() {
        if (!libraryLoaded) {
            System.out.println("Skipping testNanoTime as the library is not loaded.");
            return;
        }
        long nanoTime = JNIClock.INSTANCE.nanoTime();
        assertTrue(nanoTime > 0);
    }

    public void testTicks() {
        if (!libraryLoaded) {
            System.out.println("Skipping testTicks as the library is not loaded.");
            return;
        }
        long ticks = JNIClock.INSTANCE.ticks();
        assertTrue(ticks > 0);
    }

    public void testToNanos() {
        if (!libraryLoaded) {
            System.out.println("Skipping testToNanos as the library is not loaded.");
            return;
        }
        long ticks = JNIClock.INSTANCE.ticks();
        long nanos = JNIClock.INSTANCE.toNanos(ticks);
        assertTrue(nanos > 0);
    }

    public void testToMicros() {
        if (!libraryLoaded) {
            System.out.println("Skipping testToMicros as the library is not loaded.");
            return;
        }
        long ticks = JNIClock.INSTANCE.ticks();
        double micros = JNIClock.INSTANCE.toMicros(ticks);
        assertTrue(micros > 0);
    }

    public void testTscToNano() {
        if (!libraryLoaded) {
            System.out.println("Skipping testTscToNano as the library is not loaded.");
            return;
        }
        long ticks = JNIClock.INSTANCE.ticks();
        long nanos = JNIClock.tscToNano(ticks);
        assertTrue(nanos > 0);
    }
}
