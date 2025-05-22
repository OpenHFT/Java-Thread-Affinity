import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertSame;

public class AffinityJNALoadFailureTest {

    @Test
    public void shouldReturnNullAffinityWhenJnaFailsToLoad() throws Exception {
        // Build URLs from current classpath
        String[] cp = System.getProperty("java.class.path").split(File.pathSeparator);
        List<URL> urlList = new ArrayList<>();
        for (String p : cp) {
            urlList.add(new File(p).toURI().toURL());
        }
        URLClassLoader cl = new URLClassLoader(urlList.toArray(new URL[0]), null);

        // Force JNA version < 5 in the new classloader
        Class<?> nativeCls = Class.forName("com.sun.jna.Native", true, cl);
        Field verField = nativeCls.getDeclaredField("VERSION");
        verField.setAccessible(true);
        Field modField = Field.class.getDeclaredField("modifiers");
        modField.setAccessible(true);
        modField.setInt(verField, verField.getModifiers() & ~Modifier.FINAL);
        Object originalVer = verField.get(null);
        verField.set(null, "4.0.0");
        try {
            Class<?> affinityCls = Class.forName("net.openhft.affinity.Affinity", true, cl);
            Method m = affinityCls.getMethod("getAffinityImpl");
            Object impl = m.invoke(null);
            Class<?> nullAffinityCls = Class.forName("net.openhft.affinity.impl.NullAffinity", true, cl);
            Object expected = nullAffinityCls.getEnumConstants()[0];
            assertSame(expected, impl);
        } finally {
            verField.set(null, originalVer);
        }
    }
}
