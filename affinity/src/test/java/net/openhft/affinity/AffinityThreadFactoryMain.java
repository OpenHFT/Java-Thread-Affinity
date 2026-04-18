/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.openhft.affinity.AffinityStrategies.*;

/**
 * @author peter.lawrey
 */
public final class AffinityThreadFactoryMain {
    private static final ExecutorService ES = Executors.newFixedThreadPool(4,
            new AffinityThreadFactory("bg", SAME_CORE, DIFFERENT_SOCKET, ANY));

    private AffinityThreadFactoryMain() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static void main(String... args) throws InterruptedException {
        for (int i = 0; i < 12; i++)
            ES.submit((Callable<Void>) () -> {
                Thread.sleep(100);
                return null;
            });
        Thread.sleep(200);
        System.out.println("\nThe assignment of CPUs is\n" + AffinityLock.dumpLocks());
        ES.shutdown();
        //noinspection ResultOfMethodCallIgnored
        ES.awaitTermination(1, TimeUnit.SECONDS);
    }
}
