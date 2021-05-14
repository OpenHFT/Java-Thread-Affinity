package net.openhft.affinity;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BootClassPathTest {
    @Test
    public void shouldDetectClassesOnClassPath() {
        if (!System.getProperty("java.version").startsWith("1.8"))
            return;
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Thread"));
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Runtime"));
    }
}