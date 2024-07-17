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

import junit.framework.TestCase;
import net.openhft.affinity.CpuLayout;

import java.util.ArrayList;
import java.util.List;

public class VanillaCpuLayoutTest extends TestCase {

    public void testVanillaCpuLayoutCreation() {
        List<VanillaCpuLayout.CpuInfo> cpuDetails = new ArrayList<>();
        cpuDetails.add(new VanillaCpuLayout.CpuInfo(0, 0, 0));
        cpuDetails.add(new VanillaCpuLayout.CpuInfo(0, 0, 1));
        cpuDetails.add(new VanillaCpuLayout.CpuInfo(0, 1, 0));
        cpuDetails.add(new VanillaCpuLayout.CpuInfo(0, 1, 1));
        cpuDetails.add(new VanillaCpuLayout.CpuInfo(1, 0, 0));
        cpuDetails.add(new VanillaCpuLayout.CpuInfo(1, 0, 1));
        cpuDetails.add(new VanillaCpuLayout.CpuInfo(1, 1, 0));
        cpuDetails.add(new VanillaCpuLayout.CpuInfo(1, 1, 1));

        VanillaCpuLayout layout = new VanillaCpuLayout(cpuDetails);

        assertEquals(2, layout.sockets());
        assertEquals(2, layout.coresPerSocket());
        assertEquals(2, layout.threadsPerCore());
    }

    public void testEqualsAndHashCode() {
        List<VanillaCpuLayout.CpuInfo> cpuDetails1 = new ArrayList<>();
        cpuDetails1.add(new VanillaCpuLayout.CpuInfo(0, 0, 0));
        cpuDetails1.add(new VanillaCpuLayout.CpuInfo(0, 0, 1));
        cpuDetails1.add(new VanillaCpuLayout.CpuInfo(0, 1, 0));
        cpuDetails1.add(new VanillaCpuLayout.CpuInfo(0, 1, 1));

        List<VanillaCpuLayout.CpuInfo> cpuDetails2 = new ArrayList<>(cpuDetails1);

        VanillaCpuLayout layout1 = new VanillaCpuLayout(cpuDetails1);
        VanillaCpuLayout layout2 = new VanillaCpuLayout(cpuDetails2);

        assertEquals(layout1, layout2);
        assertEquals(layout1.hashCode(), layout2.hashCode());
    }

    public void testCpuInfoEqualsAndHashCode() {
        VanillaCpuLayout.CpuInfo info1 = new VanillaCpuLayout.CpuInfo(0, 0, 0);
        VanillaCpuLayout.CpuInfo info2 = new VanillaCpuLayout.CpuInfo(0, 0, 0);
        VanillaCpuLayout.CpuInfo info3 = new VanillaCpuLayout.CpuInfo(1, 1, 1);

        assertEquals(info1, info2);
        assertNotSame(info1, info3);

        assertEquals(info1.hashCode(), info2.hashCode());
        assertNotSame(info1.hashCode(), info3.hashCode());
    }

    public void testToString() {
        VanillaCpuLayout.CpuInfo info = new VanillaCpuLayout.CpuInfo(0, 1, 2);
        String expected = "CpuInfo{socketId=0, coreId=1, threadId=2}";
        assertEquals(expected, info.toString());
    }

    // Additional tests provided by the user
    public void testCpuInfoEquality() {
        VanillaCpuLayout.CpuInfo cpuInfo1 = new VanillaCpuLayout.CpuInfo(0, 0, 0);
        VanillaCpuLayout.CpuInfo cpuInfo2 = new VanillaCpuLayout.CpuInfo(0, 0, 0);
        VanillaCpuLayout.CpuInfo cpuInfo3 = new VanillaCpuLayout.CpuInfo(1, 1, 1);

        assertEquals(cpuInfo1, cpuInfo2);
        assertNotSame(cpuInfo1, cpuInfo3);
    }

    public void testCpuInfoHashCode() {
        VanillaCpuLayout.CpuInfo cpuInfo1 = new VanillaCpuLayout.CpuInfo(0, 0, 0);
        VanillaCpuLayout.CpuInfo cpuInfo2 = new VanillaCpuLayout.CpuInfo(0, 0, 0);

        assertEquals(cpuInfo1.hashCode(), cpuInfo2.hashCode());
    }

    public void testCpuInfoToString() {
        VanillaCpuLayout.CpuInfo cpuInfo = new VanillaCpuLayout.CpuInfo(0, 1, 2);
        String expected = "CpuInfo{socketId=0, coreId=1, threadId=2}";
        assertEquals(expected, cpuInfo.toString());
    }

    public void testCpuLayoutEquality() {
        List<VanillaCpuLayout.CpuInfo> cpuInfos1 = new ArrayList<>();
        cpuInfos1.add(new VanillaCpuLayout.CpuInfo(0, 0, 0));
        cpuInfos1.add(new VanillaCpuLayout.CpuInfo(0, 1, 1));

        List<VanillaCpuLayout.CpuInfo> cpuInfos2 = new ArrayList<>();
        cpuInfos2.add(new VanillaCpuLayout.CpuInfo(0, 0, 0));
        cpuInfos2.add(new VanillaCpuLayout.CpuInfo(0, 1, 1));

        VanillaCpuLayout cpuLayout1 = new VanillaCpuLayout(cpuInfos1);
        VanillaCpuLayout cpuLayout2 = new VanillaCpuLayout(cpuInfos2);

        assertEquals(cpuLayout1, cpuLayout2);
    }

    public void testCpuLayoutHashCode() {
        List<VanillaCpuLayout.CpuInfo> cpuInfos1 = new ArrayList<>();
        cpuInfos1.add(new VanillaCpuLayout.CpuInfo(0, 0, 0));
        cpuInfos1.add(new VanillaCpuLayout.CpuInfo(0, 1, 1));

        List<VanillaCpuLayout.CpuInfo> cpuInfos2 = new ArrayList<>();
        cpuInfos2.add(new VanillaCpuLayout.CpuInfo(0, 0, 0));
        cpuInfos2.add(new VanillaCpuLayout.CpuInfo(0, 1, 1));

        VanillaCpuLayout cpuLayout1 = new VanillaCpuLayout(cpuInfos1);
        VanillaCpuLayout cpuLayout2 = new VanillaCpuLayout(cpuInfos2);

        assertEquals(cpuLayout1.hashCode(), cpuLayout2.hashCode());
    }
}
