package net.openhft.affinity.lockchecker;

import junit.framework.TestCase;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.io.RandomAccessFile;

public class LockReferenceTest extends TestCase {

    public void testConstructorAndGetChannel() throws Exception {
        // Create a temporary file and open a channel to it
        RandomAccessFile file = new RandomAccessFile("testfile", "rw");
        FileChannel channel = file.getChannel();
        FileLock lock = channel.lock();

        // Create LockReference
        LockReference lockReference = new LockReference(channel, lock);

        // Check if channel and lock are correctly assigned
        assertEquals(channel, lockReference.getChannel());
        assertEquals(lock, lockReference.lock);

        // Release the lock and close the channel
        lock.release();
        channel.close();
        file.close();
    }

    public void testGetChannel() throws Exception {
        // Create a temporary file and open a channel to it
        RandomAccessFile file = new RandomAccessFile("testfile", "rw");
        FileChannel channel = file.getChannel();
        FileLock lock = channel.lock();

        // Create LockReference
        LockReference lockReference = new LockReference(channel, lock);

        // Check if getChannel() returns the correct channel
        assertEquals(channel, lockReference.getChannel());

        // Release the lock and close the channel
        lock.release();
        channel.close();
        file.close();
    }
}
