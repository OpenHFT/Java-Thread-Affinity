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

package software.chronicle.enterprise.internals.impl;

import net.openhft.affinity.IAffinity;

import java.util.BitSet;

public enum NativeAffinity implements IAffinity {
    INSTANCE;

    public static final boolean LOADED;

    static {
        LOADED = loadAffinityNativeLibrary();
    }

    private native static byte[] getAffinity0();

    private native static void setAffinity0(byte[] affinity);

    private native static int getCpu0();

    private native static int getProcessId0();

    private native static int getThreadId0();

    private native static long rdtsc0();

    private static boolean loadAffinityNativeLibrary() {
        try {
            System.loadLibrary("CEInternals");
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    @Override
    public BitSet getAffinity() {
        final byte[] buff = getAffinity0();
        if (buff == null) {
            return null;
        }
        return BitSet.valueOf(buff);
    }

    @Override
    public void setAffinity(BitSet affinity) {
        setAffinity0(affinity.toByteArray());
    }

    @Override
    public int getCpu() {
        return getCpu0();
    }

    @Override
    public int getProcessId() {
        return getProcessId0();
    }

    @Override
    public int getThreadId() {
        return getThreadId0();
    }
}
