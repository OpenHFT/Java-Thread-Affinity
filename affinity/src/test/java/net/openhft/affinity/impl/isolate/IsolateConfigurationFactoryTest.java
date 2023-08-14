package net.openhft.affinity.impl.isolate;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class IsolateConfigurationFactoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void after() {
        // Clear this after each test to prevent test bleed
        System.clearProperty(IsolateConfigurationFactory.ISOLATE_INI_PATH_OVERRIDE_PROPERTY);
    }

    @Test
    public void fileNotFound() {
        setIniPath(Paths.get(temporaryFolder.toString(), "does-not-exist").toString());

        IsolateConfiguration config = IsolateConfigurationFactory.load();
        assertEquals(IsolateConfigurationFactory.EMPTY_CONFIGURATION, config);
    }

    @Test
    public void parseError() throws IOException {
        File file = temporaryFolder.newFile("test.ini");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("[isolate]");
        writer.newLine();
        writer.write("cpu=!&^#");
        setIniPath(Paths.get(temporaryFolder.toString(), "test.ini").toString());

        IsolateConfiguration config = IsolateConfigurationFactory.load();
        assertEquals(IsolateConfigurationFactory.EMPTY_CONFIGURATION, config);
    }

    private static void setIniPath(String iniPath) {
        System.setProperty(IsolateConfigurationFactory.ISOLATE_INI_PATH_OVERRIDE_PROPERTY, iniPath);
    }

}