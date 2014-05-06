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
