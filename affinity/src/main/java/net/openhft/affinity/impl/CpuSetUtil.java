package net.openhft.affinity.impl;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Utility methods for working with CPU affinity masks in a platform-neutral fashion.
 */
final class CpuSetUtil {

    private CpuSetUtil() {
    }

    static int requiredBytesForLogicalProcessors(int logicalProcessors) {
        long processors = Math.max(1L, logicalProcessors);
        long groups = (processors + Long.SIZE - 1) / Long.SIZE;
        long bytes = Math.max(1L, groups) * Long.BYTES;
        if (bytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("CPU mask size exceeds integer addressable space");
        }
        return (int) bytes;
    }

    static int requiredBytesForMask(BitSet mask, int logicalProcessorsHint) {
        int requiredBits = Math.max(1, Math.max(mask.length(), logicalProcessorsHint));
        return requiredBytesForLogicalProcessors(requiredBits);
    }

    static void writeMask(BitSet affinity, byte[] target) {
        Arrays.fill(target, (byte) 0);
        byte[] source = affinity.toByteArray();
        System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
    }

    static BitSet readMask(byte[] source) {
        return BitSet.valueOf(source);
    }
}
