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
 * Implementation interface
 *
 * @author cheremin
 * @since 29.12.11,  20:14
 */
public interface IAffinity {
    /**
     * @return returns affinity mask for current thread, or -1 if unknown
     */
    long getAffinity();

    /**
     * @param affinity sets affinity mask of current thread to specified value
     */
    void setAffinity(final long affinity);

    /**
     * @return the current cpu id, or -1 if unknown.
     */
    int getCpu();

    /**
     * @return the process id of the current process.
     */
    int getProcessId();

    /**
     * @return the thread id of the current thread or -1 is not available.
     */
    int getThreadId();
}
