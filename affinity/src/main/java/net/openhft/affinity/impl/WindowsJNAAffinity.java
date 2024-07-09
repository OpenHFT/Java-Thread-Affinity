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

import com.sun.jna.*;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.LongByReference;
import net.openhft.affinity.IAffinity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

/**
 * Implementation of {@link net.openhft.affinity.IAffinity} based on JNA calls to
 * SetThreadAffinityMask and GetProcessAffinityMask from Windows 'kernel32' library.
 * Applicable for most Windows platforms.
 *
 * This class provides methods to get and set the CPU affinity of the current process and thread.
 */
public enum WindowsJNAAffinity implements IAffinity {
    INSTANCE;

    public static final boolean LOADED;
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsJNAAffinity.class);
    private static final ThreadLocal<BitSet> currentAffinity = new ThreadLocal<>();

    static {
        boolean loaded = false;
        try {
            INSTANCE.getAffinity();
            loaded = true;
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("Unable to load JNA library", e);
        }
        LOADED = loaded;
    }

    private final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<>();

    /**
     * Retrieves the current CPU affinity of the process.
     *
     * @return a BitSet representing the CPU affinity
     */
    @Override
    public BitSet getAffinity() {
        BitSet bitSet = currentAffinity.get();
        if (bitSet != null)
            return bitSet;
        BitSet longs = getAffinity0();
        return longs != null ? longs : new BitSet();
    }

    /**
     * Sets the CPU affinity for the current thread.
     *
     * @param affinity the BitSet representing the desired CPU affinity
     */
    @Override
    public void setAffinity(final BitSet affinity) {
        final CLibrary lib = CLibrary.INSTANCE;

        WinDef.DWORD aff;
        long[] longs = affinity.toLongArray();
        switch (longs.length) {
            case 0:
                aff = new WinDef.DWORD(0);
                break;
            case 1:
                aff = new WinDef.DWORD(longs[0]);
                break;
            default:
                throw new IllegalArgumentException("Windows API does not support more than 64 CPUs for thread affinity");
        }

        int pid = getTid();
        try {
            lib.SetThreadAffinityMask(handle(pid), aff);
        } catch (LastErrorException e) {
            throw new IllegalStateException("SetThreadAffinityMask((" + pid + ") , &(" + affinity + ") ) errorNo=" + e.getErrorCode(), e);
        }
        BitSet affinity2 = getAffinity0();
        if (!affinity2.equals(affinity)) {
            LoggerFactory.getLogger(WindowsJNAAffinity.class).warn("Tried to set affinity to " + affinity + " but was " + affinity2 + " you may have insufficient access rights");
        }
        currentAffinity.set((BitSet) affinity.clone());
    }

    /**
     * Retrieves the current CPU affinity of the process using JNA.
     *
     * @return a BitSet representing the CPU affinity, or null if an error occurs
     */
    @Nullable
    private BitSet getAffinity0() {
        final CLibrary lib = CLibrary.INSTANCE;
        final LongByReference cpuset1 = new LongByReference(0);
        final LongByReference cpuset2 = new LongByReference(0);
        try {

            final int ret = lib.GetProcessAffinityMask(handle(-1), cpuset1, cpuset2);
            // Successful result is positive, according to the docs
            // https://msdn.microsoft.com/en-us/library/windows/desktop/ms683213%28v=vs.85%29.aspx
            if (ret <= 0) {
                throw new IllegalStateException("GetProcessAffinityMask(( -1 ), &(" + cpuset1 + "), &(" + cpuset2 + ") ) return " + ret);
            }

            long[] longs = new long[1];
            longs[0] = cpuset1.getValue();
            return BitSet.valueOf(longs);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Converts the process ID to a HANDLE.
     *
     * @param pid the process ID
     * @return the HANDLE representing the process
     */
    private WinNT.HANDLE handle(int pid) {
        return new WinNT.HANDLE(new Pointer(pid));
    }

    /**
     * Retrieves the current thread ID.
     *
     * @return the current thread ID
     */
    public int getTid() {
        final CLibrary lib = CLibrary.INSTANCE;

        try {
            return lib.GetCurrentThread();
        } catch (LastErrorException e) {
            throw new IllegalStateException("GetCurrentThread( ) errorNo=" + e.getErrorCode(), e);
        }
    }

    /**
     * Retrieves the current CPU number.
     *
     * @return the current CPU number, or -1 if not applicable
     */
    @Override
    public int getCpu() {
        return -1;
    }

    /**
     * Retrieves the process ID of the current process.
     *
     * @return the process ID
     */
    @Override
    public int getProcessId() {
        return Kernel32.INSTANCE.GetCurrentProcessId();
    }

    /**
     * Retrieves the thread ID of the current thread.
     *
     * @return the thread ID
     */
    @Override
    public int getThreadId() {
        Integer tid = THREAD_ID.get();
        if (tid == null)
            THREAD_ID.set(tid = Kernel32.INSTANCE.GetCurrentThreadId());
        return tid;
    }

    /**
     * Interface for accessing Windows 'kernel32' library functions using JNA.
     */
    private interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("kernel32", CLibrary.class);

        int GetProcessAffinityMask(final WinNT.HANDLE pid, final PointerType lpProcessAffinityMask, final PointerType lpSystemAffinityMask) throws LastErrorException;

        void SetThreadAffinityMask(final WinNT.HANDLE pid, final WinDef.DWORD lpProcessAffinityMask) throws LastErrorException;

        int GetCurrentThread() throws LastErrorException;
    }
}
