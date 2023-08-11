package net.openhft.affinity.impl.isolate;

import java.util.Set;

/**
 * Holds <strong>Chronicle Tune</strong> configuration. Tune is a commercial offering. Please find more information
 * <a href="https://chronicle.software/tune/">here</a>.
 */
public interface IsolateConfiguration {

    /**
     * @return true if this machine is actively configured with Chronicle Tune.
     */
    boolean configured();

    /**
     * @return the set of CPUs isolated by Chronicle Tune.
     */
    Set<Integer> isolatedCpus();

    /**
     * @return true if the given CPU is isolated by Chronicle Tune.
     */
    boolean isolated(int cpuId);
}
