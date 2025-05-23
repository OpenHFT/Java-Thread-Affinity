package net.openhft.affinity;

import org.junit.Assume;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class SetThreadIdTest {

    @Test
    public void setThreadIdShouldUpdateThreadTidField() throws Exception {
        Field tidFieldField = Affinity.class.getDeclaredField("THREAD_TID_FIELD");
        tidFieldField.setAccessible(true);
        Field tidField = (Field) tidFieldField.get(null);
        Assume.assumeTrue(tidField != null);
        tidField.setAccessible(true);

        Affinity.setThreadId();
        long expected = Affinity.getThreadId();
        long actual = tidField.getLong(Thread.currentThread());
        assertEquals(expected, actual);
    }
}
