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

/**
 * Implementation of {@link IAffinity} using native methods.
 * This class provides methods to get and set CPU affinity, get CPU ID, process ID, and thread ID,
 * and read the timestamp counter using native system calls.
 */
public enum NativeAffinity implements IAffinity {
    INSTANCE;

    // Flag indicating if the native library was successfully loaded
    public static final boolean LOADED;

    static {
        LOADED = loadAffinityNativeLibrary(); // Load the native library
    }

    /**
     * Native method to get the CPU affinity.
     *
     * @return a byte array representing the CPU affinity
     */
    private native static byte[] getAffinity0();

    /**
     * Native method to set the CPU affinity.
     *
     * @param affinity a byte array representing the desired CPU affinity
     */
    private native static void setAffinity0(byte[] affinity);

    /**
     * Native method to get the current CPU ID.
     *
     * @return the current CPU ID
     */
    private native static int getCpu0();

    /**
     * Native method to get the process ID.
     *
     * @return the process ID
     */
    private native static int getProcessId0();

    /**
     * Native method to get the thread ID.
     *
     * @return the thread ID
     */
    private native static int getThreadId0();

    /**
     * Native method to read the timestamp counter.
     *
     * @return the current value of the timestamp counter
     */
    private native static long rdtsc0();

    /**
     * Loads the native affinity library.
     *
     * @return true if the library was successfully loaded, false otherwise
     */
    private static boolean loadAffinityNativeLibrary() {
        try {
            System.loadLibrary("CEInternals"); // Load the native library
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false; // Return false if the library cannot be loaded
        }
    }

    /**
     * Retrieves the current CPU affinity of the process.
     *
     * @return a BitSet representing the CPU affinity, or null if an error occurs
     */
    @Override
    public BitSet getAffinity() {
        final byte[] buff = getAffinity0();
        if (buff == null) {
            return null;
        }
        return BitSet.valueOf(buff); // Convert byte array to BitSet
    }

    /**
     * Sets the CPU affinity for the current process.
     *
     * @param affinity the BitSet representing the desired CPU affinity
     */
    @Override
    public void setAffinity(BitSet affinity) {
        setAffinity0(affinity.toByteArray()); // Convert BitSet to byte array and set affinity
    }

    /**
     * Retrieves the current CPU ID.
     *
     * @return the current CPU ID
     */
    @Override
    public int getCpu() {
        return getCpu0();
    }

    /**
     * Retrieves the process ID of the current process.
     *
     * @return the process ID
     */
    @Override
    public int getProcessId() {
        return getProcessId0();
    }

    /**
     * Retrieves the thread ID of the current thread.
     *
     * @return the thread ID
     */
    @Override
    public int getThreadId() {
        return getThreadId0();
    }
}
