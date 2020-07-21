package com.examplesql.talend.components.datastore;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.constraint.Min;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;


@DataStore("AvalancheDatastore")
@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "host", "port" }),
    @GridLayout.Row({ "database", "schema" }),
    @GridLayout.Row({ "username", "password"})
})
//@GridLayout(names = GridLayout.FormType.ADVANCED, value = { @GridLayout.Row("connectionTimeOut"),
//        @GridLayout.Row("connectionValidationTimeOut") })
@Checkable("validateConnection")
@Documentation("Connection to a ExampleSQL Database")
public class AvalancheDatastore implements Serializable {

    @Option
    @Required
    @Documentation("FQDN for the Avalanche Cluster")
    private String host;

    @Option
    @Required
    @Documentation("Port number for the Avalanche Cluster")
    private String port;

    @Option
    @Required
    @Documentation("Database name in Avalanche")
    private String database;

    @Option
    @Documentation("Schema name in Avalanche")
    private String schema;

    @Option
    @Required
    @Documentation("Avalanche connection userid")
    private String username;

    @Credential
    @Option
    @Documentation("Avalanche connection password")
    private String password;


    public String getHost() {
        return host;
    }

    public AvalancheDatastore setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public AvalancheDatastore setPort(String port) {
        this.port = port;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public AvalancheDatastore setDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public AvalancheDatastore setSchema(String schema) {
        this.schema = schema;
        return this;
    }


    public String getUsername() {
        return username;
    }

    public AvalancheDatastore setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AvalancheDatastore setPassword(String password) {
        this.password = password;
        return this;
    }


}