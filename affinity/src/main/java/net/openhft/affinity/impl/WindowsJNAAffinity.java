/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.affinity.impl;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.LongByReference;
import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<>();

    @Override
    public long getAffinity() {
        final CLibrary lib = CLibrary.INSTANCE;
        final LongByReference cpuset1 = new LongByReference(0);
        final LongByReference cpuset2 = new LongByReference(0);
        try {

            final int ret = lib.GetProcessAffinityMask(-1, cpuset1, cpuset2);
            if (ret < 0)
                throw new IllegalStateException("GetProcessAffinityMask(( -1 ), &(" + cpuset1 + "), &(" + cpuset2 + ") ) return " + ret);

            return cpuset1.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void setAffinity(final long affinity) {
        final CLibrary lib = CLibrary.INSTANCE;

        WinDef.DWORD aff = new WinDef.DWORD(affinity);
        int pid = getTid();
        try {
            lib.SetThreadAffinityMask(pid, aff);
        } catch (LastErrorException e) {
            throw new IllegalStateException("SetThreadAffinityMask((" + pid + ") , &(" + affinity + ") ) errorNo=" + e.getErrorCode(), e);
        }
    }

    public int getTid() {
        final CLibrary lib = CLibrary.INSTANCE;

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
        return Kernel32.INSTANCE.GetCurrentProcessId();
    }

    @Override
    public int getThreadId() {
        Integer tid = THREAD_ID.get();
        if (tid == null)
            THREAD_ID.set(tid = Kernel32.INSTANCE.GetCurrentThreadId());
        return tid;
    }

    /**
     * @author BegemoT
     */
    private interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary) Native.loadLibrary("kernel32", CLibrary.class);

        int GetProcessAffinityMask(final int pid, final PointerType lpProcessAffinityMask, final PointerType lpSystemAffinityMask) throws LastErrorException;

        void SetThreadAffinityMask(final int pid, final WinDef.DWORD lpProcessAffinityMask) throws LastErrorException;

        int GetCurrentThread() throws LastErrorException;
    }

    static {
        boolean loaded = false;
        try {
            INSTANCE.getAffinity();
            loaded = true;
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("Unable to load jna library", e);
        }
        LOADED = loaded;
    }
}
