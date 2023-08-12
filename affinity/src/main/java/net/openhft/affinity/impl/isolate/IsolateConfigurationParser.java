package net.openhft.affinity.impl.isolate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Parses isolate configuration.
 *
 * @see IsolateConfiguration
 */
public class IsolateConfigurationParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsolateConfigurationParser.class);
    private static final String SECTION_HEADER_ISOLATE = "isolate";

    /***
     * Parse isolate configuration from the given input stream.
     * @return parsed configuration
     * @throws ParseException if there was any failure during the parsing operation
     */
    public IsolateConfiguration parse(InputStream inputStream) throws ParseException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            IsolateParserContext parserContext = new IsolateParserContext(inputStream);
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                parseLineInFile(trimmedLine, parserContext);
            }

            return parserContext.build();
        } catch (IOException e) {
            throw new ParseException("Could not open stream", e);
        }
    }

    private void parseLineInFile(String line, IsolateParserContext parserContext) throws ParseException {
        parserContext.incrementLineNumber();

        // Process section headers
        if (line.matches("\\[.*\\]")) {
            onSectionHeader(line, parserContext);
        }

        // Process [isolate] section
        if (SECTION_HEADER_ISOLATE.equals(parserContext.getSectionHeading())) {
            if (line.matches("cpu.*=.*")) {
                parseIsolatedCpus(line, parserContext);
            }
        }
    }

    private void onSectionHeader(String line, IsolateParserContext parserContext) {
        String sectionHeading = line.replaceAll("([\\[\\]])", "");
        parserContext.setSectionHeading(sectionHeading);
    }

    private void parseIsolatedCpus(String line, IsolateParserContext parserContext) throws ParseException {
        String[] chunks = line.split("=");
        if (chunks.length < 2) {
            LOGGER.warn("isolate configuration was found but no cpus are isolated");
            return;
        }
        String csv = chunks[1];
        String[] entries = csv.split(",");
        for (String entry : entries) {
            if (entry.contains("-")) {
                String[] range = entry.split("-");
                if (range.length < 2) {
                    throw parserContext.exception("cpu range format required is start-end, found %s.", entry);
                }
                int rangeStart = parseCpu(range[0], parserContext);
                int rangeEnd = parseCpu(range[1], parserContext);
                for (int i = rangeStart; i <= rangeEnd; i++) {
                    parserContext.addIsolatedCpu(i);
                }
            } else {
                parserContext.addIsolatedCpu(parseCpu(entry, parserContext));
            }
        }
    }

    private int parseCpu(String input, IsolateParserContext parserContext) throws ParseException {
        String trimmed = input.trim();
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException nfe) {
            throw parserContext.exception("Could not parse %s as an integer", trimmed);
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

    }

    private static class IsolateParserContext {

        private final SortedSet<Integer> isolatedCpus = new TreeSet<>();

        private String sectionHeading;

        private int lineNumber;

        private final InputStream inputStream;

        private IsolateParserContext(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public IsolateParserContext addIsolatedCpu(int cpuId) {
            isolatedCpus.add(cpuId);
            return this;
        }

        public String getSectionHeading() {
            return sectionHeading;
        }

        public void setSectionHeading(String sectionHeading) {
            this.sectionHeading = sectionHeading;
        }

        public void incrementLineNumber() {
            lineNumber++;
        }

        public ParseException exception(String format, Object... args) {
            String message = "Failed to parse Chronicle Tune configuration on line " + lineNumber + " - " + String.format(format, args);
            return new ParseException(message);
        }

        public IsolateConfiguration build() {
            return new IsolateConfigurationImpl(isolatedCpus);
        }

    }

    public static class ParseException extends Exception {

        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Exception exception) {
            super(message, exception);
        }
    }

}
