/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Rob Austin.
 */
public class LockCheckTest extends AbstractLockCheckTest {

    @Override
    protected int initialCpu() {
        return 11;
    }

    @Test
    public void testNegativePidOnLinux() {
        assertFalse(LockCheck.isProcessRunning(-1), "negative PID should not be considered running");
    }

    @Test
    public void shouldNotBlowUpIfPidFileIsCorrupt() throws Exception {
        LockCheck.updateCpu(cpu, 0);

        final File file = lockChecker.doToFile(cpu);
        try (final OutputStreamWriter writer =
                     new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
            writer.append("not a number\nnot a date");
        }

        assertDoesNotThrow(() -> LockCheck.isCpuFree(cpu), "corrupt PID file should not throw");
    }
}
