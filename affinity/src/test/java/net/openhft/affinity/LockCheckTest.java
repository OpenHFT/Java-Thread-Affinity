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

package net.openhft.affinity;

import net.openhft.affinity.testimpl.TestFileLockBasedLockChecker;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import static net.openhft.affinity.LockCheck.IS_LINUX;

/**
 * @author Rob Austin.
 */
public class LockCheckTest extends BaseAffinityTest {

    private final TestFileLockBasedLockChecker lockChecker = new TestFileLockBasedLockChecker();
    private int cpu = 11;

    @Before
    public void before() {
        Assume.assumeTrue(IS_LINUX);
    }

    @Test
    public void test() throws IOException {
        Assert.assertTrue(LockCheck.isCpuFree(cpu));
        LockCheck.updateCpu(cpu);
        Assert.assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(cpu));
    }

    @Test
    public void testPidOnLinux() {
        Assert.assertTrue(LockCheck.isProcessRunning(LockCheck.getPID()));
    }

    @Test
    public void testReplace() throws IOException {
        cpu++;
        Assert.assertTrue(LockCheck.isCpuFree(cpu + 1));
        LockCheck.replacePid(cpu, 123L);
        Assert.assertEquals(123L, LockCheck.getProcessForCpu(cpu));
    }

    @Test
    public void shouldNotBlowUpIfPidFileIsEmpty() throws Exception {
        LockCheck.updateCpu(cpu);

        final File file = lockChecker.doToFile(cpu);
        new RandomAccessFile(file, "rw").setLength(0);

        LockCheck.isCpuFree(cpu);
    }

    @Test
    public void shouldNotBlowUpIfPidFileIsCorrupt() throws Exception {
        LockCheck.updateCpu(cpu);

        final File file = lockChecker.doToFile(cpu);
        try (final FileWriter writer = new FileWriter(file, false)) {
            writer.append("not a number\nnot a date");
        }

        LockCheck.isCpuFree(cpu);
    }
}