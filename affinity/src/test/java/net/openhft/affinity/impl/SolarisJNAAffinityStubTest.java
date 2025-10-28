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

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SolarisJNAAffinityStubTest {

    @Test
    public void stubReturnsConsistentThreadId() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        Assume.assumeFalse("Do not override native Solaris library", osName.contains("sunos"));

        String previous = System.getProperty("chronicle.affinity.stub.solaris");
        try {
            System.setProperty("chronicle.affinity.stub.solaris", "true");

            assertTrue("Affinity remains empty for Solaris stub", SolarisJNAAffinity.INSTANCE.getAffinity().isEmpty());

            int tid = SolarisJNAAffinity.INSTANCE.getThreadId();
            assertEquals("Stubbed pthread id should match configured constant", 0x654321, tid);
            assertEquals("Thread id should be cached per thread", tid, SolarisJNAAffinity.INSTANCE.getThreadId());
        } finally {
            if (previous == null) {
                System.clearProperty("chronicle.affinity.stub.solaris");
            } else {
                System.setProperty("chronicle.affinity.stub.solaris", previous);
            }
        }
    }
}
