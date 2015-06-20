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

package java.lang;

/**
 * A listener for various events in a Thread's life: creation, termination, etc.
 */
public interface ThreadLifecycleListener {

    /**
     * The specified thread is about to be started.
     * @param t the thread which is being started
     */
    void started(Thread t);

    /**
     * The specified thread failed to start.
     * @param t the thread that had a failed start
     */
    void startFailed(Thread t);

    /**
     * The specified thread has been terminated.
     * @param t the thread that has been terminated
     */
    void terminated(Thread t);
}
