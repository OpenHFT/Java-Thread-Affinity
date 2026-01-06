/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinitySupport;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VanillaCpuLayoutPropertiesParseTest extends BaseAffinitySupport {

    @Test
    public void testCountsI7() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("i7.properties")) {
            assertNotNull(is, "test resource: i7.properties");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(is);
            assertEquals(8, vcl.cpus(), "i7 cpus");
            assertEquals(1, vcl.sockets(), "i7 sockets");
            assertEquals(4, vcl.coresPerSocket(), "i7 cores per socket");
            assertEquals(2, vcl.threadsPerCore(), "i7 threads per core");
        }
    }

    @Test
    public void testCountsDualXeon() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dual.xeon.properties")) {
            assertNotNull(is, "test resource: dual.xeon.properties");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(is);
            assertEquals(4, vcl.cpus(), "dual xeon cpus");
            assertEquals(2, vcl.sockets(), "dual xeon sockets");
            assertEquals(1, vcl.coresPerSocket(), "dual xeon cores per socket");
            assertEquals(2, vcl.threadsPerCore(), "dual xeon threads per core");
        }
    }

    @Test
    public void testCountsDualE5405() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("dual.E5405.properties")) {
            assertNotNull(is, "test resource: dual.E5405.properties");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(is);
            assertEquals(8, vcl.cpus(), "dual E5405 cpus");
            assertEquals(2, vcl.sockets(), "dual E5405 sockets");
            assertEquals(4, vcl.coresPerSocket(), "dual E5405 cores per socket");
            assertEquals(1, vcl.threadsPerCore(), "dual E5405 threads per core");
        }
    }

    @Test
    public void testCountsI3() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("i3.properties")) {
            assertNotNull(is, "test resource: i3.properties");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(is);
            assertEquals(4, vcl.cpus(), "i3 cpus");
            assertEquals(1, vcl.sockets(), "i3 sockets");
            assertEquals(2, vcl.coresPerSocket(), "i3 cores per socket");
            assertEquals(2, vcl.threadsPerCore(), "i3 threads per core");
        }
    }
}
