package net.openhft.affinity.impl.isolate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Set;

/**
 * Factory used by {@link net.openhft.affinity.AffinityLock} for loading isolate configuration.
 *
 * @see IsolateConfiguration
 */
public class IsolateConfigurationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsolateConfigurationFactory.class);

    private static final String ISOLATE_INI_PATH = "/etc/chronicle/isolate/isolate.ini";

    public static final IsolateConfiguration EMPTY_CONFIGURATION = new EmptyIsolateConfiguration();

    public static IsolateConfiguration load() {
        IsolateConfigurationParser parser = new IsolateConfigurationParser();
        try (FileInputStream inputStream = new FileInputStream(ISOLATE_INI_PATH)) {
            return parser.parse(inputStream);
        } catch (IsolateConfigurationParser.ParseException parseException) {
            LOGGER.error("Failed to parse Chronicle Tune isolate configuration", parseException);
        } catch (FileNotFoundException fileNotFoundException) {
            LOGGER.debug("Chronicle Tune is not configured on this system");
        } catch (Exception e) {
            LOGGER.error("Unexpected exception encountered whilst parsing Chronicle Tune isolate configuration", e);
        }
        return EMPTY_CONFIGURATION;
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
