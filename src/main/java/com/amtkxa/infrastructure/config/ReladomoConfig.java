package com.amtkxa.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import com.amtkxa.infrastructure.database.DBConnectionManager;
import com.amtkxa.infrastructure.database.DatasourceProperties;
import com.gs.fw.common.mithra.MithraManager;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.mithraruntime.MithraRuntimeType;
import com.gs.fw.common.mithra.mithraruntime.PropertyType;

/**
 * Initialize Reladomo
 *
 * <p>Reladomo's runtime is configured via a MithraRuntime XML file. Reladomo will cache an object
 * as long as there is sufficient memory. When there is a memory crunch, the cached objects will be
 * garbage-collected.
 */
@Configuration
public class ReladomoConfig {
    private static int MAX_TRANSACTION_TIMEOUT = 60 * 1000; // (seconds)
    private final DatasourceProperties properties;

    public ReladomoConfig(DatasourceProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void postConstruct() {
        initializeReladomo();
        loadReladomoXMLFromClasspath("reladomo/config/ReladomoRuntimeConfiguration.xml");
    }

    /**
     * Initialize Reladomo using MithraManager class.
     */
    public void initializeReladomo() {
        MithraManager mithraManager = MithraManagerProvider.getMithraManager();
        mithraManager.setTransactionTimeout(MAX_TRANSACTION_TIMEOUT);
    }

    /**
     * Load Reladomo runtime configuration file.
     *
     * @param fileName ReladomoRuntime XML file
     */
    private void loadReladomoXMLFromClasspath(String fileName) {
        try (InputStream is = ReladomoConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            MithraRuntimeType runtimeType = MithraManagerProvider.getMithraManager().parseConfiguration(is);
            addProperties(runtimeType);
            MithraManagerProvider.getMithraManager().initializeRuntime(runtimeType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to locate " + fileName + " in classpath");
        }
    }

    private void addProperties(MithraRuntimeType mithraRuntimeType) {
        List<PropertyType> props = mithraRuntimeType.getConnectionManagers().get(0).getProperties();
        props.add(makePropertyType(DBConnectionManager.JDBC_DRIVER_CLASS_KEY, properties.getJdbcDriverClass()));
        props.add(makePropertyType(DBConnectionManager.URL_KEY, properties.getUrl()));
        props.add(makePropertyType(DBConnectionManager.TIMEZONE_KEY, properties.getTimeZone()));
        props.add(makePropertyType(DBConnectionManager.HOST_KEY, properties.getHost()));
        props.add(makePropertyType(DBConnectionManager.PORT_KEY, properties.getPort()));
        props.add(makePropertyType(DBConnectionManager.DATABASE_KEY, properties.getDatabase()));
        props.add(makePropertyType(DBConnectionManager.USERNAME_KEY, properties.getUsername()));
        props.add(makePropertyType(DBConnectionManager.PASSWORD_KEY, properties.getPassword()));
    }

    private PropertyType makePropertyType(String name, String value) {
        PropertyType propertyType = new PropertyType();
        propertyType.setName(name);
        propertyType.setValue(value);
        return propertyType;
    }
}
