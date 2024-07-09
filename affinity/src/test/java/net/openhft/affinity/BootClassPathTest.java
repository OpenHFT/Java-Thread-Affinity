/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.Assert.*;

public class BootClassPathTest {

    private Logger logger;

    @Before
    public void setUp() {
        logger = LoggerFactory.getLogger(BootClassPathTest.class);
    }

    @Test
    public void testGetResourcesOnBootClasspath() {
        Set<String> resources = BootClassPath.INSTANCE.bootClassPathResources;
        assertNotNull(resources);
    }

    @Test
    public void testHasResourceNotExists() {
        assertFalse(BootClassPath.INSTANCE.has("non.existent.ClassName"));
    }

    @Test
    public void testFindResourcesInDirectory() throws Exception {
        Path dirPath = Files.createTempDirectory("testDir");
        Path classFile = dirPath.resolve("TestClass.class");
        Files.createFile(classFile);

        Set<String> resources = BootClassPath.INSTANCE.findResourcesInDirectory(dirPath, logger);
        assertNotNull(resources);
        assertTrue(resources.contains("TestClass.class"));

        Files.deleteIfExists(classFile);
        Files.deleteIfExists(dirPath);
    }

    @Test
    public void testFindResourcesPathNotExists() {
        Path nonExistentPath = Paths.get("non-existent-path");
        Set<String> resources = BootClassPath.INSTANCE.findResources(nonExistentPath, logger);
        assertTrue(resources.isEmpty());
    }

    @Test
    public void testFindResourcesPathIsDirectory() throws Exception {
        Path dirPath = Files.createTempDirectory("testDir");
        Set<String> resources = BootClassPath.INSTANCE.findResources(dirPath, logger);
        assertNotNull(resources);

        Files.deleteIfExists(dirPath);
    }

    @Test
    public void shouldDetectClassesOnClassPath() {
        if (!System.getProperty("java.version").startsWith("1.8"))
            return;
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Thread"));
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Runtime"));
    }
}
