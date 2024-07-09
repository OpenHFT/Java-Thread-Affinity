package net.openhft.affinity;

import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MicroJitterSamplerTest extends TestCase {

    public void testPauseBusyWait() throws InterruptedException {
        System.setProperty("busywait", "true");
        MicroJitterSampler.pause();
    }

    public void testPauseSleep() throws InterruptedException {
        System.setProperty("busywait", "false");
        MicroJitterSampler.pause();
    }

    public void testAsString() {
        assertEquals("500ns", MicroJitterSampler.asString(500));
        assertEquals("500us", MicroJitterSampler.asString(500000));
        assertEquals("500ms", MicroJitterSampler.asString(500000000));
        assertEquals("5sec", MicroJitterSampler.asString(5000000000L));
    }

    public void testReset() {
        MicroJitterSampler sampler = new MicroJitterSampler();
        sampler.reset();
        for (int count : sampler.count) {
            assertEquals(0, count);
        }
        assertEquals(0, sampler.totalTime);
    }

    public void testSample() {
        MicroJitterSampler sampler = new MicroJitterSampler();
        sampler.sample(1000000L);
        // This is a simple check to ensure that some counts have increased
        boolean countIncreased = false;
        for (int count : sampler.count) {
            if (count > 0) {
                countIncreased = true;
                break;
            }
        }
        assertTrue(countIncreased);
        assertEquals(1000000L, sampler.totalTime);
    }

    public void testPrint() {
        MicroJitterSampler sampler = new MicroJitterSampler();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        sampler.sample(1000000L); // Perform a sample to have some data
        sampler.print(ps);
        String output = baos.toString();
        assertTrue(output.contains("After"));
    }

    public void testRun() throws InterruptedException {
        MicroJitterSampler sampler = new MicroJitterSampler();
        Thread t = new Thread(sampler::run);
        t.start();
        Thread.sleep(1000); // Let it run for 1 second
        t.interrupt();
        t.join();
    }
}
