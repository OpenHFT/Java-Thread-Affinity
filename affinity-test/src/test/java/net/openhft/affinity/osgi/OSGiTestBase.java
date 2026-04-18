/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.osgi;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;

class OSGiTestBase {

    static Option workspaceBundle(String projectName) {
        String baseDir = System.getProperty("main.basedir");
        String bundleDir;

        bundleDir = String.format("%s/%s/target/classes", baseDir, projectName);
        if (new File(bundleDir).exists()) {
            return CoreOptions.bundle(String.format("reference:file:%s", bundleDir));
        }

        bundleDir = String.format("%s/../%s/target/classes", baseDir, projectName);
        if (new File(bundleDir).exists()) {
            return CoreOptions.bundle(String.format("reference:file:%s", bundleDir));
        }

        return null;
    }

    static MavenArtifactProvisionOption mavenBundleAsInProject(final String groupId, final String artifactId) {
        return CoreOptions.mavenBundle().groupId(groupId).artifactId(artifactId).versionAsInProject();
    }

    static Bundle findBundle(BundleContext context, String symbolicName) {
        Bundle[] bundles = context.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle != null) {
                if (bundle.getSymbolicName().equals(symbolicName)) {
                    return bundle;
                }
            }
        }

        return null;
    }
}
