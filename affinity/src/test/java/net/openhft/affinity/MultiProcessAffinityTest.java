package net.openhft.affinity;

import net.openhft.affinity.common.ProcessRunner;
import net.openhft.affinity.lockchecker.FileBasedLockChecker;
import org.jetbrains.annotations.NotNull;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static net.openhft.affinity.LockCheck.IS_LINUX;
import static org.junit.Assert.*;

public class MultiProcessAffinityTest extends BaseAffinityTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiProcessAffinityTest.class);

    @Before
    public void setUp() {
        Assume.assumeTrue(IS_LINUX);
    }

    @Test
    public void shouldNotAcquireLockOnCoresLockedByOtherProcesses() throws IOException, InterruptedException {
        // run the separate affinity locker
        final Process affinityLockerProcess = ProcessRunner.runClass(AffinityLockerProcess.class,
                new String[]{"-Djava.io.tmpdir=" + folder.getRoot().getAbsolutePath()},
                new String[]{"last"});
        try {
            int lastCpuId = AffinityLock.PROCESSORS - 1;

            // wait for the CPU to be locked
            long endTime = System.currentTimeMillis() + 5_000;
            while (FileBasedLockChecker.getInstance().isLockFree(lastCpuId)) {
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
            waitForProcessToEnd(5, "Affinity locker process", affinityLockerProcess, false);
        }
    }

    @Test
    public void shouldAllocateCoresCorrectlyUnderContention() throws IOException, InterruptedException {
        final int numberOfLockers = Math.max(2, Math.min(12, Runtime.getRuntime().availableProcessors())) / 2;
        List<Process> lockers = new ArrayList<>();
        LOGGER.info("Running test with {} locker processes", numberOfLockers);
        for (int i = 0; i < numberOfLockers; i++) {
            lockers.add(ProcessRunner.runClass(RepeatedAffinityLocker.class,
                    new String[]{"-Djava.io.tmpdir=" + folder.getRoot().getAbsolutePath()},
                    new String[]{"last", "30", "2"}));
        }
        for (int i = 0; i < numberOfLockers; i++) {
            final Process process = lockers.get(i);
            waitForProcessToEnd(20, "Locking process", process);
        }
    }

    @Test
    public void shouldAllocateCoresCorrectlyUnderContentionWithFailures() throws IOException, InterruptedException {
        final int numberOfLockers = Math.max(2, Math.min(12, Runtime.getRuntime().availableProcessors())) / 2;
        List<Process> lockers = new ArrayList<>();
        LOGGER.info("Running test with {} locker processes", numberOfLockers);
        Process lockFileDropper = ProcessRunner.runClass(LockFileDropper.class);
        for (int i = 0; i < numberOfLockers; i++) {
            lockers.add(ProcessRunner.runClass(RepeatedAffinityLocker.class,
                    new String[]{"-Djava.io.tmpdir=" + folder.getRoot().getAbsolutePath()},
                    new String[]{"last", "30", "2"}));
        }
        for (int i = 0; i < numberOfLockers; i++) {
            final Process process = lockers.get(i);
            waitForProcessToEnd(20, "Locking process", process);
        }
        lockFileDropper.destroy();
        waitForProcessToEnd(5, "Lock file droppper", lockFileDropper);
    }

    @Test
    public void shouldBeAbleToAcquireLockLeftByOtherProcess() throws IOException, InterruptedException {
        final Process process = ProcessRunner.runClass(AffinityLockerThatDoesNotReleaseProcess.class,
                new String[]{"-Djava.io.tmpdir=" + folder.getRoot().getAbsolutePath()},
                new String[]{"last"});
        waitForProcessToEnd(5, "Locking process", process);
        // We should be able to acquire the lock despite the other process not explicitly releasing it
        try (final AffinityLock acquired = AffinityLock.acquireLock("last")) {
            assertEquals(AffinityLock.PROCESSORS - 1, acquired.cpuId());
        }
    }

    private void waitForProcessToEnd(int timeToWaitSeconds, String processDescription, Process process) throws InterruptedException {
        waitForProcessToEnd(timeToWaitSeconds, processDescription, process, true);
    }

    private void waitForProcessToEnd(int timeToWaitSeconds, String processDescription, Process process, boolean checkExitCode) throws InterruptedException {
        if (!process.waitFor(timeToWaitSeconds, TimeUnit.SECONDS)) {
            ProcessRunner.printProcessOutput(processDescription, process);
            fail(processDescription + " didn't end in time");
        }
        if (checkExitCode && process.exitValue() != 0) {
            ProcessRunner.printProcessOutput(processDescription, process);
            fail(processDescription + " failed, see output above (exit value " + process.exitValue() + ")");
        }
    }

    /**
     * Repeatedly acquires and releases a lock on the specified core
     */
    static class RepeatedAffinityLocker implements Callable<Void> {

        private static final Logger LOGGER = LoggerFactory.getLogger(RepeatedAffinityLocker.class);
        private static final long PID = LockCheck.getPID();
        private final int iterations;
        private final String cpuIdToLock;

        public static void main(String[] args) throws InterruptedException, ExecutionException {
            String cpuIdToLock = args[0];
            int iterations = Integer.parseInt(args[1]);
            int threads = Integer.parseInt(args[2]);

            LOGGER.info("Acquiring lock with {} threads, {} iterations", threads, iterations);
            ExecutorService executorService = Executors.newFixedThreadPool(threads);
            final List<Future<Void>> futures = executorService.invokeAll(IntStream.range(0, threads)
                    .mapToObj(tid -> new RepeatedAffinityLocker(cpuIdToLock, iterations))
                    .collect(Collectors.toList()));
            for (Future<Void> future : futures) {
                future.get();
            }
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Executor service didn't shut down");
            }
        }

        public RepeatedAffinityLocker(String cpuIdToLock, int iterations) {
            this.iterations = iterations;
            this.cpuIdToLock = cpuIdToLock;
        }

        @Override
        public Void call() throws Exception {
            for (int i = 0; i < iterations; i++) {
                LOGGER.info("******* Starting iteration {} at {}", i, LocalDateTime.now());
                try (final AffinityLock affinityLock = AffinityLock.acquireLock(cpuIdToLock)) {
                    if (affinityLock.isAllocated()) {
                        assertLockFileContainsMyPid(affinityLock);
                        Thread.sleep(ThreadLocalRandom.current().nextInt(50));
                        assertLockFileContainsMyPid(affinityLock);
                    } else {
                        LOGGER.info("Couldn't get a lock");
                    }
                }
            }
            return null;
        }

        private void assertLockFileContainsMyPid(AffinityLock affinityLock) throws IOException {
            final String metaInfo = FileBasedLockChecker.getInstance().getMetaInfo(affinityLock.cpuId());
            long lockPID = Long.parseLong(metaInfo);
            if (lockPID != PID) {
                throw new IllegalStateException(format("PID in lock file is not mine (lockPID=%d, myPID=%d)", lockPID, PID));
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

    /**
     * Acquires a lock then ends
     */
    static class AffinityLockerThatDoesNotReleaseProcess {
        private static final Logger LOGGER = LoggerFactory.getLogger(AffinityLockerProcess.class);

        public static void main(String[] args) {
            String cpuIdToLock = args[0];

            final AffinityLock affinityLock = AffinityLock.acquireLock(cpuIdToLock);
            LOGGER.info("Got affinity lock " + affinityLock + " at " + LocalDateTime.now() + ", CPU=" + affinityLock.cpuId());
        }
    }

    /**
     * Simulate failing processes by repeatedly dropping lock files
     * with PIDs of non-existent processes
     */
    static class LockFileDropper {

        public static void main(String[] args) throws InterruptedException, IOException {
            while (Thread.currentThread().isInterrupted()) {
                for (int cpu = 0; cpu < AffinityLock.PROCESSORS; cpu++) {
                    try {
                        File lockFile = toFile(cpu);
                        try (final FileChannel fc = FileChannel.open(lockFile.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                            final long maxValue = Long.MAX_VALUE; // a PID that never exists
                            ByteBuffer buffer = ByteBuffer.wrap((maxValue + "\n").getBytes(StandardCharsets.UTF_8));
                            while (buffer.hasRemaining()) {
                                fc.write(buffer);
                            }
                        }
                    } catch (FileAlreadyExistsException e) {
                        LOGGER.info("Failed, trying again");
                    }
                    Thread.sleep(ThreadLocalRandom.current().nextInt(50));
                }
            }
        }

        /**
         * This is copied from {@link FileBasedLockChecker} it should probably be called directly
         * but it's not visible
         *
         * @param id The CPU ID whose lock file to create
         * @return The lock file
         */
        @NotNull
        protected static File toFile(int id) {
            assert id >= 0;
            File file = new File(tmpDir(), "cpu-" + id + ".lock");
            return file;
        }

        private static File tmpDir() {
            final File tempDir = new File(System.getProperty("java.io.tmpdir"));

            if (!tempDir.exists())
                tempDir.mkdirs();

            return tempDir;
        }
    }
}
