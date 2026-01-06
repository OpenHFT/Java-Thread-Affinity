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

/**
 * @author peter.lawrey
 */
public class VanillaCpuLayoutTest extends BaseAffinitySupport {

    @Test
    public void testFromCpuInfoI7() throws IOException {
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
                    "i7.cpuinfo mapping"
            );
        }
    }

    @Test
    public void testFromCpuInfoOthers() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("amd64.dual.core.cpuinfo")) {
            assertNotNull(is, "test resource: amd64.dual.core.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n",
                    vcl.toString(),
                    "amd64.dual.core.cpuinfo mapping"
            );
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("core.duo.cpuinfo")) {
            assertNotNull(is, "test resource: core.duo.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n",
                    vcl.toString(),
                    "core.duo.cpuinfo mapping"
            );
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("amd64.quad.core.cpuinfo")) {
            assertNotNull(is, "test resource: amd64.quad.core.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                            "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                            "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n",
                    vcl.toString(),
                    "amd64.quad.core.cpuinfo mapping"
            );
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dual.xeon.cpuinfo")) {
            assertNotNull(is, "test resource: dual.xeon.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=0, threadId=1}\n" +
                            "2: CpuInfo{socketId=3, coreId=3, threadId=0}\n" +
                            "3: CpuInfo{socketId=3, coreId=3, threadId=1}\n",
                    vcl.toString(),
                    "dual.xeon.cpuinfo mapping"
            );
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("i3.cpuinfo")) {
            assertNotNull(is, "test resource: i3.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                            "2: CpuInfo{socketId=0, coreId=0, threadId=1}\n" +
                            "3: CpuInfo{socketId=0, coreId=2, threadId=1}\n",
                    vcl.toString(),
                    "i3.cpuinfo mapping"
            );
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("q6600.noht.cpuinfo")) {
            assertNotNull(is, "test resource: q6600.noht.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                            "2: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                            "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n",
                    vcl.toString(),
                    "q6600.noht.cpuinfo mapping"
            );
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dual.E5405.cpuinfo")) {
            assertNotNull(is, "test resource: dual.E5405.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                            "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                            "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n" +
                            "4: CpuInfo{socketId=1, coreId=4, threadId=0}\n" +
                            "5: CpuInfo{socketId=1, coreId=5, threadId=0}\n" +
                            "6: CpuInfo{socketId=1, coreId=6, threadId=0}\n" +
                            "7: CpuInfo{socketId=1, coreId=7, threadId=0}\n",
                    vcl.toString(),
                    "dual.E5405.cpuinfo mapping"
            );
        }
    }

    @Test
    public void testNoIDs() throws IOException {
        try (InputStream noids = getClass().getClassLoader().getResourceAsStream("q6600.vm.cpuinfo")) {
            assertNotNull(noids, "test resource: q6600.vm.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(noids);
            assertEquals(
                    "0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                            "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                            "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                            "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n",
                    vcl.toString(),
                    "q6600.vm.cpuinfo mapping"
            );
        }
    }

    @Test
    public void testFromProperties() throws IOException {
        try (InputStream i7 = getClass().getClassLoader().getResourceAsStream("i7.properties")) {
            assertNotNull(i7, "test resource: i7.properties");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(i7);
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
                    "i7.properties mapping"
            );
        }
    }
}
