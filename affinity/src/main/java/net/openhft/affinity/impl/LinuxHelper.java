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
import com.sun.jna.ptr.IntByReference;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class LinuxHelper {
    private static final String LIBRARY_NAME = "c";
    private static final VersionHelper UNKNOWN = new VersionHelper(0, 0, 0);
    private static final VersionHelper VERSION_2_6 = new VersionHelper(2, 6, 0);

    private static final VersionHelper version;

    static {
        final utsname uname = new utsname();
        VersionHelper ver = UNKNOWN;
        try {
            if (CLibrary.INSTANCE.uname(uname) == 0) {
                ver = new VersionHelper(uname.getRealeaseVersion());
            }
        } catch (Throwable e) {
            //Jvm.warn().on(getClass(), "Failed to determine Linux version: " + e);
        }

        version = ver;
    }

    public static
    @NotNull
    cpu_set_t sched_getaffinity() {
        final CLibrary lib = CLibrary.INSTANCE;
        final cpu_set_t cpuset = new cpu_set_t();
        final int size = version.isSameOrNewer(VERSION_2_6) ? cpu_set_t.SIZE_OF_CPU_SET_T : NativeLong.SIZE;

        try {
            if (lib.sched_getaffinity(0, size, cpuset) != 0) {
                throw new IllegalStateException("sched_getaffinity(0, " + size +
                        ", cpuset) failed; errno=" + Native.getLastError());
            }
        } catch (LastErrorException e) {
            throw new IllegalStateException("sched_getaffinity(0, (" +
                    size + ") , cpuset) failed; errno=" + e.getErrorCode(), e);
        }
        return cpuset;
    }

	public static void sched_setaffinity(final BitSet affinity) {
		sched_setaffinity(0, affinity);
	}
	
    public static void sched_setaffinity(final int pid, final BitSet affinity) {
        final CLibrary lib = CLibrary.INSTANCE;
        final cpu_set_t cpuset = new cpu_set_t();
        final int size = version.isSameOrNewer(VERSION_2_6) ? cpu_set_t.SIZE_OF_CPU_SET_T : NativeLong.SIZE;
        final long[] bits = affinity.toLongArray();
        for (int i = 0; i < bits.length; i++) {
            if (Platform.is64Bit()) {
                cpuset.__bits[i].setValue(bits[i]);
            } else {
                cpuset.__bits[i * 2].setValue(bits[i] & 0xFFFFFFFFL);
                cpuset.__bits[i * 2 + 1].setValue((bits[i] >>> 32) & 0xFFFFFFFFL);
            }
        }
        try {
            if (lib.sched_setaffinity(pid, size, cpuset) != 0) {
                throw new IllegalStateException("sched_setaffinity(" + pid + ", " + size +
                        ", 0x" + Utilities.toHexString(affinity) + ") failed; errno=" + Native.getLastError());
            }
        } catch (LastErrorException e) {
            throw new IllegalStateException("sched_setaffinity(" + pid + ", " + size +
                    ", 0x" + Utilities.toHexString(affinity) + ") failed; errno=" + e.getErrorCode(), e);
        }
    }

    public static int sched_getcpu() {
        final CLibrary lib = CLibrary.INSTANCE;
        try {
            final int ret = lib.sched_getcpu();
            if (ret < 0) {
                throw new IllegalStateException("sched_getcpu() failed; errno=" + Native.getLastError());
            }
            return ret;
        } catch (LastErrorException e) {
            throw new IllegalStateException("sched_getcpu() failed; errno=" + e.getErrorCode(), e);
        } catch (UnsatisfiedLinkError ule) {
            try {
                final IntByReference cpu = new IntByReference();
                final IntByReference node = new IntByReference();
                final int ret = lib.syscall(318, cpu, node, null);
                if (ret != 0) {
                    throw new IllegalStateException("getcpu() failed; errno=" + Native.getLastError());
                }
                return cpu.getValue();
            } catch (LastErrorException lee) {
                if (lee.getErrorCode() == 38 && Platform.is64Bit()) { // unknown call
                    final Pointer getcpuAddr = new Pointer((-10L << 20) + 1024L * 2L);
                    final Function getcpu = Function.getFunction(getcpuAddr, Function.C_CONVENTION);
                    final IntByReference cpu = new IntByReference();
                    if (getcpu.invokeInt(new Object[]{cpu, null, null}) < 0) {
                        throw new IllegalStateException("getcpu() failed; errno=" + Native.getLastError());

                    } else {
                        return cpu.getValue();
                    }
                } else {
                    throw new IllegalStateException("getcpu() failed; errno=" + lee.getErrorCode(), lee);
                }
            }
        }
    }

    public static int getpid() {
        final CLibrary lib = CLibrary.INSTANCE;
        try {
            final int ret = lib.getpid();
            if (ret < 0) {
                throw new IllegalStateException("getpid() failed; errno=" + Native.getLastError());
            }
            return ret;
        } catch (LastErrorException e) {
            throw new IllegalStateException("getpid() failed; errno=" + e.getErrorCode(), e);
        }
    }

    public static int syscall(int number, Object... args) {
        final CLibrary lib = CLibrary.INSTANCE;
        try {
            final int ret = lib.syscall(number, args);
            if (ret < 0) {
                throw new IllegalStateException("sched_getcpu() failed; errno=" + Native.getLastError());
            }
            return ret;
        } catch (LastErrorException e) {
            throw new IllegalStateException("sched_getcpu() failed; errno=" + e.getErrorCode(), e);
        }
    }

    interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load(LIBRARY_NAME, CLibrary.class);

        int sched_setaffinity(final int pid,
                              final int cpusetsize,
                              final cpu_set_t cpuset) throws LastErrorException;

        int sched_getaffinity(final int pid,
                              final int cpusetsize,
                              final cpu_set_t cpuset) throws LastErrorException;

        int getpid() throws LastErrorException;

        int sched_getcpu() throws LastErrorException;

        int uname(final utsname name) throws LastErrorException;

        int syscall(int number, Object... args) throws LastErrorException;
    }

    /**
     * Structure describing the system and machine.
     */
    public static class utsname extends Structure {
        public static final int _UTSNAME_LENGTH = 65;

        static List<String> FIELD_ORDER = Arrays.asList(
                "sysname",
                "nodename",
                "release",
                "version",
                "machine",
                "domainname"
        );

        /**
         * Name of the implementation of the operating system.
         */
        public byte[] sysname = new byte[_UTSNAME_LENGTH];

        /**
         * Name of this node on the network.
         */
        public byte[] nodename = new byte[_UTSNAME_LENGTH];

        /**
         * Current release level of this implementation.
         */
        public byte[] release = new byte[_UTSNAME_LENGTH];

        /**
         * Current version level of this release.
         */
        public byte[] version = new byte[_UTSNAME_LENGTH];

        /**
         * Name of the hardware type the system is running on.
         */
        public byte[] machine = new byte[_UTSNAME_LENGTH];

        /**
         * NIS or YP domain name
         */
        public byte[] domainname = new byte[_UTSNAME_LENGTH];

        static int length(final byte[] data) {
            int len = 0;
            final int datalen = data.length;
            while (len < datalen && data[len] != 0)
                len++;
            return len;
        }

        @Override
        protected List getFieldOrder() {
            return FIELD_ORDER;
        }

        public String getSysname() {
            return new String(sysname, 0, length(sysname));
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public String getNodename() {
            return new String(nodename, 0, length(nodename));
        }

        public String getRelease() {
            return new String(release, 0, length(release));
        }

        public String getRealeaseVersion() {
            final String release = getRelease();
            final int releaseLen = release.length();
            int len = 0;
            for (; len < releaseLen; len++) {
                final char c = release.charAt(len);
                if (Character.isDigit(c) || c == '.') {
                    continue;
                }
                break;
            }
            return release.substring(0, len);
        }

        public String getVersion() {
            return new String(version, 0, length(version));
        }

        public String getMachine() {
            return new String(machine, 0, length(machine));
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public String getDomainname() {
            return new String(domainname, 0, length(domainname));
        }

        @Override
        public String toString() {
            return getSysname() + " " + getRelease() +
                    " " + getVersion() + " " + getMachine();
        }
    }

    public static class cpu_set_t extends Structure {
        static final int __CPU_SETSIZE = 1024;
        static final int __NCPUBITS = 8 * NativeLong.SIZE;
        static final int SIZE_OF_CPU_SET_T = (__CPU_SETSIZE / __NCPUBITS) * NativeLong.SIZE;
        static List<String> FIELD_ORDER = Collections.singletonList("__bits");
        public NativeLong[] __bits = new NativeLong[__CPU_SETSIZE / __NCPUBITS];

        public cpu_set_t() {
            for (int i = 0; i < __bits.length; i++) {
                __bits[i] = new NativeLong(0);
            }
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public static void __CPU_ZERO(cpu_set_t cpuset) {
            for (NativeLong bits : cpuset.__bits) {
                bits.setValue(0L);
            }
        }

        public static int __CPUELT(int cpu) {
            return cpu / __NCPUBITS;
        }

        public static long __CPUMASK(int cpu) {
            return 1L << (cpu % __NCPUBITS);
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public static void __CPU_SET(int cpu, cpu_set_t cpuset) {
            cpuset.__bits[__CPUELT(cpu)].setValue(
                    cpuset.__bits[__CPUELT(cpu)].longValue() | __CPUMASK(cpu));
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public static void __CPU_CLR(int cpu, cpu_set_t cpuset) {
            cpuset.__bits[__CPUELT(cpu)].setValue(
                    cpuset.__bits[__CPUELT(cpu)].longValue() & ~__CPUMASK(cpu));
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public static boolean __CPU_ISSET(int cpu, cpu_set_t cpuset) {
            return (cpuset.__bits[__CPUELT(cpu)].longValue() & __CPUMASK(cpu)) != 0;
        }

        @Override
        protected List getFieldOrder() {
            return FIELD_ORDER;
        }
    }
}
