/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.net.URI;
import java.util.Set;
import java.util.jar.*;
import static org.junit.jupiter.api.Assertions.*;

class BootResourceTraversalTest {
    @TempDir Path directory;

    @SuppressWarnings("unchecked")
    private Set<String> resources(String method, Path path) throws Exception {
        Method find = BootClassPath.class.getDeclaredMethod(method, Path.class, Logger.class);
        find.setAccessible(true);
        return (Set<String>) find.invoke(null, path, LoggerFactory.getLogger(getClass()));
    }

    @Test
    void directoryAndJarUseTheSameBinaryResourceNames() throws Exception {
        Files.createDirectories(directory.resolve("example/nested"));
        Files.write(directory.resolve("example/nested/Value.class"), new byte[]{1});
        Files.write(directory.resolve("example/nested/notes.txt"), new byte[]{1});
        Path jar = directory.resolve("classes.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("example/nested/Value.class"));
            out.write(1); out.closeEntry();
        }
        Set<String> fromJar = resources("findResourcesInJar", jar);
        assertEquals(fromJar, resources("findResourcesInDirectory", directory));
        Files.delete(jar); // also checks the owned JarFile was closed on Windows
    }

    @Test
    void sharedRuntimeFileSystemRemainsAvailable() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse("1.8".equals(System.getProperty("java.specification.version")));
        assertTrue(BootClassPath.INSTANCE.has("java.lang.String"));
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        assertTrue(fs.isOpen());
        assertTrue(Files.exists(fs.getPath("/modules/java.base/java/lang/String.class")));
    }
}
