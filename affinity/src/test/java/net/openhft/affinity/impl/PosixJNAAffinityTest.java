/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.IAffinity;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author peter.lawrey
 */
@Disabled("TODO: JNA library is not used, test is flaky")
public class PosixJNAAffinityTest extends AbstractAffinityImplTest {
    @Override
    public IAffinity getImpl() {
        return Affinity.getAffinityImpl();
    }

    @Test
    public void testGettid() {
        assertDoesNotThrow(() -> runThreadIdBenchmark(1 << 24), "gettid benchmark completes");
    }
}
