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

package net.openhft.affinity;

/**
 * @author peter.lawrey
 */
public interface CpuLayout {
    /**
     * @return the number of cpus.
     */
    int cpus();

    int sockets();

    int coresPerSocket();

    int threadsPerCore();

    /**
     * @param cpuId the logical processor number
     * @return which socket id this cpu is on.
     */
    int socketId(int cpuId);

    /**
     * @param cpuId the logical processor number
     * @return which core on a socket this cpu is on.
     */
    int coreId(int cpuId);

    /**
     * @param cpuId the logical processor number
     * @return which thread on a core this cpu is on.
     */
    int threadId(int cpuId);
}
