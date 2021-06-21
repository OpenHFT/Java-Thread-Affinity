/*
 * Copyright 2016-2020 chronicle.software
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

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author peter.lawrey
 */
public class VanillaCpuLayoutTest {

    @Test
    public void testFromCpuInfoI7() throws IOException {
        final InputStream i7 = getClass().getClassLoader().getResourceAsStream("i7.cpuinfo");
        VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(i7);
        assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n" +
                "4: CpuInfo{socketId=0, coreId=0, threadId=1}\n" +
                "5: CpuInfo{socketId=0, coreId=1, threadId=1}\n" +
                "6: CpuInfo{socketId=0, coreId=2, threadId=1}\n" +
                "7: CpuInfo{socketId=0, coreId=3, threadId=1}\n", vcl.toString());
    }

    @Test
    public void testFromCpuInfoOthers() throws IOException {
        {
            final InputStream is = getClass().getClassLoader().getResourceAsStream("amd64.dual.core.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                    "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n", vcl.toString());
        }
        {
            final InputStream is = getClass().getClassLoader().getResourceAsStream("core.duo.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                    "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n", vcl.toString());
        }
        {
            final InputStream is = getClass().getClassLoader().getResourceAsStream("amd64.quad.core.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                    "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                    "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                    "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n", vcl.toString());
        }
        {
            final InputStream is = getClass().getClassLoader().getResourceAsStream("dual.xeon.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                    "1: CpuInfo{socketId=0, coreId=0, threadId=1}\n" +
                    "2: CpuInfo{socketId=3, coreId=3, threadId=0}\n" +
                    "3: CpuInfo{socketId=3, coreId=3, threadId=1}\n", vcl.toString());
        }
        {
            final InputStream is = getClass().getClassLoader().getResourceAsStream("i3.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                    "1: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                    "2: CpuInfo{socketId=0, coreId=0, threadId=1}\n" +
                    "3: CpuInfo{socketId=0, coreId=2, threadId=1}\n", vcl.toString());
        }
        {
            final InputStream is = getClass().getClassLoader().getResourceAsStream("q6600.noht.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                    "1: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                    "2: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                    "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n", vcl.toString());
        }
        {
            final InputStream is = getClass().getClassLoader().getResourceAsStream("dual.E5405.cpuinfo");
            VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                    "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                    "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                    "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n" +
                    "4: CpuInfo{socketId=1, coreId=4, threadId=0}\n" +
                    "5: CpuInfo{socketId=1, coreId=5, threadId=0}\n" +
                    "6: CpuInfo{socketId=1, coreId=6, threadId=0}\n" +
                    "7: CpuInfo{socketId=1, coreId=7, threadId=0}\n", vcl.toString());
        }
    }

    @Test
    public void testNoIDs() throws IOException {
        final InputStream noids = getClass().getClassLoader().getResourceAsStream("q6600.vm.cpuinfo");
        VanillaCpuLayout vcl = VanillaCpuLayout.fromCpuInfo(noids);
        assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n", vcl.toString());
    }

    @Test
    public void testFromProperties() throws IOException {
        final InputStream i7 = getClass().getClassLoader().getResourceAsStream("i7.properties");
        VanillaCpuLayout vcl = VanillaCpuLayout.fromProperties(i7);
        assertEquals("0: CpuInfo{socketId=0, coreId=0, threadId=0}\n" +
                "1: CpuInfo{socketId=0, coreId=1, threadId=0}\n" +
                "2: CpuInfo{socketId=0, coreId=2, threadId=0}\n" +
                "3: CpuInfo{socketId=0, coreId=3, threadId=0}\n" +
                "4: CpuInfo{socketId=0, coreId=0, threadId=1}\n" +
                "5: CpuInfo{socketId=0, coreId=1, threadId=1}\n" +
                "6: CpuInfo{socketId=0, coreId=2, threadId=1}\n" +
                "7: CpuInfo{socketId=0, coreId=3, threadId=1}\n", vcl.toString());
    }
}
