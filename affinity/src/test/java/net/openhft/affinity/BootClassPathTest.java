package net.openhft.affinity;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BootClassPathTest {
    @Test
    public void shouldDetectClassesOnClassPath() throws Exception {
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Thread"));
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Runtime"));
    }
}