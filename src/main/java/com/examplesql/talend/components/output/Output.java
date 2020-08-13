package com.examplesql.talend.components.output;

import com.examplesql.talend.components.service.AvalancheComponentBulkService;
import com.examplesql.talend.components.service.I18nMessage;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.AfterGroup;
import org.talend.sdk.component.api.processor.BeforeGroup;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = Icon.IconType.CUSTOM, custom = "AvalancheStop") // you can use a custom one using @Icon(value=CUSTOM, custom="filename") and adding icons/filename.svg in resources
@Processor(name = "Stop")
@Documentation("TODO fill the documentation for this processor")
public class Output implements Serializable {

    private final OutputConfiguration configuration;
    private final AvalancheComponentBulkService service;
    private transient List<Record> records;
    private static final transient Logger LOG = LoggerFactory.getLogger(Output.class);

    public Output(@Option("configuration") final OutputConfiguration configuration,
                  final AvalancheComponentBulkService service) {
        this.configuration = configuration;
        this.service = service;

    }

    @PostConstruct
    public void init() throws SQLException {
        // this method will be executed once for the whole component execution,
        // this is where you can establish a connection for instance
        // Note: if you don't need it you can delete it

    }

    @BeforeGroup
    public void beforeGroup() {
        // if the environment supports chunking this method is called at the beginning if a chunk
        // it can be used to start a local transaction specific to the backend you use
        // Note: if you don't need it you can delete it
        this.records = new ArrayList<>();

    }

    @ElementListener
    public void onNext(@Input final Record defaultInput) throws SQLException {
        // this is the method allowing you to handle the input(s) and emit the output(s)
        // after some custom logic you put here, to send a value to next element you can use an
        // output parameter and call emit(value).



        records.add(defaultInput);
        LOG.debug("Number of records " + new Gson().toJson(records));
    }

    @AfterGroup
    public void afterGroup() throws SQLException{
        records.clear();
    }


}