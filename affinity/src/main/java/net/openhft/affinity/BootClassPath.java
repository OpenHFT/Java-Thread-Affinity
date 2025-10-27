/*
 * Copyright 2016-2025 chronicle.software
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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

enum BootClassPath {
    INSTANCE;

    private final Set<String> bootClassPathResources = Collections.unmodifiableSet(getResourcesOnBootClasspath());

    private static Set<String> getResourcesOnBootClasspath() {
        final Logger logger = LoggerFactory.getLogger(BootClassPath.class);
        final Set<String> resources = new HashSet<>();

        final String bootClassPath = System.getProperty("sun.boot.class.path", "");
        if (!bootClassPath.isEmpty()) {
            logger.trace("Boot class-path is: {}", bootClassPath);

            final String pathSeparator = File.pathSeparator;
            logger.trace("Path separator is: '{}'", pathSeparator);

            final String[] pathElements = bootClassPath.split(pathSeparator);

            for (final String pathElement : pathElements) {
                resources.addAll(findResources(Paths.get(pathElement), logger));
            }
        } else {
            resources.addAll(findResourcesInJrt(logger));
        }

        return resources;
    }

    private static Set<String> findResourcesInJrt(final Logger logger) {
        final Set<String> jrtResources = new HashSet<>();
        try {
            FileSystem fs;
            try {
                fs = FileSystems.getFileSystem(URI.create("jrt:/"));
            } catch (FileSystemNotFoundException | ProviderNotFoundException e) {
                fs = FileSystems.newFileSystem(URI.create("jrt:/"), Collections.emptyMap());
            }
            final Path modules = fs.getPath("/modules");
            Files.walkFileTree(modules, new SimpleFileVisitor<Path>() {
                @Override
                public @NotNull FileVisitResult visitFile(final @NotNull Path file,
                                                          final @NotNull BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {
                        Path relative = modules.relativize(file);
                        if (relative.getNameCount() > 1) {
                            Path classPath = relative.subpath(1, relative.getNameCount());
                            jrtResources.add(classPath.toString());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.warn("Error walking jrt filesystem", e);
        }
        return jrtResources;
    }

    private static Set<String> findResources(final Path path, final Logger logger) {
        if (!Files.exists(path)) {
            return Collections.emptySet();
        }

        if (Files.isDirectory(path)) {
            return findResourcesInDirectory(path, logger);
        }

        return findResourcesInJar(path, logger);
    }

    private static Set<String> findResourcesInJar(final Path path, final Logger logger) {
        final Set<String> jarResources = new HashSet<>();
        try (JarFile jarFile = new JarFile(path.toFile())) {
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

    private static Set<String> findResourcesInDirectory(final Path path, final Logger logger) {
        final Set<String> dirResources = new HashSet<>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public @NotNull FileVisitResult visitFile(final @NotNull Path file, final @NotNull BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {
                        dirResources.add(path.relativize(file).toString());
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            logger.warn("Error walking dir: {}", path, e);
        }

        return dirResources;
    }

    public final boolean has(String binaryClassName) {
        final String resourceClassName = binaryClassName.replace('.', '/').concat(".class");
        return bootClassPathResources.contains(resourceClassName);
    }
}
