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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PosixJNAAffinityStubTest {

    @Test
    public void stubTracksAffinityAndCpu() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        Assume.assumeFalse("Use stub only when native POSIX calls are unavailable", osName.contains("linux"));

        String previous = System.getProperty("chronicle.affinity.stub.posix");
        try {
            System.setProperty("chronicle.affinity.stub.posix", "true");

            BitSet mask = new BitSet();
            mask.set(1);
            mask.set(4);
            PosixJNAAffinity.INSTANCE.setAffinity(mask);

            BitSet actual = PosixJNAAffinity.INSTANCE.getAffinity();
            assertEquals("Stub should echo assigned affinity mask", mask, actual);
            assertEquals("Stub CPU should follow lowest set bit", 1, PosixJNAAffinity.INSTANCE.getCpu());
            assertTrue("Process id should be positive", PosixJNAAffinity.INSTANCE.getProcessId() > 0);
            assertTrue("Stub reports loaded state", PosixJNAAffinity.LOADED);

            int tid = PosixJNAAffinity.INSTANCE.getThreadId();
            assertEquals("Thread id should be stable across calls", tid, PosixJNAAffinity.INSTANCE.getThreadId());
        } finally {
            if (previous == null) {
                System.clearProperty("chronicle.affinity.stub.posix");
            } else {
                System.setProperty("chronicle.affinity.stub.posix", previous);
            }
        }
    }
}
