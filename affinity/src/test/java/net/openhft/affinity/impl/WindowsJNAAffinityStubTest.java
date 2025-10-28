/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity.impl;

import org.junit.Assume;
import org.junit.Test;

import java.util.BitSet;
import java.util.Locale;

import static org.junit.Assert.*;

public class WindowsJNAAffinityStubTest {

    @Test
    public void stubProvidesDeterministicAffinity() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        Assume.assumeFalse("Stub test should not run on native Windows", osName.contains("win"));

        String previous = System.getProperty("chronicle.affinity.stub.windows");
        try {
            System.setProperty("chronicle.affinity.stub.windows", "true");

            BitSet mask = new BitSet();
            mask.set(3);
            WindowsJNAAffinity.INSTANCE.setAffinity(mask);

            BitSet actual = WindowsJNAAffinity.INSTANCE.getAffinity();
            assertTrue("Stub should reflect affinity mask", actual.get(3));
            assertTrue("Process id should be positive", WindowsJNAAffinity.INSTANCE.getProcessId() > 0);
            assertFalse("Stubbed implementation should report as not loaded", WindowsJNAAffinity.LOADED);

            int threadId = WindowsJNAAffinity.INSTANCE.getThreadId();
            assertEquals("Thread id should be stable for same thread", threadId, WindowsJNAAffinity.INSTANCE.getThreadId());
        } finally {
            if (previous == null) {
                System.clearProperty("chronicle.affinity.stub.windows");
            } else {
                System.setProperty("chronicle.affinity.stub.windows", previous);
            }
        }
    }
}
