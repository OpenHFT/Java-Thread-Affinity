/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.testimpl;

import net.openhft.affinity.lockchecker.FileLockBasedLockChecker;

import java.io.File;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class TestFileLockBasedLockChecker extends FileLockBasedLockChecker {

    public File doToFile(int cpu) {
        return toFile(cpu);
    }
}
