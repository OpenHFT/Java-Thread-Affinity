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
            ES.submit(new Callable<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    Thread.sleep(100);
                    return null;
                }
            });
        Thread.sleep(200);
        System.out.println("\nThe assignment of CPUs is\n" + AffinityLock.dumpLocks());
        ES.shutdown();
        ES.awaitTermination(1, TimeUnit.SECONDS);
    }
}
