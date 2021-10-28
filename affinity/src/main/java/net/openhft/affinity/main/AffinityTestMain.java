package net.openhft.affinity.main;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Tom Shercliff
 */
public class AffinityTestMain {

    public static void main(String[] args) {

        int cpus = 1;
        if (args.length == 0) {
            cpus = AffinityLock.cpuLayout().cpus() / 12;
        } else {
            cpus = Integer.valueOf(args[0]);
        }

        for (int i = 0; i < cpus; i++) {
            acquireAndDoWork();
        }
    }

    private static void acquireAndDoWork() {

        Thread t = new Thread(() -> {
            final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");
            try (AffinityLock al = Affinity.acquireLock()) {
                String threadName = Thread.currentThread().getName();
                System.out.println("Thread (" + threadName + ") locked onto cpu " + al.cpuId());

                while (true) {
                    System.out.println(df.format(new Date()) + " - Thread (" + threadName + ") doing work on cpu " + al.cpuId() + ". IsAllocated = " + al.isAllocated() + ", isBound = " + al.isBound() + ". " + al.toString());

                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e) {
                        //nothing
                    }
                }
            }
        });
        t.start();
    }
}
