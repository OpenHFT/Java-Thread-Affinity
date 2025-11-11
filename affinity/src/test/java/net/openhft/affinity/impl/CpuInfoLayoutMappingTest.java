/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinityTest;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class CpuInfoLayoutMappingTest extends BaseAffinityTest {

    @Test
    public void verifyI7CpuInfoMapping() throws IOException {
        final InputStream i7 = getClass().getClassLoader().getResourceAsStream("i7.cpuinfo");
        VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(i7);
        assertEquals("" +
                        "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                        "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                        "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                        "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n" +
                        "4: CpuInfo{socketId=0, coreId=0, threadId=1}\n" +
                        "5: CpuInfo{socketId=0, coreId=1, threadId=1}\n" +
                        "6: CpuInfo{socketId=0, coreId=2, threadId=1}\n" +
                        "7: CpuInfo{socketId=0, coreId=3, threadId=1}\n",
                vcl.toString());
    }
}

