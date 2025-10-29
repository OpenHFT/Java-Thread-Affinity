package net.openhft.posix.internal.util;

import java.util.Arrays;
import java.util.BitSet;

/**
 * DUPLICATED with net.openhft.affinity.internal.duplicated.CpuMaskConversion.
 * <p>
 * This class is housed in the shared test-support module for now so that both the Posix and
 * Java Thread Affinity code paths can evolve independently while staying in sync. Once Posix
 * ships the canonical implementation this duplicate should be removed and the affinity module
 * should consume it directly.
 */
public final class CpuMaskConversion {

    private CpuMaskConversion() {
    }

    public static int requiredBytesForLogicalProcessors(int logicalProcessors) {
        long processors = Math.max(1L, logicalProcessors);
        long groups = (processors + Long.SIZE - 1) / Long.SIZE;
        long bytes = Math.max(1L, groups) * Long.BYTES;
        if (bytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("CPU mask size exceeds integer addressable space");
        }
        return (int) bytes;
    }

    public static int requiredBytesForMask(BitSet mask, int logicalProcessorsHint) {
        int requiredBits = Math.max(1, Math.max(mask.length(), logicalProcessorsHint));
        return requiredBytesForLogicalProcessors(requiredBits);
    }

    public static void writeMask(BitSet affinity, byte[] target) {
        Arrays.fill(target, (byte) 0);
        byte[] source = affinity.toByteArray();
        System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
    }

    public static BitSet readMask(byte[] source) {
        return BitSet.valueOf(source);
    }
}
