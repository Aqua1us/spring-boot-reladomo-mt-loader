package com.amtkxa.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.amtkxa.infrastructure.database.DBConnectionManager;
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

    @Value("${datasource.url}")
    private String url;

    @Value("${datasource.host}")
    private String host;

    @Value("${datasource.port}")
    private String port;

    @Value("${datasource.database}")
    private String database;

    @Value("${datasource.username}")
    private String username;

    @Value("${datasource.password}")
    private String password;

    @Value("${datasource.jdbc-driver-class-name}")
    private String jdbcDriverClassName;

    @Value("${datasource.timeZone}")
    private String timeZone;

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
        props.add(makePropertyType(DBConnectionManager.JDBC_DRIVER_CLASS_NAME_KEY, this.jdbcDriverClassName));
        props.add(makePropertyType(DBConnectionManager.URL_KEY, this.url));
        props.add(makePropertyType(DBConnectionManager.TIMEZONE_KEY, this.timeZone));
        props.add(makePropertyType(DBConnectionManager.HOST_KEY, this.host));
        props.add(makePropertyType(DBConnectionManager.PORT_KEY, this.port));
        props.add(makePropertyType(DBConnectionManager.DATABASE_KEY, this.database));
        props.add(makePropertyType(DBConnectionManager.USERNAME_KEY, this.username));
        props.add(makePropertyType(DBConnectionManager.PASSWORD_KEY, this.password));
    }

    private PropertyType makePropertyType(String name, String value) {
        PropertyType propertyType = new PropertyType();
        propertyType.setName(name);
        propertyType.setValue(value);
        return propertyType;
    }
}
