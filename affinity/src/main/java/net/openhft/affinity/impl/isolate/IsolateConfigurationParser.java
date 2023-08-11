package net.openhft.affinity.impl.isolate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class IsolateConfigurationParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsolateConfigurationParser.class);

    public IsolateConfiguration parse(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            IsolateConfigurationImplBuilder builder = new IsolateConfigurationImplBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().matches("cpu.*=.*")) {
                    parseIsolatedCpus(line, builder);
                }
            }

            return builder.build();

        } catch (Exception e) {
            LOGGER.warn("Failed to parse isolate configuration, proceeding with empty configuration", e);
            return IsolateConfigurationFactory.EMPTY;
        }
    }

    private void parseIsolatedCpus(String line, IsolateConfigurationImplBuilder builder) {
        String[] chunks = line.split("=");
        String csv = chunks[1];
        String[] entries = csv.split(",");
        for (String entry : entries) {
            if (entry.contains("-")) {
                String[] range = entry.split("-");
                int rangeStart = Integer.parseInt(range[0].trim());
                int rangeEnd = Integer.parseInt(range[1].trim());
                for (int i = rangeStart; i <= rangeEnd; i++) {
                    builder.addIsolatedCpu(i);
                }
            } else {
                builder.addIsolatedCpu(Integer.parseInt(entry.trim()));
            }
        }
    }

    private static class IsolateConfigurationImpl implements IsolateConfiguration {

        private final SortedSet<Integer> isolatedCpus;

        private IsolateConfigurationImpl(SortedSet<Integer> isolatedCpus) {
            this.isolatedCpus = Collections.unmodifiableSortedSet(isolatedCpus);
        }

        @Override
        public boolean configured() {
            return true;
        }

        @Override
        public Set<Integer> isolatedCpus() {
            return isolatedCpus;
        }

        @Override
        public boolean isolated(int cpuId) {
            return isolatedCpus.contains(cpuId);
        }

        @Override
        public String toString() {
            return "IsolateConfigurationImpl{" + "isolatedCpus=" + isolatedCpus + '}';
        }
    }

    private static class IsolateConfigurationImplBuilder {

        private final SortedSet<Integer> isolatedCpus = new TreeSet<>();

        public IsolateConfigurationImplBuilder addIsolatedCpu(int cpuId) {
            isolatedCpus.add(cpuId);
            return this;
        }

        public IsolateConfiguration build() {
            return new IsolateConfigurationImpl(isolatedCpus);
        }

    }

}
