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
 *
 */

package net.openhft.affinity.impl;

import com.sun.jna.*;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.LongByReference;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.openhft.affinity.IAffinity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

/**
 * Implementation of {@link net.openhft.affinity.IAffinity} based on JNA call of
 * sched_SetThreadAffinityMask/GetProcessAffinityMask from Windows 'kernel32' library. Applicable for
 * most windows platforms
 * <p> *
 *
 * @author andre.monteiro
 */
public enum WindowsJNAAffinity implements IAffinity {
    INSTANCE;
    public static final boolean LOADED;
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsJNAAffinity.class);
    private static final ThreadLocal<BitSet> currentAffinity = new ThreadLocal<>();
    private static final String STUB_PROPERTY = "chronicle.affinity.stub.windows";
    private static final boolean USE_STUB = Boolean.getBoolean(STUB_PROPERTY);

    private static final class LibraryContainer {
        final CLibrary library;
        final boolean stub;

        LibraryContainer() {
            if (USE_STUB) {
                library = new StubWindowsCLibrary();
                stub = true;
                return;
            }
            CLibrary lib;
            boolean isStub = false;
            try {
                lib = Native.load("kernel32", CLibrary.class);
            } catch (UnsatisfiedLinkError e) {
                LOGGER.warn("Unable to load jna library", e);
                lib = new StubWindowsCLibrary();
                isStub = true;
            }
            library = lib;
            stub = isStub;
        }
    }

    private static final LibraryContainer LIBRARY_CONTAINER = new LibraryContainer();
    private static final CLibrary LIBRARY = LIBRARY_CONTAINER.library;
    private static final boolean LIBRARY_IS_STUB = LIBRARY_CONTAINER.stub;

    static {
        boolean loaded = false;
        if (!LIBRARY_IS_STUB) {
            try {
                INSTANCE.getAffinity();
                loaded = true;
            } catch (UnsatisfiedLinkError e) {
                LOGGER.warn("Unable to load jna library", e);
            }
        }
        LOADED = loaded;
    }

    private final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<>();

    @Override
    public BitSet getAffinity() {
        BitSet bitSet = currentAffinity.get();
        if (bitSet != null)
            return bitSet;
        BitSet longs = getAffinity0();
        return longs != null ? longs : new BitSet();
    }

    @Override
    public void setAffinity(final BitSet affinity) {
        final CLibrary lib = LIBRARY;

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
        assert affinity2 != null;
        if (!affinity2.intersects(affinity)) {
            LoggerFactory.getLogger(WindowsJNAAffinity.class).warn("Tried to set affinity to {} but was {} you may have insufficient access rights", affinity, affinity2);
        }
        currentAffinity.set((BitSet) affinity.clone());
    }

    @Nullable
    private BitSet getAffinity0() {
        final CLibrary lib = LIBRARY;
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

    private WinNT.HANDLE handle(int pid) {
        return new WinNT.HANDLE(new Pointer(pid));
    }

    public int getTid() {
        final CLibrary lib = LIBRARY;

        try {
            return lib.GetCurrentThread();
        } catch (LastErrorException e) {
            throw new IllegalStateException("GetCurrentThread( ) errorNo=" + e.getErrorCode(), e);
        }
    }

    @Override
    public int getCpu() {
        return -1;
    }

    @Override
    public int getProcessId() {
        if (LIBRARY_IS_STUB) {
            return 1;
        }
        try {
            return Kernel32.INSTANCE.GetCurrentProcessId();
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            return 1;
        }
    }

    @Override
    public int getThreadId() {
        if (LIBRARY_IS_STUB) {
            return 1;
        }
        Integer tid = THREAD_ID.get();
        if (tid == null) {
            try {
                tid = Kernel32.INSTANCE.GetCurrentThreadId();
            } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
                tid = 1;
            }
            THREAD_ID.set(tid);
        }
        return tid;
    }

    /**
     * @author BegemoT
     */
    private interface CLibrary extends Library {
        int GetProcessAffinityMask(final WinNT.HANDLE pid, final PointerType lpProcessAffinityMask, final PointerType lpSystemAffinityMask) throws LastErrorException;

        void SetThreadAffinityMask(final WinNT.HANDLE pid, final WinDef.DWORD lpProcessAffinityMask) throws LastErrorException;

        int GetCurrentThread() throws LastErrorException;
    }

    @SuppressFBWarnings(value = "NM_METHOD_NAMING_CONVENTION", justification = "Method names mirror WinAPI declarations for JNA compatibility and are intentionally PascalCase")
    private static final class StubWindowsCLibrary implements CLibrary {
        private long mask = 1L;

        @Override
        public int GetProcessAffinityMask(WinNT.HANDLE pid, PointerType lpProcessAffinityMask, PointerType lpSystemAffinityMask) {
            if (lpProcessAffinityMask != null && lpProcessAffinityMask.getPointer() != null) {
                lpProcessAffinityMask.getPointer().setLong(0, mask);
            }
            if (lpSystemAffinityMask != null && lpSystemAffinityMask.getPointer() != null) {
                lpSystemAffinityMask.getPointer().setLong(0, mask);
            }
            return 1;
        }

        @Override
        public void SetThreadAffinityMask(WinNT.HANDLE pid, WinDef.DWORD lpProcessAffinityMask) {
            mask = lpProcessAffinityMask.longValue();
        }

        @Override
        public int GetCurrentThread() {
            return 1;
        }
    }
}
