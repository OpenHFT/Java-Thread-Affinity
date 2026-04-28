/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseAffinitySupport {

    @TempDir
    Path folder;
    private String originalTmpDir;

    @BeforeEach
    public void setTmpDirectory() {
        originalTmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", folder.toAbsolutePath().toString());
    }

    @AfterEach
    public void restoreTmpDirectoryAndReleaseAllLocks() {
        BitSet affinity = Affinity.getAffinity();
        Affinity.resetToBaseAffinity();
        assertEquals(AffinityLock.BASE_AFFINITY, affinity, "base affinity");
        // don't leave any locks locked
        for (int i = 0; i < AffinityLock.PROCESSORS; i++) {
            LockCheck.releaseLock(i);
        }
        System.setProperty("java.io.tmpdir", originalTmpDir);
    }
}
