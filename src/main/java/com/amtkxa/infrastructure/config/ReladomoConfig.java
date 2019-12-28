package com.amtkxa.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.amtkxa.common.exception.ReladomoConfigurationException;
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
    private static Logger log = LoggerFactory.getLogger(ReladomoConfig.class.getName());
    private static int MAX_TRANSACTION_TIMEOUT = 60 * 1000; // (seconds)
    private final DatasourceProperties properties;

    public ReladomoConfig(DatasourceProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void postConstruct() {
        initializeReladomo();
        loadReladomoXMLFromClasspath(properties.getRuntimeConfigurationFile());
    }

    /**
     * Initialize Reladomo using MithraManager class.
     */
    public void initializeReladomo() {
        log.info("Transaction Timeout is " + MAX_TRANSACTION_TIMEOUT);
        MithraManager mithraManager = MithraManagerProvider.getMithraManager();
        mithraManager.setTransactionTimeout(MAX_TRANSACTION_TIMEOUT);
        log.info("Reladomo has been initialised.");
    }

    /**
     * Load Reladomo runtime configuration file.
     *
     * @param filePath ReladomoRuntime XML file
     */
    private void loadReladomoXMLFromClasspath(String filePath) {
        try (InputStream is = ReladomoConfig.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IOException("Reladomo configuration file not found. filePath: " + filePath);
            }
            MithraRuntimeType runtimeType = MithraManagerProvider.getMithraManager().parseConfiguration(is);
            addProperties(runtimeType);
            MithraManagerProvider.getMithraManager().initializeRuntime(runtimeType);
            log.info("Reladomo configuration file {} is now loaded. Connecting to {} ",
                     filePath, properties.getUrl());
        } catch (Exception e) {
            throw new ReladomoConfigurationException("Failed to initialize reladomo. " + e.getMessage());
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
