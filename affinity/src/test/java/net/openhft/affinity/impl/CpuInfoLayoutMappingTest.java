/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinitySupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CpuInfoLayoutMappingTest extends BaseAffinitySupport {

    @Test
    public void verifyI7CpuInfoMapping() throws IOException {
        try (InputStream i7 = getClass().getClassLoader().getResourceAsStream("i7.cpuinfo")) {
            assertNotNull(i7, "test resource: i7.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(i7);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                            "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                            "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n" +
                            "4: CpuInfo{socketId=0, coreId=0, threadId=1}\n" +
                            "5: CpuInfo{socketId=0, coreId=1, threadId=1}\n" +
                            "6: CpuInfo{socketId=0, coreId=2, threadId=1}\n" +
                            "7: CpuInfo{socketId=0, coreId=3, threadId=1}\n",
                    vcl.toString(),
                    "cpuinfo mapping"
            );
        }
    }
}
