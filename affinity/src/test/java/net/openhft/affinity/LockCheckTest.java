/*
 * Copyright 2016 higherfrequencytrading.com
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

package net.openhft.affinity;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static net.openhft.affinity.LockCheck.IS_LINUX;

/**
 * @author Rob Austin.
 */
public class LockCheckTest {

    private static int CPU = 1111;

    @Before
    public void before() {
        Assume.assumeTrue(IS_LINUX);
        System.setProperty("java.io.tmpdir", LockCheck.TARGET + "/" + System.nanoTime());
    }

    @Test
    public void test() throws IOException {
        Assert.assertTrue(LockCheck.isCpuFree(CPU));
        LockCheck.updateCpu(CPU);
        Assert.assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(CPU));
    }

    @Test
    public void testPidOnLinux() {
        Assert.assertTrue(LockCheck.isProcessRunning(LockCheck.getPID()));
    }

    @Test
    public void testReplace() throws IOException {
        CPU++;
        Assert.assertTrue(LockCheck.isCpuFree(CPU + 1));
        LockCheck.replacePid(CPU, 123L);
        Assert.assertEquals(123L, LockCheck.getProcessForCpu(CPU));
    }


}
