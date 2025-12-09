/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class BaseAffinityTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();
    private String originalTmpDir;

    @Before
    public void setTmpDirectory() {
        originalTmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", folder.getRoot().getAbsolutePath());
    }

    @After
    public void restoreTmpDirectoryAndReleaseAllLocks() {
        // don't leave any locks locked
        for (int i = 0; i < AffinityLock.PROCESSORS; i++) {
            LockCheck.releaseLock(i);
        }
        System.setProperty("java.io.tmpdir", originalTmpDir);
    }

    @After
    public void baseAffinity() {
        BitSet affinity = Affinity.getAffinity();
        Affinity.resetToBaseAffinity();
        assertEquals(AffinityLock.BASE_AFFINITY, affinity);
    }
}
