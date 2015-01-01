/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.URLClassPath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

enum BootClassPath {
    INSTANCE;

    private final URLClassPath bootClassPath = new URLClassPath(getBootClassPathURLs());

    public final boolean has(String binaryClassName) {
        String resourceClassName = binaryClassName.replace('.', '/').concat(".class");
        return bootClassPath.getResource(resourceClassName, false) != null;
    }

    private URL[] getBootClassPathURLs() {
        Logger LOGGER = LoggerFactory.getLogger(BootClassPath.class);
        try {
            String bootClassPath = System.getProperty("sun.boot.class.path");
            LOGGER.trace("Boot class-path is: {}",bootClassPath);

            String pathSeparator = System.getProperty("path.separator");
            LOGGER.trace("Path separator is: '{}'", pathSeparator);

            String[] pathElements = bootClassPath.split(pathSeparator);
            URL[] pathURLs = new URL[pathElements.length];
            for (int i = 0; i < pathElements.length; i++) {
                pathURLs[i] = new File(pathElements[i]).toURI().toURL();
            }

            return pathURLs;
        } catch (MalformedURLException e) {
            LOGGER.warn("Parsing the boot class-path failed! Reason: {}", e.getMessage());
            return new URL[0];
        }
    }
}
