package net.openhft.affinity.testimpl;

import net.openhft.affinity.lockchecker.FileBasedLockChecker;

import java.io.File;

public class TestFileBasedLockChecker extends FileBasedLockChecker {

    public File doToFile(int cpu) {
        return toFile(cpu);
    }
}
