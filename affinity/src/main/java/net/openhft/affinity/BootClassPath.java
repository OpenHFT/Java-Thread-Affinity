/*
 * Copyright 2016-2020 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.affinity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The BootClassPath enum provides functionality to inspect and query the boot classpath for resources.
 */
enum BootClassPath {
    INSTANCE;

    // Set of resources on the boot classpath
    final Set<String> bootClassPathResources = Collections.unmodifiableSet(getResourcesOnBootClasspath());

    /**
     * Retrieves all resources on the boot classpath.
     *
     * @return a Set of resource names on the boot classpath
     */
    private static Set<String> getResourcesOnBootClasspath() {
        final Logger logger = LoggerFactory.getLogger(BootClassPath.class);
        final Set<String> resources = new HashSet<>();
        final String bootClassPath = System.getProperty("sun.boot.class.path", "");
        logger.trace("Boot class-path is: {}", bootClassPath);

        final String pathSeparator = System.getProperty("path.separator");
        logger.trace("Path separator is: '{}'", pathSeparator);

        final String[] pathElements = bootClassPath.split(pathSeparator);

        // Iterate over each path element in the boot classpath
        for (final String pathElement : pathElements) {
            resources.addAll(findResources(Paths.get(pathElement), logger));
        }

        return resources;
    }

    /**
     * Finds resources at the specified path.
     *
     * @param path   the path to search for resources
     * @param logger the logger for logging messages
     * @return a Set of resource names found at the specified path
     */
    static Set<String> findResources(final Path path, final Logger logger) {
        if (!Files.exists(path)) {
            return Collections.emptySet();
        }

        if (Files.isDirectory(path)) {
            return findResourcesInDirectory(path, logger);
        }

        return findResourcesInJar(path, logger);
    }

    /**
     * Finds resources in a JAR file at the specified path.
     *
     * @param path   the path to the JAR file
     * @param logger the logger for logging messages
     * @return a Set of resource names found in the JAR file
     */
    static Set<String> findResourcesInJar(final Path path, final Logger logger) {
        final Set<String> jarResources = new HashSet<>();
        try (final JarFile jarFile = new JarFile(path.toFile())) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    jarResources.add(jarEntry.getName());
                }
            }
        } catch (IOException e) {
            logger.warn("Not a jar file: {}", path);
        }

        return jarResources;
    }

    /**
     * Finds resources in a directory at the specified path.
     *
     * @param path   the path to the directory
     * @param logger the logger for logging messages
     * @return a Set of resource names found in the directory
     */
    static Set<String> findResourcesInDirectory(final Path path, final Logger logger) {
        final Set<String> dirResources = new HashSet<>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {
                        dirResources.add(path.relativize(file).toString());
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            logger.warn("Error walking dir: " + path, e);
        }

        return dirResources;
    }

    /**
     * Checks if the specified class is present on the boot classpath.
     *
     * @param binaryClassName the binary name of the class to check
     * @return true if the class is present on the boot classpath, false otherwise
     */
    public final boolean has(String binaryClassName) {
        final String resourceClassName = binaryClassName.replace('.', '/').concat(".class");
        return bootClassPathResources.contains(resourceClassName);
    }
}
