package net.openhft.affinity.lockchecker;

import java.io.IOException;

/**
 * @author Tom Shercliff
 */

public interface LockChecker {

    boolean isLockFree(int id);

    boolean obtainLock(int id, String metaInfo) throws IOException;

    boolean releaseLock(int id);

    String getMetaInfo(int id) throws IOException;
}
