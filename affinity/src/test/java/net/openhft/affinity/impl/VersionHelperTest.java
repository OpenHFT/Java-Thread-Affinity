/*
 * Copyright 20127chronicle.software
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

package net.openhft.affinity.impl;

import org.junit.Assert;
import org.junit.Test;

public class VersionHelperTest {

    @Test
    public void isSameOrNewerTest() {
        final VersionHelper v0 = new VersionHelper(0, 0, 0);
        final VersionHelper v2_6 = new VersionHelper(2, 6, 0);
        final VersionHelper v4_1 = new VersionHelper(4, 1, 1);
        final VersionHelper v4_9 = new VersionHelper(4, 9, 0);
        final VersionHelper v9_9 = new VersionHelper(9, 9, 9);

        VersionHelper[] versions = new VersionHelper[]{v0, v2_6, v4_1, v4_9, v9_9};

        for (int i = 0; i < versions.length; i++) {
            for (int j = 0; j < versions.length; j++) {
                Assert.assertEquals(String.format("expected %s.isSameOrNewer(%s) to be %b", versions[i], versions[j], i >= j),
                        i >= j, versions[i].isSameOrNewer(versions[j]));
            }
        }
    }
}
