package net.openhft.affinity.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.BitSet;

/**
 * Created by andre on 20/06/15.
 */
public class Utilities {
    /**
     * Creates a hexademical representation of the bit set
     *
     * @param set the bit set to convert
     * @return the hexademical string representation
     */
    public static String toHexString(final BitSet set) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        final long[] longs = set.toLongArray();
        for (int i = 0; i < longs.length; i++) {
            writer.write(Long.toHexString(longs[i]));
        }
        writer.flush();

        return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    public static String toBinaryString(BitSet set) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        final long[] longs = set.toLongArray();
        for (int i = 0; i < longs.length; i++) {
            writer.write(Long.toBinaryString(longs[i]));
        }
        writer.flush();

        return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }
}
