package net.openhft.affinity;

import net.openhft.affinity.common.ProcessRunner;
import net.openhft.affinity.lockchecker.FileLockBasedLockChecker;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static net.openhft.affinity.LockCheck.IS_LINUX;
import static org.junit.Assert.*;

public class MultiProcessAffinityTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiProcessAffinityTest.class);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String originalTmpDir;

    @Before
    public void setUp() {
        originalTmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", folder.getRoot().getAbsolutePath());
    }

    @After
    public void tearDown() {
        System.setProperty("java.io.tmpdir", originalTmpDir);
    }

    @Test
    public void shouldNotAcquireLockOnCoresLockedByOtherProcesses() throws IOException, InterruptedException {
        Assume.assumeTrue(IS_LINUX);
        // run the separate affinity locker
        final Process affinityLockerProcess = ProcessRunner.runClass(AffinityLockerProcess.class,
                new String[]{"-Djava.io.tmpdir=" + folder.getRoot().getAbsolutePath()},
                new String[]{"last"});
        try {
            int lastCpuId = AffinityLock.PROCESSORS - 1;

            // wait for the CPU to be locked
            long endTime = System.currentTimeMillis() + 5_000;
            while (FileLockBasedLockChecker.getInstance().isLockFree(lastCpuId)) {
                Thread.sleep(100);
                if (System.currentTimeMillis() > endTime) {
                    LOGGER.info("Timed out waiting for the lock to be acquired: isAlive={}, exitCode={}",
                            affinityLockerProcess.isAlive(), affinityLockerProcess.isAlive() ? "N/A" : affinityLockerProcess.exitValue());
                    ProcessRunner.printProcessOutput("AffinityLockerProcess", affinityLockerProcess);
                    fail("Timed out waiting for the sub-process to acquire the lock");
                }
            }

            try (AffinityLock lock = AffinityLock.acquireLock("last")) {
                assertNotEquals(lastCpuId, lock.cpuId());
            }
        } finally {
            affinityLockerProcess.destroy();
            if (!affinityLockerProcess.waitFor(5, TimeUnit.SECONDS)) {
                fail("Sub-process didn't terminate!");
            }
        }
    }

    @Ignore("https://github.com/OpenHFT/Java-Thread-Affinity/issues/81")
    @Test
    public void shouldAllocateCoresCorrectlyUnderContention() throws IOException, InterruptedException {
        final int numberOfLockers = Math.max(8, Runtime.getRuntime().availableProcessors());
        List<Process> lockers = new ArrayList<>();
        for (int i = 0; i < numberOfLockers; i++) {
            lockers.add(ProcessRunner.runClass(RepeatedAffinityLocker.class, "last", "100"));
        }
        for (int i = 0; i < numberOfLockers; i++) {
            if (!lockers.get(i).waitFor(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Locker process didn't end in time");
            }
            assertEquals(0, lockers.get(i).exitValue());
        }
    }

    /**
     * Repeatedly acquires and releases a lock on the specified core
     */
    static class RepeatedAffinityLocker {

        private static final long PID = LockCheck.getPID();

        public static void main(String[] args) throws IOException, InterruptedException {
            String cpuIdToLock = args[0];
            int iterations = Integer.parseInt(args[1]);

            for (int i = 0; i < iterations; i++) {
                try (final AffinityLock affinityLock = AffinityLock.acquireLock(cpuIdToLock)) {
                    long lockPID = Long.parseLong(FileLockBasedLockChecker.getInstance().getMetaInfo(affinityLock.cpuId()));
                    if (lockPID != PID) {
                        throw new IllegalStateException(format("PID in lock file is not mine (lockPID=%d, myPID=%d)", lockPID, PID));
                    }
                    Thread.sleep(ThreadLocalRandom.current().nextInt(50));
                }
            }
        }
    }

    /**
     * Acquires a lock on the specified CPU, holds it until interrupted
     */
    static class AffinityLockerProcess {

        private static final Logger LOGGER = LoggerFactory.getLogger(AffinityLockerProcess.class);

        public static void main(String[] args) {
            String cpuIdToLock = args[0];

            try (final AffinityLock affinityLock = AffinityLock.acquireLock(cpuIdToLock)) {
                LOGGER.info("Got affinity lock " + affinityLock + " at " + LocalDateTime.now() + ", CPU=" + affinityLock.cpuId());
                Thread.sleep(Integer.MAX_VALUE);
                LOGGER.error("Woke from sleep? this should never happen");
            } catch (InterruptedException e) {
                // expected, just end
                LOGGER.info("Interrupted at " + LocalDateTime.now() + " lock is released");
            }
        }
    }
}
