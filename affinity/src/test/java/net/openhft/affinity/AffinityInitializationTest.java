/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.LINUX)
public class AffinityInitializationTest extends AffinityTestProcess {
    @Test
    void selectsLinuxJnaInFreshJvm() throws Exception {
        runProbe(AffinityInitializationTest.class, "normal", true);
    }

    @Test
    void fallsBackWhenJnaIsAbsentFromClasspath() throws Exception {
        runProbe(AffinityInitializationTest.class, "absent", false);
    }

    @Test
    void fallsBackWhenJnaNativeLoadingFails() throws Exception {
        // JNA classes remain present, but neither native search path may provide jnidispatch.
        runProbe(AffinityInitializationTest.class, "native-failure", true,
                "-Djna.boot.library.path=" + directory.toAbsolutePath(),
                "-Djna.nosys=true", "-Djna.noclasspath=true");
    }

    @Test
    void fallsBackWhenJnaNativeVersionIsIncompatible() throws Exception {
        // The newer fixture's native ABI is incompatible with the BOM's JNA 5.5 Java classes.
        try (JarFile jar = new JarFile(jnaModuleJar())) {
            String resource = "com/sun/jna/" + com.sun.jna.Platform.RESOURCE_PREFIX + "/libjnidispatch.so";
            JarEntry entry = jar.getJarEntry(resource);
            assertNotNull(entry, resource);
            try (InputStream input = jar.getInputStream(entry)) {
                Files.copy(input, directory.resolve("libjnidispatch.so"));
            }
        }
        String log = runProbe(AffinityInitializationTest.class, "native-failure", true,
                "-Djna.boot.library.path=" + directory.toAbsolutePath(),
                "-Djna.nosys=true", "-Djna.noclasspath=true");
        assertTrue(log.contains("java.lang.Error:"), log);
        assertTrue(log.contains("incompatible JNA native library"), log);
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_9)
    void selectsJnaFromExportedButUnopenedModule() throws Exception {
        runProbe(AffinityInitializationTest.class, "module", false,
                "--module-path=" + jnaModuleJar(), "--add-modules=com.sun.jna");
    }

    private static String jnaModuleJar() {
        return Arrays.stream(testClasspath().split(File.pathSeparator))
                .filter(entry -> new File(entry).getName().startsWith("jna-jpms-"))
                .findFirst().orElseThrow(() -> new AssertionError("Missing JNA module test fixture"));
    }

    @Test
    @SuppressWarnings("removal") // ThreadDeath remains relevant to supported older JDKs.
    void propagatesVmThreadTerminationAndAssertionFailures() throws Exception {
        String[] entries = testClasspath().split(File.pathSeparator);
        URL[] urls = new URL[entries.length];
        for (int i = 0; i < entries.length; i++) {
            urls[i] = new File(entries[i]).toURI().toURL();
        }
        for (Error failure : new Error[]{new OutOfMemoryError("test loading failure"), new ThreadDeath(),
                new AssertionError("test loading failure")}) {
            try (URLClassLoader loader = new URLClassLoader(urls, null) {
                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    if (name.equals("com.sun.jna.Native")) {
                        throw failure;
                    }
                    return super.loadClass(name, resolve);
                }
            }) {
                Error thrown = assertThrows(Error.class,
                        () -> Class.forName("net.openhft.affinity.Affinity", true, loader));
                assertSame(failure, thrown);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String scenario = System.getProperty("affinity.test.scenario");
        ClassLoader loader = AffinityInitializationTest.class.getClassLoader();
        if (scenario.equals("absent")) {
            assertThrows(ClassNotFoundException.class, () -> Class.forName("com.sun.jna.Native", false, loader));
            assertThrows(ClassNotFoundException.class, () -> Class.forName("com.sun.jna.Platform", false, loader));
        } else {
            assertNotNull(Class.forName("com.sun.jna.Native", false, loader));
        }
        if (scenario.equals("normal") || scenario.equals("module")) {
            assertTrue(Affinity.isJNAAvailable());
            assertEquals("net.openhft.affinity.impl.LinuxJNAAffinity", Affinity.getAffinityImpl().getClass().getName());
            assertFalse(Affinity.getAffinity().isEmpty());
            if (scenario.equals("module")) {
                // Reflection here keeps these tests compilable on Java 8; the module run requires Java 9+.
                Object module = Class.class.getMethod("getModule").invoke(Class.forName("com.sun.jna.Native"));
                assertEquals(Boolean.TRUE, module.getClass().getMethod("isNamed").invoke(module));
                assertEquals(Boolean.TRUE, module.getClass().getMethod("isExported", String.class).invoke(module, "com.sun.jna"));
                assertEquals(Boolean.FALSE, module.getClass().getMethod("isOpen", String.class).invoke(module, "com.sun.jna"));
            }
        } else {
            assertFalse(Affinity.isJNAAvailable());
            assertEquals("net.openhft.affinity.impl.NullAffinity", Affinity.getAffinityImpl().getClass().getName());
            assertTrue(Affinity.getAffinity().isEmpty());
            if (scenario.equals("native-failure")) {
                // The real Native class failed initialisation; this is not a mocked availability flag.
                assertThrows(NoClassDefFoundError.class, () -> Class.forName("com.sun.jna.Native", true, loader));
            }
        }
        System.out.println("PASS " + scenario);
    }
}
