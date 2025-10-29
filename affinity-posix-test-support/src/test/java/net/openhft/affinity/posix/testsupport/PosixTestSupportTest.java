package net.openhft.affinity.posix.testsupport;

import net.openhft.posix.PosixAPI;
import org.junit.Test;

import java.util.BitSet;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PosixTestSupportTest {

    @Test
    public void canAttemptToLoadPosix() {
        PosixAPI api = PosixTestSupport.tryLoadPosix();
        assertNotNull("PosixAPI handle should be returned when the runtime offers an implementation", api);
    }

    @Test
    public void cpuMaskConversionsStayInSync() {
        int[] processorCounts = {0, 1, 2, 7, 8, 31, 32, 33, 63, 64, 65, 255, 256};
        for (int count : processorCounts) {
            int affinityBytes = net.openhft.affinity.internal.duplicated.CpuMaskConversion.requiredBytesForLogicalProcessors(count);
            int posixBytes = net.openhft.posix.internal.util.CpuMaskConversion.requiredBytesForLogicalProcessors(count);
            assertEquals("Byte requirement mismatch for processor count " + count, affinityBytes, posixBytes);
        }

        Random random = new Random(1234L);
        for (int logicalHint : processorCounts) {
            BitSet mask = randomBitSet(random, logicalHint + 32);
            int affinityBytes = net.openhft.affinity.internal.duplicated.CpuMaskConversion.requiredBytesForMask(mask, logicalHint);
            int posixBytes = net.openhft.posix.internal.util.CpuMaskConversion.requiredBytesForMask(mask, logicalHint);
            assertEquals("Mask byte requirement mismatch", affinityBytes, posixBytes);

            byte[] affinityBuffer = new byte[affinityBytes];
            byte[] posixBuffer = new byte[posixBytes];
            net.openhft.affinity.internal.duplicated.CpuMaskConversion.writeMask(mask, affinityBuffer);
            net.openhft.posix.internal.util.CpuMaskConversion.writeMask(mask, posixBuffer);
            assertEquals("Written mask mismatch", toBitString(affinityBuffer), toBitString(posixBuffer));

            BitSet fromAffinity = net.openhft.affinity.internal.duplicated.CpuMaskConversion.readMask(affinityBuffer);
            BitSet fromPosix = net.openhft.posix.internal.util.CpuMaskConversion.readMask(posixBuffer);
            assertEquals("Reconstructed mask mismatch", fromAffinity, fromPosix);
        }
    }

    private static BitSet randomBitSet(Random random, int maxBits) {
        BitSet bitSet = new BitSet(maxBits);
        for (int i = 0; i < maxBits; i++) {
            if (random.nextBoolean()) {
                bitSet.set(i);
            }
        }
        return bitSet;
    }

    private static String toBitString(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 8);
        for (byte b : data) {
            for (int i = 0; i < 8; i++) {
                sb.append((b >> i) & 1);
            }
        }
        return sb.toString();
    }
}
