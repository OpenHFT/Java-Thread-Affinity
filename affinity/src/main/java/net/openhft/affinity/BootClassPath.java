package net.openhft.affinity;

import sun.misc.URLClassPath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

enum BootClassPath {
    INSTANCE;

    private final Logger logger = Logger.getLogger(BootClassPath.class.getName());

    private final URLClassPath bootClassPath = new URLClassPath(getBootClassPathURLs());

    public final boolean has(String binaryClassName) {
        String resourceClassName = binaryClassName.replace('.', '/').concat(".class");
        return bootClassPath.getResource(resourceClassName, false) != null;
    }

    private URL[] getBootClassPathURLs() {
        try {
            String bootClassPath = System.getProperty("sun.boot.class.path");
            logger.fine("Boot class-path is: " + bootClassPath);

            String pathSeparator = System.getProperty("path.separator");
            logger.fine("Path separator is: '" + pathSeparator + "'");

            String[] pathElements = bootClassPath.split(pathSeparator);
            URL[] pathURLs = new URL[pathElements.length];
            for (int i = 0; i < pathElements.length; i++) {
                pathURLs[i] = new File(pathElements[i]).toURI().toURL();
            }

            return pathURLs;
        } catch (MalformedURLException e) {
            logger.warning("Parsing the boot class-path failed! Reason: " + e.getMessage());
            return new URL[0];
        }
    }
}
