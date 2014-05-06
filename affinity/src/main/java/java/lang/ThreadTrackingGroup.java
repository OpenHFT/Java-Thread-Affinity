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
        System.out.println("ThreadTrackingGroup.add: " + t); //todo: remove
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
