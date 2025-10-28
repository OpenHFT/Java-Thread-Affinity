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
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.BitSet;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class OSXJNAAffinityStubTest {

    @BeforeClass
    public static void enableStub() {
        System.setProperty("chronicle.affinity.stub.osx", "true");
    }

    @Test
    public void stubReturnsMaskedThreadId() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        Assume.assumeFalse("Stub should not override native macOS library", osName.contains("mac"));

        BitSet affinity = OSXJNAAffinity.INSTANCE.getAffinity();
        assertFalse("Affinity should be empty on macOS stub", affinity.get(0));

        int tid = OSXJNAAffinity.INSTANCE.getThreadId();
        assertEquals("Stubbed pthread id should match configured constant", 0x123456, tid);
    }
}
