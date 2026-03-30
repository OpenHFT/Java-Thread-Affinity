/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

public class BaseAffinityTest {

    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    File folder;
    private String originalTmpDir;

    @BeforeEach
    void setTmpDirectory() {
        originalTmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", folder.getAbsolutePath());
    }

    @AfterEach
    void afterEachBaseAffinityTest() {
        restoreTmpDirectoryAndReleaseAllLocks();
        baseAffinity();
    }

    public void restoreTmpDirectoryAndReleaseAllLocks() {
        // don't leave any locks locked
        for (int i = 0; i < AffinityLock.PROCESSORS; i++) {
            LockCheck.releaseLock(i);
        }
        System.setProperty("java.io.tmpdir", originalTmpDir);
    }

    public void baseAffinity() {
        BitSet affinity = Affinity.getAffinity();
        Affinity.resetToBaseAffinity();
        assertEquals(AffinityLock.BASE_AFFINITY, affinity);
    }
}
