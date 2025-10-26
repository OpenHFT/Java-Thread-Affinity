package net.openhft.affinity.impl;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

public class CpuSetUtilTest {

    @Test
    public void requiredBytesRoundsUpToEightByteBlocks() {
        assertEquals(Long.BYTES, CpuSetUtil.requiredBytesForLogicalProcessors(1));
        assertEquals(Long.BYTES, CpuSetUtil.requiredBytesForLogicalProcessors(64));
        assertEquals(Long.BYTES * 2, CpuSetUtil.requiredBytesForLogicalProcessors(65));
        assertEquals(Long.BYTES * 3, CpuSetUtil.requiredBytesForLogicalProcessors(129));
    }

    @Test
    public void writeAndReadMaskAcrossWordBoundaries() {
        BitSet affinity = new BitSet();
        affinity.set(0);
        affinity.set(63);
        affinity.set(64);
        affinity.set(127);

        int bytes = CpuSetUtil.requiredBytesForMask(affinity, 128);
        byte[] target = new byte[bytes];
        CpuSetUtil.writeMask(affinity, target);

        BitSet roundTrip = CpuSetUtil.readMask(target);
        assertEquals(affinity, roundTrip);
    }
}
