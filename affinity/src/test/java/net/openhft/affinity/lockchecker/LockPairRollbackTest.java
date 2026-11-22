/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.lockchecker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class LockPairRollbackTest {
    @TempDir Path directory;

    private FileLockBasedLockChecker checker() {
        return new FileLockBasedLockChecker() {
            @Override protected File toFile(int id) { return directory.resolve("cpu-" + id).toFile(); }
        };
    }

    @Test
    void secondLockContentionReleasesFirstLock() throws Exception {
        FileLockBasedLockChecker first = checker(), competitor = checker();
        try {
            assertTrue(competitor.obtainLock(2, -1, "owner"));
            assertFalse(first.obtainLock(1, 2, "pair"));
            assertTrue(competitor.obtainLock(1, -1, "reacquired"), "failed pair must not strand its first lock");
        } finally {
            first.releaseLock(1); first.releaseLock(2);
            competitor.releaseLock(1); competitor.releaseLock(2);
        }
    }

    @Test
    void secondLockIOExceptionReleasesFirstLock() throws Exception {
        Files.createDirectory(directory.resolve("cpu-2"));
        FileLockBasedLockChecker first = checker(), competitor = checker();
        try {
            assertThrows(IOException.class, () -> first.obtainLock(1, 2, "pair"));
            assertTrue(competitor.obtainLock(1, -1, "reacquired"));
        } finally { first.releaseLock(1); competitor.releaseLock(1); }
    }

    @Test
    void successfulPairAndOrphanChecksPreserveOwnership() throws Exception {
        FileLockBasedLockChecker owner = checker(), observer = checker();
        assertTrue(owner.isLockFree(1));
        Files.write(directory.resolve("cpu-1"), new byte[0]);
        assertTrue(observer.isLockFree(1));
        try {
            assertTrue(owner.obtainLock(1, 2, "pair"));
            assertFalse(owner.isLockFree(1));
            assertFalse(observer.isLockFree(1));
            assertFalse(observer.obtainLock(1, -1, "other"));
            assertTrue(owner.getMetaInfo(1).startsWith("pair"));
            assertFalse(observer.isLockFree(2));
        } finally { owner.releaseLock(1); owner.releaseLock(2); }
        assertTrue(observer.isLockFree(1));
        assertTrue(observer.isLockFree(2));
        assertFalse(owner.releaseLock(1));
    }
}
