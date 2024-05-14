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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class BaseAffinityTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String originalTmpDir;

    @Before
    public void setTmpDirectory() {
        originalTmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", folder.getRoot().getAbsolutePath());
    }

    @After
    public void restoreTmpDirectoryAndReleaseAllLocks() {
        // don't leave any locks locked
        for (int i : AffinityLock.PROCESSORS) {
            LockCheck.releaseLock(i);
        }
        System.setProperty("java.io.tmpdir", originalTmpDir);
    }
}
