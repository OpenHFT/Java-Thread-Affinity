/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinityTest;
import org.junit.Assert;
import org.junit.Test;

public class VersionHelperTest extends BaseAffinityTest {

    @Test
    public void isSameOrNewerTest() {
        final VersionHelper v0 = new VersionHelper(0, 0, 0);
        final VersionHelper v2_6 = new VersionHelper(2, 6, 0);
        final VersionHelper v4_1 = new VersionHelper(4, 1, 1);
        final VersionHelper v4_9 = new VersionHelper(4, 9, 0);
        final VersionHelper v9_9 = new VersionHelper(9, 9, 9);

        VersionHelper[] versions = {v0, v2_6, v4_1, v4_9, v9_9};

        for (int i = 0; i < versions.length; i++) {
            for (int j = 0; j < versions.length; j++) {
                Assert.assertEquals(String.format("expected %s.isSameOrNewer(%s) to be %b", versions[i], versions[j], i >= j),
                        i >= j, versions[i].isSameOrNewer(versions[j]));
            }
        }
    }
}
