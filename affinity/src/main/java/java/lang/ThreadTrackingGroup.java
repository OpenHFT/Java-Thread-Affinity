/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
