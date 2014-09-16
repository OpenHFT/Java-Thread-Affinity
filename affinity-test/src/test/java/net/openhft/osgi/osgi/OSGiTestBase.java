/*
 * Copyright 2014 Higher Frequency Trading
 * <p/>
 * http://www.higherfrequencytrading.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.affinity.osgi;

import java.io.File;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;

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

