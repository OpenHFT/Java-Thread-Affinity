package net.openhft.affinity.posix.testsupport;

import net.openhft.posix.PosixAPI;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PosixTestSupportTest {

    @Test
    public void canAttemptToLoadPosix() {
        // Even if the runtime cannot provide a concrete implementation, the call should succeed
        // without throwing, enabling higher-level tests to branch on null.
        PosixAPI api = PosixTestSupport.tryLoadPosix();
        assertNotNull("PosixAPI handle should be returned when the runtime offers an implementation",
                api);
    }
}
