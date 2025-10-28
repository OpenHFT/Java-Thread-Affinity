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

public class LinuxJNAAffinityStubTest {

    @Test
    public void stubbedHelperProvidesDeterministicValues() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        Assume.assumeFalse("Skip stub when running on native Linux", osName.contains("linux"));

        String previous = System.getProperty("chronicle.affinity.stub.linux");
        try {
            System.setProperty("chronicle.affinity.stub.linux", "true");

            BitSet initial = new BitSet();
            initial.set(2);
            LinuxHelper.setStubAffinity(initial);
            LinuxHelper.setStubCpu(2);

            BitSet observed = LinuxJNAAffinity.INSTANCE.getAffinity();
            assertTrue("Affinity should reflect stub mask", observed.get(2));
            assertTrue("Linux affinity stub reports loaded", LinuxJNAAffinity.LOADED);
            assertTrue("Process id should be positive", LinuxJNAAffinity.INSTANCE.getProcessId() > 0);

            BitSet update = new BitSet();
            update.set(5);
            LinuxJNAAffinity.INSTANCE.setAffinity(update);

            BitSet updated = LinuxJNAAffinity.INSTANCE.getAffinity();
            assertTrue("Updated affinity should reflect new cpu", updated.get(5));
            assertEquals("Stub CPU should follow latest assignment", 5, LinuxJNAAffinity.INSTANCE.getCpu());

            int tid = LinuxJNAAffinity.INSTANCE.getThreadId();
            assertEquals("Thread id should be stable for stubbed helper", tid, LinuxJNAAffinity.INSTANCE.getThreadId());
        } finally {
            if (previous == null) {
                System.clearProperty("chronicle.affinity.stub.linux");
            } else {
                System.setProperty("chronicle.affinity.stub.linux", previous);
            }
        }
    }
}
