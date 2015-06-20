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
 * A wrapper of {@link java.lang.ThreadGroup} that tracks the creation and termination of threads.
 */
public class ThreadTrackingGroup extends ThreadGroup {

    /**
     * Listener to be notified of various events in thread lifecycles.
     */
    private final ThreadLifecycleListener listener;

    public ThreadTrackingGroup(ThreadGroup parent, ThreadLifecycleListener listener) {
        super(parent, ThreadTrackingGroup.class.getSimpleName().toLowerCase() + System.identityHashCode(listener));
        this.listener = listener;
    }

    @Override
    void add(Thread t) {
        //  System.out.println("ThreadTrackingGroup.add: " + t); //todo: remove
        super.add(t);
        listener.started(t);
    }

    @Override
    void threadStartFailed(Thread t) {
        super.threadStartFailed(t);
        listener.startFailed(t);
    }

    @Override
    void threadTerminated(Thread t) {
        super.threadTerminated(t);
        listener.terminated(t);
    }
}
