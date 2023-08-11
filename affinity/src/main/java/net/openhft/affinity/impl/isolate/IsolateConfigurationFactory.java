package net.openhft.affinity.impl.isolate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Set;

public class IsolateConfigurationFactory {

    private static final String ISOLATE_INI_PATH = "/etc/chronicle/isolate/isolate.ini";

    public static final IsolateConfiguration EMPTY = new EmptyIsolateConfiguration();

    public static IsolateConfiguration load() {
        IsolateConfigurationParser parser = new IsolateConfigurationParser();
        try {
            return parser.parse(new FileInputStream(ISOLATE_INI_PATH));
        } catch (FileNotFoundException e) {
            return EMPTY;
        }
    }

    public static class EmptyIsolateConfiguration implements IsolateConfiguration {

        @Override
        public boolean configured() {
            return false;
        }

        @Override
        public Set<Integer> isolatedCpus() {
            return Collections.emptySet();
        }

        @Override
        public boolean isolated(int cpuId) {
            return false;
        }
    }

}
