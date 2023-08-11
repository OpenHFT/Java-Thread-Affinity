package net.openhft.affinity.impl.isolate;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IsolateConfigurationParserTest {

    private IsolateConfigurationParser parser;

    @Before
    public void before() {
        parser = new IsolateConfigurationParser();
    }

    @Test
    public void parseWellFormed() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("isolate/isolate.ini");
        IsolateConfigurationParser parser = new IsolateConfigurationParser();
        IsolateConfiguration config = parser.parse(inputStream);
        assertEquals(8, config.isolatedCpus().size());
        assertTrue(config.isolatedCpus().contains(1));
        assertTrue(config.isolatedCpus().contains(2));
        assertTrue(config.isolatedCpus().contains(3));
        assertTrue(config.isolatedCpus().contains(4));
        assertTrue(config.isolatedCpus().contains(5));
        assertTrue(config.isolatedCpus().contains(6));
        assertTrue(config.isolatedCpus().contains(7));
        assertTrue(config.isolatedCpus().contains(10));
    }

    @Test
    public void malformedInput() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("cpus=!".getBytes(StandardCharsets.UTF_8));
        IsolateConfiguration config = parser.parse(inputStream);
        assertEquals(0, config.isolatedCpus().size());
    }

}