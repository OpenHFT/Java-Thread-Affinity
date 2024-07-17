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

import junit.framework.TestCase;
import org.junit.Assert;

public class VersionHelperTest extends TestCase {

    public void testVersionHelperStringConstructor() {
        VersionHelper version = new VersionHelper("1.2.3");
        assertEquals(1, version.major);
        assertEquals(2, version.minor);
        assertEquals(3, version.release);

        version = new VersionHelper("1.2");
        assertEquals(1, version.major);
        assertEquals(2, version.minor);
        assertEquals(0, version.release);

        version = new VersionHelper("1");
        assertEquals(1, version.major);
        assertEquals(0, version.minor);
        assertEquals(0, version.release);

        version = new VersionHelper("");
        assertEquals(0, version.major);
        assertEquals(0, version.minor);
        assertEquals(0, version.release);
    }

    public void testVersionHelperIntConstructor() {
        VersionHelper version = new VersionHelper(1, 2, 3);
        assertEquals(1, version.major);
        assertEquals(2, version.minor);
        assertEquals(3, version.release);
    }

    public void testToString() {
        VersionHelper version = new VersionHelper(1, 2, 3);
        assertEquals("1.2.3", version.toString());
    }

    public void testEqualsAndHashCode() {
        VersionHelper version1 = new VersionHelper(1, 2, 3);
        VersionHelper version2 = new VersionHelper(1, 2, 3);
        VersionHelper version3 = new VersionHelper(1, 2, 4);

        assertEquals(version1, version2);
        assertNotSame(version1, version3);
        assertEquals(version1.hashCode(), version2.hashCode());
        assertNotSame(version1.hashCode(), version3.hashCode());
    }

    public void testMajorMinorEquals() {
        VersionHelper version1 = new VersionHelper(1, 2, 3);
        VersionHelper version2 = new VersionHelper(1, 2, 4);
        VersionHelper version3 = new VersionHelper(1, 3, 0);

        assertTrue(version1.majorMinorEquals(version2));
        assertFalse(version1.majorMinorEquals(version3));
    }

    public void testIsSameOrNewer() {
        VersionHelper version1 = new VersionHelper(1, 2, 3);
        VersionHelper version2 = new VersionHelper(1, 2, 2);
        VersionHelper version3 = new VersionHelper(1, 2, 3);
        VersionHelper version4 = new VersionHelper(1, 2, 4);
        VersionHelper version5 = new VersionHelper(1, 3, 0);

        assertTrue(version1.isSameOrNewer(version2));
        assertTrue(version1.isSameOrNewer(version3));
        assertFalse(version1.isSameOrNewer(version4));
        assertFalse(version1.isSameOrNewer(version5));
    }


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
