/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
