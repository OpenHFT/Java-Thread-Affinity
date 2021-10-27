package net.openhft.affinity.testimpl;

import net.openhft.affinity.lockchecker.FileLockBasedLockChecker;

import java.io.File;

public class TestFileLockBasedLockChecker extends FileLockBasedLockChecker {

    public File doToFile(int cpu) {
        return toFile(cpu);
    }
}
