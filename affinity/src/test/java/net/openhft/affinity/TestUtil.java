package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;

import java.io.IOException;

public final class TestUtil {

    private TestUtil() {
    }

    public static int processorCount() {
        try {
            return VanillaCpuLayout.fromCpuInfo().cpus();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
