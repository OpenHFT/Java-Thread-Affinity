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

package net.openhft.affinity.osgi;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;

public class OSGiTestBase {
    
    public static Option workspaceBundle(String projectName) {
        String baseDir = System.getProperty("main.basedir");
        String bundleDir = null;

        bundleDir = String.format("%s/%s/target/classes",baseDir,projectName);
        if(new File(bundleDir).exists()) {
            return CoreOptions.bundle(String.format("reference:file:%s", bundleDir));
        }

        bundleDir = String.format("%s/../%s/target/classes",baseDir,projectName);
        if(new File(bundleDir).exists()) {
            return CoreOptions.bundle(String.format("reference:file:%s", bundleDir));
        }

        return null;
    }
    
    public static MavenArtifactProvisionOption mavenBundleAsInProject(final String groupId,final String artifactId) {
        return CoreOptions.mavenBundle().groupId(groupId).artifactId(artifactId).versionAsInProject();
    }
    
    public static Bundle findBundle(BundleContext context, String symbolicName) {
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

