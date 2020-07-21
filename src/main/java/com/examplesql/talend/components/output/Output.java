package com.examplesql.talend.components.output;

import com.examplesql.talend.components.output.statement.QueryManagerFactory;
import com.examplesql.talend.components.output.statement.operations.QueryManagerImpl;
import com.examplesql.talend.components.service.AvalancheComponentBulkService;
import com.examplesql.talend.components.service.I18nMessage;
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//import com.examplesql.talend.components.output.statement.QueryManager;
//import com.examplesql.talend.components.output.statement.QueryManagerFactory;
//import com.examplesql.talend.components.output.statement.operations.QueryManagerImpl;

@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = Icon.IconType.CUSTOM, custom = "Avalanche") // you can use a custom one using @Icon(value=CUSTOM, custom="filename") and adding icons/filename.svg in resources
@Processor(name = "BulkExec")
@Documentation("TODO fill the documentation for this processor")
public class Output implements Serializable {
    private final OutputConfiguration configuration;
    private final AvalancheComponentBulkService service;
    private final I18nMessage i18nMessage;
    private transient List<Record> records;
    private final QueryManagerImpl queryManager;
    private transient AvalancheComponentBulkService.DataSource datasource;
    private static final transient Logger LOG = LoggerFactory.getLogger(Output.class);
    private Boolean tableExistsCheck;

    private boolean tableCreated;

    private transient boolean init;


    public Output(@Option("configuration") final OutputConfiguration configuration,
                  final AvalancheComponentBulkService service,
                  final I18nMessage i18nMessage) {
        this.configuration = configuration;
        this.service = service;
        this.i18nMessage = i18nMessage;
        queryManager = QueryManagerFactory.getQueryManager(i18nMessage, configuration);
    }

    @PostConstruct
    public void init()  throws  SQLException{
        // this method will be executed once for the whole component execution,
        // this is where you can establish a connection for instance
        // Note: if you don't need it you can delete it
        lazyInit();
    }

    @BeforeGroup
    public void beforeGroup() {
        // if the environment supports chunking this method is called at the beginning if a chunk
        // it can be used to start a local transaction specific to the backend you use
        // Note: if you don't need it you can delete it
        this.records = new ArrayList<>();
    }

    @ElementListener
    public void onNext(
            @Input final Record defaultInput) throws SQLException{
        // this is the method allowing you to handle the input(s) and emit the output(s)
        // after some custom logic you put here, to send a value to next element you can use an
        // output parameter and call emit(value).
        if (!init) {
            // prevent creating db connection if no records
            // it's mostly useful for streaming scenario
            lazyInit();
        }
        records.add(defaultInput);
    }

    private void lazyInit() throws SQLException {
        this.init = true;
        this.datasource = service.createDataSource(configuration.getDataset().getDatastore());
        if (this.tableExistsCheck == null) {
            this.tableExistsCheck = service.checkTableExistence(configuration.getDataset().getTableName(), datasource);
        }
//        if (!this.tableExistsCheck && !this.configuration.isCreateTableIfNotExists()) {
//            throw new IllegalStateException(this.i18nMessage.errorTaberDoesNotExists(this.configuration.getDataset().getTableName()));
//        }

    }

    @AfterGroup
    public void afterGroup() throws SQLException{
        // symmetric method of the beforeGroup() executed after the chunk processing
        // Note: if you don't need it you can delete it
//        if (!tableExistsCheck && !tableCreated
////                && configuration.isCreateTableIfNotExists()
//                ) {
//            try (final Connection connection = datasource.getConnection()) {
//                service.createTableIfNotExist(connection, configuration.getDataset().getTableName(),
////                        configuration.getKeys(),
//                        configuration.getVarcharLength(), records);
//                tableCreated = true;
//            }
//        }

//        // TODO : handle discarded records
        try {
            queryManager.execute(records, datasource);
            records.clear();
//            discards.stream().map(Object::toString).forEach(LOG::error);
        } catch (final SQLException  e) {
//            records.stream().map(r -> new Reject(e.getMessage(), r)).map(Reject::toString).forEach(LOG::error);
            throw new IllegalStateException(e);
        }
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
//        try {
//            if (records.size() > 0)
//                queryManager.execute(records, datasource);
//            if (datasource != null) {
//
//                    queryManager.load(datasource.getConnection());
//                    datasource.close();
//
//            }
//        } catch (Exception e) {
//            LOG.error(e.getMessage());
//        }
    }
}