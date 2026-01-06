/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.osgi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.*;

@Disabled("Fails with current Felix resolver (NoSuchMethodError: ResolveContext.onCancel); skip until updated")
public class OSGiBundleTest extends net.openhft.affinity.osgi.OSGiTestBase {
    @Inject
    private BundleContext context;

    @Configuration
    public Option[] config() {
        return options(
                systemProperty("org.osgi.framework.storage.clean").value("true"),
                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
                mavenBundleAsInProject("org.slf4j", "slf4j-api"),
                mavenBundleAsInProject("org.slf4j", "slf4j-simple").noStart(),
                mavenBundleAsInProject("net.openhft", "affinity"),
                workspaceBundle("affinity-test"),
                junitBundles(),
                systemPackage("sun.misc"),
                systemPackage("sun.nio.ch"),
                systemPackage("com.sun.jna"),
                systemPackage("com.sun.jna.ptr"),
                cleanCaches()
        );
    }

    @Test
    public void checkInject() {
        assertNotNull(context, "BundleContext injected");
    }

    @Test
    public void checkBundleState() {
        final Bundle bundle = findBundle(context, "net.openhft.affinity");
        assertNotNull(bundle, "bundle found: net.openhft.affinity");
        assertEquals(Bundle.ACTIVE, bundle.getState(), "bundle is active");
    }

    @Test
    public void checkBundleExports() {
        final Bundle bundle = findBundle(context, "net.openhft.affinity");
        assertNotNull(bundle, "bundle found: net.openhft.affinity");

        final String exports = bundle.getHeaders().get("Export-Package");
        final String[] packages = exports.split(",");

        assertTrue(packages.length >= 2, "at least two exported packages");
        assertTrue(
                packages[0].startsWith("net.openhft.affinity;") || packages[0].startsWith("net.openhft.affinity.impl;"),
                "export[0] is affinity package"
        );
        assertTrue(
                packages[1].startsWith("net.openhft.affinity;") || packages[1].startsWith("net.openhft.affinity.impl;"),
                "export[1] is affinity package"
        );
    }
}
