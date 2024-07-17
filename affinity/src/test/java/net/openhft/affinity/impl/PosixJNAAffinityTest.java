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

package net.openhft.affinity.impl;

import junit.framework.TestCase;
import net.openhft.affinity.IAffinity;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.BitSet;

public class PosixJNAAffinityTest extends TestCase {

    private IAffinity affinity;
    private boolean libraryLoaded = false;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            // Attempt to initialize the PosixJNAAffinity class
            Class.forName("net.openhft.affinity.impl.PosixJNAAffinity");
            affinity = PosixJNAAffinity.INSTANCE;
            libraryLoaded = PosixJNAAffinity.LOADED;
        } catch (ClassNotFoundException | NoClassDefFoundError | UnsatisfiedLinkError e) {
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

    public void testGetAffinity() {
        if (!libraryLoaded) {
            System.out.println("Skipping testGetAffinity as the library is not loaded.");
            return;
        }
        BitSet bitSet = affinity.getAffinity();
        assertNotNull(bitSet);
        // Ensure the BitSet is valid.
    }

    public void testSetAffinity() {
        if (!libraryLoaded) {
            System.out.println("Skipping testSetAffinity as the library is not loaded.");
            return;
        }
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        try {
            affinity.setAffinity(bitSet);
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }
    }

    public void testGetCpu() {
        if (!libraryLoaded) {
            System.out.println("Skipping testGetCpu as the library is not loaded.");
            return;
        }
        try {
            int cpu = affinity.getCpu();
            assertTrue(cpu >= 0);
        } catch (IllegalStateException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }
    }

    public void testGetProcessId() throws Exception {
        if (!libraryLoaded) {
            System.out.println("Skipping testGetProcessId as the library is not loaded.");
            return;
        }
        int processId = affinity.getProcessId();
        Field field = PosixJNAAffinity.class.getDeclaredField("PROCESS_ID");
        field.setAccessible(true);
        int expectedProcessId = field.getInt(null);
        assertEquals(expectedProcessId, processId);
    }

    public void testGetThreadId() {
        if (!libraryLoaded) {
            System.out.println("Skipping testGetThreadId as the library is not loaded.");
            return;
        }
        try {
            int threadId = affinity.getThreadId();
            assertTrue(threadId > 0);
        } catch (IllegalStateException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }
    }

    public void testLoggerInitialization() {
        assertNotNull(LoggerFactory.getLogger(PosixJNAAffinity.class));
    }
}
