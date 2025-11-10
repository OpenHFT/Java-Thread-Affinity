//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Unit test to verify that releasing an {@link AffinityLock} restores the
 * affinity mask back to {@link AffinityLock#BASE_AFFINITY}.
 */
public class AffinityLockReleaseTest extends BaseAffinityTest {

    @Test
    public void acquireAndReleaseShouldRestoreBaseAffinity() throws Exception {
        if (!new File("/proc/cpuinfo").exists()) {
            System.out.println("Cannot run affinity test as this system doesn't have a /proc/cpuinfo file");
            return;
        }

        // initialise CPU layout from the running machine so acquireLock works
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
        AffinityLock lock = AffinityLock.acquireLock();
        assertEquals(1, Affinity.getAffinity().cardinality());
        lock.release();
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
    }
}
