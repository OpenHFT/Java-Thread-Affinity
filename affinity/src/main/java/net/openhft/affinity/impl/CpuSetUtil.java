package net.openhft.affinity.impl;

import net.openhft.affinity.internal.duplicated.CpuMaskConversion;

import java.util.BitSet;

/**
 * Utility methods for working with CPU affinity masks in a platform-neutral fashion.
 */
final class CpuSetUtil {

    private CpuSetUtil() {
    }

    static int requiredBytesForLogicalProcessors(int logicalProcessors) {
        return CpuMaskConversion.requiredBytesForLogicalProcessors(logicalProcessors);
    }

    static int requiredBytesForMask(BitSet mask, int logicalProcessorsHint) {
        return CpuMaskConversion.requiredBytesForMask(mask, logicalProcessorsHint);
    }

    static void writeMask(BitSet affinity, byte[] target) {
        CpuMaskConversion.writeMask(affinity, target);
    }

    static BitSet readMask(byte[] source) {
        return CpuMaskConversion.readMask(source);
    }
}
