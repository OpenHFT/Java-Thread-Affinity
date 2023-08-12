package net.openhft.affinity.impl.isolate;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static net.openhft.affinity.impl.isolate.IsolateConfigurationParser.ParseException;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class IsolateConfigurationParserTest {

    private IsolateConfigurationParser parser;

    @Before
    public void before() {
        parser = new IsolateConfigurationParser();
    }

    @Test
    public void parseWellFormed() throws ParseException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("isolate/isolate.ini");
        IsolateConfigurationParser parser = new IsolateConfigurationParser();
        IsolateConfiguration config = parser.parse(inputStream);
        assertThat(config.isolatedCpus(), hasItems(1, 2, 3, 4, 5, 6, 7, 10));
        assertTrue(config.configured());
        for (int cpu : config.isolatedCpus()) {
            assertTrue(config.isolated(cpu));
        }
    }

    @Test
    public void parseFailureSingleCpu() {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse(createCpuInput("!")));
        assertThat(exception.getMessage(), containsString("Could not parse ! as an integer"));
    }

    @Test
    public void parseFailureRange() {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse(createCpuInput("-")));
        assertThat(exception.getMessage(), containsString("cpu range format required is start-end, found -."));
    }

    @Test
    public void parseNoCpus() throws ParseException {
        IsolateConfiguration config = parser.parse(createCpuInput(""));
        assertEquals(0, config.isolatedCpus().size());
        assertTrue(config.configured());
    }

    private InputStream createCpuInput(String cpuString) {
        String text = "[isolate]\n" +
                "cpus=" + cpuString;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        return inputStream;
    }

}