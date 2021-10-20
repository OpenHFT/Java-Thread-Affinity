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
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Test
    public void shouldAllocateCoresCorrectlyUnderContention() throws IOException, InterruptedException {
        Assume.assumeTrue(IS_LINUX);
        final int numberOfLockers = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors())) / 2;
        List<Process> lockers = new ArrayList<>();
        LOGGER.info("Running test with {} locker processes", numberOfLockers);
        for (int i = 0; i < numberOfLockers; i++) {
            lockers.add(ProcessRunner.runClass(RepeatedAffinityLocker.class,
                    new String[]{"-Djava.io.tmpdir=" + folder.getRoot().getAbsolutePath()},
                    new String[]{"last", "30", "2"}));
        }
        for (int i = 0; i < numberOfLockers; i++) {
            final Process process = lockers.get(i);
            if (!process.waitFor(20, TimeUnit.SECONDS)) {
                ProcessRunner.printProcessOutput("Stalled locking process", process);
                fail("Locker process didn't end in time");
            }
            if (process.exitValue() != 0) {
                ProcessRunner.printProcessOutput("Failed locking process", process);
                fail("At least one of the locking processes failed, see output above");
            }
            assertEquals(0, process.exitValue());
        }
    }

    @Test
    public void shouldBeAbleToAcquireLockLeftByOtherProcess() throws IOException, InterruptedException {
        Assume.assumeTrue(IS_LINUX);
        final Process process = ProcessRunner.runClass(AffinityLockerThatDoesNotReleaseProcess.class,
                new String[]{"-Djava.io.tmpdir=" + folder.getRoot().getAbsolutePath()},
                new String[]{"last"});
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            ProcessRunner.printProcessOutput("locker process", process);
            fail("Locker process timed out");
        }
        if (process.exitValue() != 0) {
            ProcessRunner.printProcessOutput("locker process", process);
            fail("Locker process failed");
        }
        // We should be able to acquire the lock despite the other process not explicitly releasing it
        try (final AffinityLock acquired = AffinityLock.acquireLock("last")) {
            assertEquals(AffinityLock.PROCESSORS - 1, acquired.cpuId());
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
                        final String metaInfo = FileLockBasedLockChecker.getInstance().getMetaInfo(affinityLock.cpuId());
                        LOGGER.info("Meta info is: " + metaInfo);
                        long lockPID = Long.parseLong(metaInfo);
                        if (lockPID != PID) {
                            throw new IllegalStateException(format("PID in lock file is not mine (lockPID=%d, myPID=%d)", lockPID, PID));
                        }
                        Thread.sleep(ThreadLocalRandom.current().nextInt(50));
                    } else {
                        LOGGER.info("Couldn't get a lock");
                    }
                }
            }
            return null;
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
}
