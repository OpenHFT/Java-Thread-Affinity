package net.openhft.affinity.posix.testsupport;

import net.openhft.posix.PosixAPI;

/**
 * Lightweight bridge for JVMs that want to share Posix-backed test fixtures.
 * This module keeps Posix on the classpath beside the affinity code base,
 * enabling future reuse without forcing the main runtime code to depend on it yet.
 */
public final class PosixTestSupport {

    private PosixTestSupport() {
        // utility
    }

    /**
     * Returns a Posix API handle if one can be loaded, otherwise null.
     * Tests can use this to decide whether to exercise Posix-backed behaviour.
     */
    public static PosixAPI tryLoadPosix() {
        try {
            return PosixAPI.posix();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
