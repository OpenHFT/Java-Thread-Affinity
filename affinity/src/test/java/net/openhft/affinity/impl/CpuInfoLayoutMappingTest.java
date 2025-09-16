/*
 * Copyright 2016-2025 chronicle.software
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

