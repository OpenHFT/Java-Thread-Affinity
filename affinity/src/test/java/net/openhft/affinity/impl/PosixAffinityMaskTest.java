package net.openhft.affinity.impl;

import org.junit.Test;

import java.util.Arrays;
import java.util.BitSet;

import static org.junit.Assert.assertEquals;

public class PosixAffinityMaskTest {

    @Test
    public void fakeSchedSetAndGetRoundTripForNinetySixCores() {
        FakeScheduler scheduler = new FakeScheduler(96);
        BitSet affinity = new BitSet();
        affinity.set(0);
        affinity.set(31);
        affinity.set(32);
        affinity.set(63);
        affinity.set(64);
        affinity.set(95);

        scheduler.sched_setaffinity(affinity);

        BitSet observed = scheduler.sched_getaffinity();
        assertEquals(affinity, observed);
    }

    private static final class FakeScheduler {
        private final int logicalProcessors;
        private byte[] stored;

        FakeScheduler(int logicalProcessors) {
            this.logicalProcessors = logicalProcessors;
            this.stored = new byte[CpuSetUtil.requiredBytesForLogicalProcessors(logicalProcessors)];
        }

        void sched_setaffinity(BitSet affinity) {
            int bytes = CpuSetUtil.requiredBytesForMask(affinity, logicalProcessors);
            if (stored.length != bytes) {
                stored = new byte[bytes];
            } else {
                Arrays.fill(stored, (byte) 0);
            }
            CpuSetUtil.writeMask(affinity, stored);
        }

        BitSet sched_getaffinity() {
            return CpuSetUtil.readMask(stored);
        }
    }
}
