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

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class AffinityLockInvalidCpuTest {

    @Test
    public void acquiringLockWithOutOfRangeCpuReturnsNoLock() {
        try (AffinityLock lock = AffinityLock.acquireLock(AffinityLock.PROCESSORS)) {
            assertFalse("Expected no lock to be allocated for out of range cpuId",
                    lock.isAllocated());
        }
    }

    @Test
    public void acquiringLockFromInvalidCpuListReturnsNoLock() {
        int[] candidates = {AffinityLock.PROCESSORS, -1, Integer.MIN_VALUE};
        try (AffinityLock lock = AffinityLock.acquireLock(candidates)) {
            assertFalse("Expected no lock to be allocated when all candidates are invalid",
                    lock.isAllocated());
        }
    }
}
