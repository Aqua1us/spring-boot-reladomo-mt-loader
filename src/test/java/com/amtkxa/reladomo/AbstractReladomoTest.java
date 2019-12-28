package com.amtkxa.reladomo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gs.fw.common.mithra.test.ConnectionManagerForTests;
import com.gs.fw.common.mithra.test.MithraTestResource;

public abstract class AbstractReladomoTest {
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractReladomoTest.class);
    private MithraTestResource mithraTestResource;

    protected abstract String[] getTestDataFilenames();

    protected String getMithraConfigXmlFilename() {
        return "reladomo/config/TestReladomoRuntimeConfiguration.xml";
    }

    @BeforeEach
    public void setUp() {
        LOGGER.info("Setting up reladomo on h2");
        this.mithraTestResource = new MithraTestResource(this.getMithraConfigXmlFilename());
        ConnectionManagerForTests connectionManager = ConnectionManagerForTests.getInstanceForDbName("testdb");
        this.mithraTestResource.createSingleDatabase(connectionManager);
        for (String filename : this.getTestDataFilenames()) {
            this.mithraTestResource.addTestDataToDatabase(filename, connectionManager);
        }
        this.mithraTestResource.setUp();
    }

    @AfterEach
    public void tearDown() {
        this.mithraTestResource.tearDown();
    }
}