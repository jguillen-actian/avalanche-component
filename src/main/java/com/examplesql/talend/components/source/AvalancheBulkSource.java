package com.examplesql.talend.components.source;

import com.examplesql.talend.components.output.AvalancheSchema;
import com.examplesql.talend.components.output.Field;
import com.examplesql.talend.components.output.statement.QueryManagerFactory;
import com.examplesql.talend.components.output.statement.operations.QueryManagerImpl;
import com.examplesql.talend.components.service.AvalancheComponentBulkService;
import com.examplesql.talend.components.service.I18nMessage;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.talend.sdk.component.api.record.Schema.Type.INT;
import static org.talend.sdk.component.api.record.Schema.Type.RECORD;

@Documentation("TODO fill the documentation for this source")
public class AvalancheBulkSource implements Serializable{
    private static final Pattern validTableName = Pattern.compile("[a-zA-Z0-9$_.]{3,}");
    private final AvalancheBulkMapperConfiguration configuration;
    private final AvalancheComponentBulkService service;
    private final I18nMessage i18nMessage;
    private transient List<Record> records;
    private final QueryManagerImpl queryManager;
    private transient AvalancheComponentBulkService.DataSource datasource;
    private static final transient Logger LOG = LoggerFactory.getLogger(AvalancheBulkSource.class);
    private Boolean tableExistsCheck;
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private boolean isGuessSchema = false;
    private transient AvalancheSchema schema;
    private final RecordBuilderFactory builderFactory;

    private transient boolean init;
    private transient boolean execute;


    public AvalancheBulkSource(@Option("configuration") final AvalancheBulkMapperConfiguration configuration,
                  final AvalancheComponentBulkService service,
                  final RecordBuilderFactory recordBuilderFactory,
                  final I18nMessage i18nMessage) {
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = recordBuilderFactory;
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

    @Producer
    public Record next() throws SQLException {

        if(execute){
            return null;
        }
        int records = queryManager.execute(datasource, schema);
        LOG.debug("records: " + records);
        execute = true;
        final Schema.Builder schemaBuilder= builderFactory.newSchemaBuilder(RECORD);
        final Schema.Entry.Builder entryBuilder = builderFactory.newEntryBuilder();
        Schema sc = schemaBuilder.withEntry(entryBuilder.withName("records").withType(INT).build()).build();

        final Record.Builder recordBuilder = builderFactory.newRecordBuilder(sc);

        Record record =  recordBuilder.withInt(entryBuilder.withName("records").withType(INT).build(),  records).build();
        LOG.debug("Put in record: ", new Gson().toJson(record));

        return record;
    }

    private void lazyInit() throws SQLException {
        this.init = true;
        this.execute = false;
        this.datasource = service.createDataSource(configuration.getDataset().getDatastore());

        String table = configuration.getDataset().getTableName();
        if (!validTableName.matcher(configuration.getDataset().getTableName().trim()).matches())
            throw new SQLException("Possbile SQL Injection Detected for table name " + table);
        if (this.tableExistsCheck == null) {
            this.tableExistsCheck = service.checkTableExistence(configuration.getDataset().getTableName(), datasource);
        }

        if (this.tableExistsCheck && configuration.getUseExistingTableSchema()) {
            try {
                connection = datasource.getConnection();
                statement = connection.createStatement();
                statement.setFetchSize(1);

                LOG.debug("###select * from " + configuration.getDataset().getTableName());
                resultSet = statement.executeQuery("select * from " + configuration.getDataset().getTableName());
                LOG.debug("metadata : " + new Gson().toJson(resultSet.getMetaData()));

                final ResultSetMetaData metaData = resultSet.getMetaData();
                final List<Field> fields = new ArrayList<>();
                LOG.debug("metadata: " + new Gson().toJson( metaData));
                IntStream.rangeClosed(1, metaData.getColumnCount()).forEach(index -> service.addField(fields, metaData, index));

                schema = AvalancheSchema.builder().fields(fields).build();


            } catch (final SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                try {
                    connection.rollback();
                } catch (final SQLException rollbackError) {
                    LOG.error(i18nMessage.errorSQL(rollbackError.getErrorCode(), rollbackError.getMessage()), rollbackError);
                }
            }
        } else {
            schema = service.convert(configuration.getSchema());
        }
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
//        if (statement != null) {
//            try {
//                statement.close();
//            } catch (SQLException e) {
//                LOG.warn(i18nMessage.warnStatementCantBeClosed(), e);
//            }
//        }
//        if (connection != null) {
//            try {
//                connection.close();
//            } catch (SQLException e) {
//                LOG.warn(i18nMessage.warnConnectionCantBeClosed(), e);
//            }
//        }
//        if (datasource != null) {
//            try {
//                datasource.close();
//            } catch (Exception e) {
//                LOG.error(e.getMessage());
//            }
//        }
    }

//    @DiscoverSchema
//    public Schema guessSchema(@Option AvalancheTableDataset dataset) {
//        try {
//            connection = datasource.getConnection();
//            statement = connection.createStatement();
//            statement.setFetchSize(1);
//
//            LOG.debug("###select * from " + dataset.getTableName());
//            ResultSet resultSet = statement.executeQuery("select * from " + dataset.getTableName());
//            LOG.debug("metadata : " + new Gson().toJson(resultSet.getMetaData()));
//
//            final ResultSetMetaData metaData = resultSet.getMetaData();
//            final List<Field> fields = new ArrayList<>();
//            LOG.debug("metadata: " + new Gson().toJson( metaData));
//            IntStream.rangeClosed(1, metaData.getColumnCount()).forEach(index -> service.addField(fields, metaData, index));
//
//            AvalancheSchema schema = AvalancheSchema.builder().fields(fields).build();
//
//            configuration.setSchema(service.schemaToColumns(schema));
//            return null;
//        } catch (final SQLException e) {
//            throw new IllegalStateException(e);
//        }
//    }



}