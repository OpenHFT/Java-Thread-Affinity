//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinityTest;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class VanillaCpuLayoutPropertiesParseTest extends BaseAffinityTest {

    @Test
    public void testCountsI7() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("i7.properties");
        VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(is);
        assertEquals(8, vcl.cpus());
        assertEquals(1, vcl.sockets());
        assertEquals(4, vcl.coresPerSocket());
        assertEquals(2, vcl.threadsPerCore());
    }

    @Test
    public void testCountsDualXeon() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dual.xeon.properties");
        VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(is);
        assertEquals(4, vcl.cpus());
        assertEquals(2, vcl.sockets());
        assertEquals(1, vcl.coresPerSocket());
        assertEquals(2, vcl.threadsPerCore());
    }

    @Test
    public void testCountsDualE5405() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dual.E5405.properties");
        VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(is);
        assertEquals(8, vcl.cpus());
        assertEquals(2, vcl.sockets());
        assertEquals(4, vcl.coresPerSocket());
        assertEquals(1, vcl.threadsPerCore());
    }

    @Test
    public void testCountsI3() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("i3.properties");
        VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(is);
        assertEquals(4, vcl.cpus());
        assertEquals(1, vcl.sockets());
        assertEquals(2, vcl.coresPerSocket());
        assertEquals(2, vcl.threadsPerCore());
    }
}
