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

import net.openhft.affinity.CpuLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;

/**
 * This class represents the CPU layout for a system, providing information about
 * sockets, cores, and threads.
 */
public class VanillaCpuLayout implements CpuLayout {
    public static final int MAX_CPUS_SUPPORTED = 256;

    @NotNull
    private final List<CpuInfo> cpuDetails;
    private final int sockets;
    private final int coresPerSocket;
    private final int threadsPerCore;

    /**
     * Constructs a VanillaCpuLayout instance with the given CPU details.
     *
     * @param cpuDetails a list of CpuInfo objects representing the CPU details
     */
    VanillaCpuLayout(@NotNull List<CpuInfo> cpuDetails) {
        this.cpuDetails = cpuDetails;
        SortedSet<Integer> sockets = new TreeSet<>(),
                cores = new TreeSet<>(),
                threads = new TreeSet<>();
        for (CpuInfo cpuDetail : cpuDetails) {
            sockets.add(cpuDetail.socketId);
            cores.add((cpuDetail.socketId << 16) + cpuDetail.coreId);
            threads.add(cpuDetail.threadId);
        }
        this.sockets = sockets.size();
        this.coresPerSocket = cores.size() / sockets.size();
        this.threadsPerCore = threads.size();
        if (cpuDetails.size() != sockets() * coresPerSocket() * threadsPerCore()) {
            StringBuilder error = new StringBuilder();
            error.append("cpuDetails.size= ").append(cpuDetails.size())
                    .append(" != sockets: ").append(sockets())
                    .append(" * coresPerSocket: ").append(coresPerSocket())
                    .append(" * threadsPerCore: ").append(threadsPerCore()).append('\n');
            for (CpuInfo detail : cpuDetails) {
                error.append(detail).append('\n');
            }
            LoggerFactory.getLogger(VanillaCpuLayout.class).warn(error.toString());
        }
    }

    /**
     * Creates a VanillaCpuLayout instance from a properties file.
     *
     * @param fileName the name of the properties file
     * @return a VanillaCpuLayout instance
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public static VanillaCpuLayout fromProperties(String fileName) throws IOException {
        return fromProperties(openFile(fileName));
    }

    /**
     * Creates a VanillaCpuLayout instance from an InputStream of properties.
     *
     * @param is the InputStream of properties
     * @return a VanillaCpuLayout instance
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public static VanillaCpuLayout fromProperties(InputStream is) throws IOException {
        Properties prop = new Properties();
        prop.load(is);
        return fromProperties(prop);
    }

    /**
     * Creates a VanillaCpuLayout instance from a Properties object.
     *
     * @param prop the Properties object
     * @return a VanillaCpuLayout instance
     */
    @NotNull
    public static VanillaCpuLayout fromProperties(@NotNull Properties prop) {
        List<CpuInfo> cpuDetails = new ArrayList<>();
        for (int i = 0; i < MAX_CPUS_SUPPORTED; i++) {
            String line = prop.getProperty("" + i);
            if (line == null) break;
            String[] word = line.trim().split(" *, *");
            CpuInfo details = new CpuInfo(parseInt(word[0]),
                    parseInt(word[1]), parseInt(word[2]));
            cpuDetails.add(details);
        }
        return new VanillaCpuLayout(cpuDetails);
    }

    /**
     * Creates a VanillaCpuLayout instance from the /proc/cpuinfo file.
     *
     * @return a VanillaCpuLayout instance
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public static VanillaCpuLayout fromCpuInfo() throws IOException {
        return fromCpuInfo("/proc/cpuinfo");
    }

    /**
     * Creates a VanillaCpuLayout instance from a specified cpuinfo file.
     *
     * @param filename the name of the cpuinfo file
     * @return a VanillaCpuLayout instance
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public static VanillaCpuLayout fromCpuInfo(String filename) throws IOException {
        return fromCpuInfo(openFile(filename));
    }

    /**
     * Opens a file and returns its InputStream.
     *
     * @param filename the name of the file
     * @return an InputStream of the file
     * @throws FileNotFoundException if the file is not found
     */
    private static InputStream openFile(String filename) throws FileNotFoundException {
        try {
            return new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (is == null)
                throw e;
            return is;
        }
    }

    /**
     * Creates a VanillaCpuLayout instance from an InputStream of cpuinfo.
     *
     * @param is the InputStream of cpuinfo
     * @return a VanillaCpuLayout instance
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public static VanillaCpuLayout fromCpuInfo(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        List<CpuInfo> cpuDetails = new ArrayList<>();
        CpuInfo details = new CpuInfo();
        Map<String, Integer> threadCount = new LinkedHashMap<>();

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) {
                String key = details.socketId + "," + details.coreId;
                Integer count = threadCount.get(key);
                if (count == null)
                    threadCount.put(key, count = 1);
                else
                    threadCount.put(key, count += 1);
                details.threadId = count - 1;
                cpuDetails.add(details);
                details = new CpuInfo();
                details.coreId = cpuDetails.size();
                continue;
            }
            String[] words = line.split("\\s*:\\s*", 2);
            if (words[0].equals("physical id"))
                details.socketId = parseInt(words[1]);
            else if (words[0].equals("core id"))
                details.coreId = parseInt(words[1]);
        }
        return new VanillaCpuLayout(cpuDetails);
    }

    /**
     * Gets the number of CPUs.
     *
     * @return the number of CPUs
     */
    @Override
    public int cpus() {
        return cpuDetails.size();
    }

    /**
     * Gets the number of sockets.
     *
     * @return the number of sockets
     */
    public int sockets() {
        return sockets;
    }

    /**
     * Gets the number of cores per socket.
     *
     * @return the number of cores per socket
     */
    public int coresPerSocket() {
        return coresPerSocket;
    }

    /**
     * Gets the number of threads per core.
     *
     * @return the number of threads per core
     */
    @Override
    public int threadsPerCore() {
        return threadsPerCore;
    }

    /**
     * Gets the socket ID for the given CPU ID.
     *
     * @param cpuId the CPU ID
     * @return the socket ID
     */
    @Override
    public int socketId(int cpuId) {
        return cpuDetails.get(cpuId).socketId;
    }

    /**
     * Gets the core ID for the given CPU ID.
     *
     * @param cpuId the CPU ID
     * @return the core ID
     */
    @Override
    public int coreId(int cpuId) {
        return cpuDetails.get(cpuId).coreId;
    }

    /**
     * Gets the thread ID for the given CPU ID.
     *
     * @param cpuId the CPU ID
     * @return the thread ID
     */
    @Override
    public int threadId(int cpuId) {
        return cpuDetails.get(cpuId).threadId;
    }

    /**
     * Returns a string representation of the VanillaCpuLayout.
     *
     * @return a string representation of the VanillaCpuLayout
     */
    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, cpuDetailsSize = cpuDetails.size(); i < cpuDetailsSize; i++) {
            CpuInfo cpuDetail = cpuDetails.get(i);
            sb.append(i).append(": ").append(cpuDetail).append('\n');
        }
        return sb.toString();
    }

    /**
     * Checks if this VanillaCpuLayout is equal to another object.
     *
     * @param o the object to compare to
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VanillaCpuLayout that = (VanillaCpuLayout) o;

        if (coresPerSocket != that.coresPerSocket) return false;
        if (sockets != that.sockets) return false;
        if (threadsPerCore != that.threadsPerCore) return false;
        return cpuDetails.equals(that.cpuDetails);

    }

    /**
     * Returns a hash code for this VanillaCpuLayout.
     *
     * @return a hash code for this VanillaCpuLayout
     */
    @Override
    public int hashCode() {
        int result = cpuDetails.hashCode();
        result = 31 * result + sockets;
        result = 31 * result + coresPerSocket;
        result = 31 * result + threadsPerCore;
        return result;
    }

    /**
     * This class represents the details of a CPU, including the socket ID, core ID, and thread ID.
     */
    static class CpuInfo {
        int socketId, coreId, threadId;

        /**
         * Default constructor for CpuInfo.
         */
        CpuInfo() {
        }

        /**
         * Constructs a CpuInfo instance with the given socket ID, core ID, and thread ID.
         *
         * @param socketId the socket ID
         * @param coreId   the core ID
         * @param threadId the thread ID
         */
        CpuInfo(int socketId, int coreId, int threadId) {
            this.socketId = socketId;
            this.coreId = coreId;
            this.threadId = threadId;
        }

        /**
         * Returns a string representation of the CpuInfo.
         *
         * @return a string representation of the CpuInfo
         */
        @NotNull
        @Override
        public String toString() {
            return "CpuInfo{" +
                    "socketId=" + socketId +
                    ", coreId=" + coreId +
                    ", threadId=" + threadId +
                    '}';
        }

        /**
         * Checks if this CpuInfo is equal to another object.
         *
         * @param o the object to compare to
         * @return true if the objects are equal, false otherwise
         */
        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CpuInfo cpuInfo = (CpuInfo) o;

            if (coreId != cpuInfo.coreId) return false;
            if (socketId != cpuInfo.socketId) return false;
            return threadId == cpuInfo.threadId;

        }

        /**
         * Returns a hash code for this CpuInfo.
         *
         * @return a hash code for this CpuInfo
         */
        @Override
        public int hashCode() {
            int result = socketId;
            result = 31 * result + coreId;
            result = 31 * result + threadId;
            return result;
        }
    }
}
