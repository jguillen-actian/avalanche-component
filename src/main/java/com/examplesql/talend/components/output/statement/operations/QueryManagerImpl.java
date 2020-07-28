package com.examplesql.talend.components.output.statement.operations;

import com.examplesql.talend.components.output.Column;
import com.examplesql.talend.components.output.OutputConfiguration;
import com.examplesql.talend.components.output.Reject;
import com.examplesql.talend.components.output.Table;
import com.examplesql.talend.components.output.statement.QueryManager;
import com.examplesql.talend.components.service.AvalancheComponentBulkService;
import com.examplesql.talend.components.service.I18nMessage;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.talend.sdk.component.api.record.Schema.Type.DATETIME;
import static org.talend.sdk.component.api.record.Schema.Type.STRING;

public abstract class QueryManagerImpl implements QueryManager {

    public static final transient Logger LOG = LoggerFactory.getLogger(QueryManagerImpl.class);

    private final OutputConfiguration configuration;

    private final I18nMessage i18n;

    private final Integer maxRetry = 10;

    private Integer retryCount = 0;

    private Boolean isExternalTableCreated = false;

    protected Boolean isCreateTable = false;
    protected Boolean isDropTable = false;
    protected Boolean isCreateNotExists = false;
    protected Boolean isDropExists = false;

//    abstract protected String buildQuery(Table table);
//
//    abstract protected Map<Integer, Schema.Entry> getQueryParams();
//
//    abstract protected boolean validateQueryParam(Record record);
//
//    abstract public void load(final Connection connection) throws SQLException;
    abstract public  void setConfigurations();

    public QueryManagerImpl(final I18nMessage i18n, final OutputConfiguration configuration)
    {
        this.i18n = i18n;
        this.configuration = configuration;
    }

    public OutputConfiguration getConfiguration() { return this.configuration; }


    public I18nMessage getI18n() { return this.i18n; }
    @Override
    public void execute(final List<Record> records, final AvalancheComponentBulkService.DataSource dataSource) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
//            return processRecords(records, connection, buildQuery(getTableModel(connection, configuration.getDataset().getTableName(),
////                keys,
//                    configuration.getVarcharLength(), records));
            setConfigurations();

            LOG.debug(new Gson().toJson(configuration));

            String externalTableName = UUID.randomUUID().toString();
            externalTableName = externalTableName.replaceAll("[-\\d]+", "");
            Table table =  getTableModel(connection,
                    configuration.getDataset().getTableName(),
//                    configuration.getKeys(),
                    configuration.getVarcharLength(),
                    records);
            Table externalTable = getTableModel(connection,
                    externalTableName,
//                    configuration.getKeys(),
                    configuration.getVarcharLength(),
                    records);

            //Load data to External Table
            executeUpdate(connection, createExternalTableQuery(externalTable));
            isCreateNotExists = true;

            if(isDropTable){
                executeUpdate(connection, dropTableQuery(table));
            }
            if(isCreateTable) {
                executeUpdate(connection,
                   createTableQuery(table));
            }

            //Load data to Table from External Table
            executeUpdate(connection, insertIntoQuery(table, externalTable));

            executeUpdate(connection, dropTableQuery(externalTable));
            isExternalTableCreated = true;

        }

    }

    private void executeUpdate(Connection connection, String sql) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            connection.commit();
        } catch (final Throwable e) {
            connection.rollback();
            if (!isTableExistsCreationError(e)) {
                throw new IllegalStateException(e);
            }

            LOG.trace("create table issue was ignored. The table and it's name space has been created by an other worker", e);
        }
    }


    private String createExternalTableQuery(Table table){
        OutputConfiguration.Format fileFormat = configuration.getFormat();
        final StringBuilder sql = new StringBuilder("CREATE EXTERNAL TABLE");
        sql.append(" ");
        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            sql.append(table.getSchema()).append(".");
        }
        sql.append(table.getName());
        sql.append("(");
        sql.append(createExternalColumns(table.getColumns()));
        sql.append(createPKs(table.getName(),
                table.getColumns().stream().filter(Column::isPrimaryKey).collect(Collectors.toList())));
        sql.append(")");
        sql.append(" ");
        sql.append("USING SPARK");
        sql.append(" ");
        sql.append("WITH REFERENCE=");
        sql.append(identifier(awsS3Path(configuration.getS3Bucket(), configuration.getFilename())));
        sql.append(",");
        sql.append(" ");
        sql.append("FORMAT=");
        sql.append(identifier(fileFormat.label));
        switch (fileFormat)  {
            case CSV: {
                sql.append(",");
                sql.append(" ");
                sql.append("OPTIONS=(");
                sql.append(addOption("header", String.valueOf(configuration.getHeader())).substring(1));
                sql.append(addOption("sep", configuration.getFieldDelimiter()));
                sql.append(addOption("schema", createSchemaColumns(table.getColumns())));
                if (configuration.getDateFormat() != null && configuration.getDateFormat() != "") {
                    sql.append((addOption("dateFormat", configuration.getDateFormat())));
                }
                if (configuration.getTimestampFormat() != null && configuration.getTimestampFormat() != "") {
                    sql.append((addOption("timestampFormat", configuration.getTimestampFormat())));
                }
                //Add extra delimiter options.
                sql.append(delimiterOptions(configuration.getSettings()));
                sql.append(")");
            }

        }

        LOG.debug("### create external table query ###");
        LOG.debug(sql.toString());
        return sql.toString();
    }

    private String insertIntoQuery(Table table, Table externalTable){
        final StringBuilder sql = new StringBuilder("INSERT INTO");
        sql.append(" ");
        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            sql.append(table.getSchema()).append(".");
        }
        sql.append(table.getName());
        sql.append(" ");
        sql.append("SELECT * FROM ");
        if (externalTable.getSchema() != null && !externalTable.getSchema().isEmpty()) {
            sql.append(externalTable.getSchema()).append(".");
        }
        sql.append(externalTable.getName());

        LOG.debug("### insert from external table query ###");
        LOG.debug(sql.toString());
        return  sql.toString();
    }

    private String createTableQuery(Table table){
        final StringBuilder sql = new StringBuilder("CREATE TABLE");
        sql.append(" ");
        if(isCreateNotExists){
            sql.append("IF NOT EXISTS");
            sql.append(" ");
        }
        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            sql.append(table.getSchema()).append(".");
        }
        sql.append(table.getName());
        sql.append("(");
        sql.append(createColumns(table.getColumns()));
        sql.append(createPKs(table.getName(),
                table.getColumns().stream().filter(Column::isPrimaryKey).collect(Collectors.toList())));
        sql.append(")");
        sql.append(" WITH NOPARTITION");
        // todo create index

        LOG.debug("### create table query ###");
        LOG.debug(sql.toString());
        return sql.toString();
    }

    private String dropTableQuery(Table table){
        final StringBuilder sql = new StringBuilder("DROP TABLE");
        sql.append(" ");
        if(isDropExists){
            sql.append("IF EXISTS");
            sql.append(" ");
        }
        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            sql.append(table.getSchema()).append(".");
        }
        sql.append(table.getName());
        LOG.debug(sql.toString());

        LOG.debug("### drop table query ###");
        return sql.toString();
    }

    private List<Reject> processRecords(final List<Record> records, final Connection connection, final String query)
            throws SQLException {
        List<Reject> rejects;
        do {
            rejects = new ArrayList<>();
            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                final Map<Integer, Integer> batchOrder = new HashMap<>();
                int recordIndex = -1;
                int batchNumber = -1;
//                for (final Record record : records) {
//                    recordIndex++;
//                    statement.clearParameters();
//                    if (!validateQueryParam(record)) {
//                        rejects.add(new Reject("missing required query param in this record", record));
//                        continue;
//                    }
//                    for (final Map.Entry<Integer, Schema.Entry> entry : getQueryParams().entrySet()) {
//                        RecordToSQLTypeConverter.valueOf(entry.getValue().getType().name()).setValue(statement, entry.getKey(),
//                                entry.getValue(), record);
//                    }
//                    statement.addBatch();
//                    batchNumber++;
//                    batchOrder.put(batchNumber, recordIndex);
//                }

                try {
                    statement.execute();
                    connection.commit();
                    break;
                } catch (final SQLException e) {
                    if (!connection.getAutoCommit()) {
                        connection.rollback();
                    }
                    if (!retry(e) || retryCount > maxRetry) {
                        rejects.addAll(handleRejects(records, batchOrder, e));
                        break;
                    }
                    retryCount++;
                    LOG.warn("Deadlock detected. retrying for the " + retryCount + " time", e);
                    try {
                        Thread.sleep((long) Math.exp(retryCount) * 2000);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } while (true);

        return rejects;
    }

    private boolean retry(final SQLException e) {
        return "40001".equals(ofNullable(e.getNextException()).orElse(e).getSQLState());
    }

    private List<Reject> handleRejects(final List<Record> records, Map<Integer, Integer> batchOrder, final SQLException e)
            throws SQLException {
        if (!(e instanceof BatchUpdateException)) {
            throw e;
        }
        final List<Reject> discards = new ArrayList<>();
        final int[] result = ((BatchUpdateException) e).getUpdateCounts();
        SQLException error = e;
        if (result.length == records.size()) {
            for (int i = 0; i < result.length; i++) {
                if (result[i] == Statement.EXECUTE_FAILED) {
                    error = ofNullable(error.getNextException()).orElse(error);
                    discards.add(new Reject(error.getMessage(), error.getSQLState(), error.getErrorCode(),
                            records.get(batchOrder.get(i))));
                }
            }
        } else {
            int failurePoint = result.length;
            error = ofNullable(error.getNextException()).orElse(error);
            discards.add(new Reject(error.getMessage(), error.getSQLState(), error.getErrorCode(),
                    records.get(batchOrder.get(failurePoint))));
            // todo we may retry for this sub list
            discards.addAll(records.subList(batchOrder.get(failurePoint) + 1, records.size()).stream()
                    .map(r -> new Reject("rejected due to error in previous elements error in this transaction", r))
                    .collect(toList()));
        }

        return discards;
    }

    public String namespace(final Connection connection) throws SQLException {
        return (connection.getCatalog() != null && !connection.getCatalog().isEmpty()
                ? connection.getCatalog() + "."
                : "")
                + (connection.getSchema() != null && !connection.getSchema().isEmpty()
                ? connection.getSchema()
                : "");
    }

    public static Optional<Object> valueOf(final Record record, final Schema.Entry entry) {
        switch (entry.getType()) {
            case INT:
                return record.getOptionalInt(entry.getName()).isPresent() ? of(record.getOptionalInt(entry.getName()).getAsInt())
                        : empty();
            case LONG:
                return record.getOptionalLong(entry.getName()).isPresent() ? of(record.getOptionalLong(entry.getName()).getAsLong())
                        : empty();
            case FLOAT:
                return record.getOptionalFloat(entry.getName()).isPresent()
                        ? of(record.getOptionalFloat(entry.getName()).getAsDouble())
                        : empty();
            case DOUBLE:
                return record.getOptionalDouble(entry.getName()).isPresent()
                        ? of(record.getOptionalDouble(entry.getName()).getAsDouble())
                        : empty();
            case BOOLEAN:
                return record.getOptionalBoolean(entry.getName()).isPresent() ? of(record.getOptionalBoolean(entry.getName()).get())
                        : empty();
            case BYTES:
                return record.getOptionalBytes(entry.getName()).isPresent() ? of(record.getOptionalBytes(entry.getName()).get())
                        : empty();
            case DATETIME:
                return record.getOptionalDateTime(entry.getName()).isPresent() ? of(record.getOptionalDateTime(entry.getName()).get())
                        : empty();
            case STRING:
                return record.getOptionalString(entry.getName()).isPresent() ? of(record.getOptionalString(entry.getName()).get())
                        : empty();
            case RECORD:
                return record.getOptionalRecord(entry.getName()).isPresent() ? of(record.getOptionalRecord(entry.getName()).get())
                        : empty();
            case ARRAY:
            default:
                return empty();
        }
    }

    protected Table getTableModel(final Connection connection, final String name,
//                                  final List<String> keys,
                                  final int varcharLength, final List<Record> records) {
        final Table.TableBuilder builder = Table.builder().name(name);
        try {
            builder.catalog(connection.getCatalog()).schema(connection.getSchema());
        } catch (final SQLException e) {
            LOG.warn("can't get database catalog or schema", e);
        }
        final List<Schema.Entry> entries = records.stream().flatMap(record -> record.getSchema().getEntries().stream()).distinct()
                .collect(toList());
        return builder.columns(entries.stream()
                .map(entry -> Column.builder().entry(entry)
//                        .primaryKey(keys.contains(entry.getName()))
                        .size(STRING == entry.getType() ? varcharLength : null).build())
                .collect(toList())).build();
    }

    protected String createColumns(final List<Column> columns) {
        return columns.stream().map(this::createColumn).collect(joining(","));
    }
    protected String createExternalColumns(final List<Column> columns) {
        return columns.stream().map(this::createExternalColumn).collect(joining(","));
    }
    protected String createSchemaColumns(final List<Column> columns) {
        return columns.stream().map(this::createSchemaColumn).collect(joining(","));
    }

    private String createColumn(final Column column) {
        return column.getEntry().getName()//
                + " " + toDBType(column)//
                + " " + isRequired(column)//
                ;
    }

    private String createExternalColumn(final Column column) {
        return column.getEntry().getName()//
                + " " + toDBExternalType(column)
                + " " + isRequired(column)//
                ;
    }

    private String createSchemaColumn(final Column column) {
        return column.getEntry().getName()//
                + " " + (column.getEntry().getType() == DATETIME ? STRING: column.getEntry().getType()) //
                ;
    }

    private String isRequired(final Column column) {
        return column.getEntry().isNullable() && !column.isPrimaryKey() ? "NULL" : "NOT NULL";
    }

    private String toDBType(final Column column) {
        switch (column.getEntry().getType()) {
            case STRING:
                return column.getSize() <= -1 ?  "VARCHAR(255)"
                        : "VARCHAR(" + column.getSize() + ")";
            case BOOLEAN:
                return "BOOLEAN";
            case DOUBLE:
                return "DECIMAL";
            case FLOAT:
                return "FLOAT";
            case LONG:
                return "BIGINT";
            case INT:
                return "INTEGER";
            case DATETIME:
                return "TIMESTAMP";
            case BYTES:
            case RECORD:
            case ARRAY:
            default:
                throw new IllegalStateException(this.i18n.errorUnsupportedType(column.getEntry().getType().name(), column.getEntry().getName()));
        }
    }

    private String toDBExternalType(final Column column) {
        switch (column.getEntry().getType()) {
            case STRING:
                return column.getSize() <= -1 ?  "VARCHAR(255)"
                        : "VARCHAR(" + column.getSize() + ")";
            case BOOLEAN:
                return "BOOLEAN";
            case DOUBLE:
                return "DECIMAL";
            case FLOAT:
                return "FLOAT";
            case LONG:
                return "BIGINT";
            case INT:
                return "INTEGER";
            case DATETIME:
                return "VARCHAR(20)";
            case BYTES:
            case RECORD:
            case ARRAY:
            default:
                throw new IllegalStateException(this.i18n.errorUnsupportedType(column.getEntry().getType().name(), column.getEntry().getName()));
        }
    }


    protected boolean isTableExistsCreationError(Throwable e) {
        return false;
    }

    protected String createPKs(final String table, final List<Column> primaryKeys) {
        return primaryKeys == null || primaryKeys.isEmpty() ? ""
                : ", CONSTRAINT " + pkConstraintName(table, primaryKeys) + " PRIMARY KEY "
                + primaryKeys.stream().map(Column::getName).collect(joining(",", "(", ")"));
    }

    private String pkConstraintName(String table, List<Column> primaryKeys) {
        final String uuid = UUID.randomUUID().toString();
        return "pk_" + table + "_" + primaryKeys.stream().map(Column::getName).collect(joining("_")) + "_"
                + uuid.substring(0, Math.min(4, uuid.length()));
    }

    public String awsS3Path(String bucket, String filename){
        final StringBuilder s3 = new StringBuilder("s3a://");
        s3.append(bucket);
        s3.append("/");
        s3.append(filename);

        return s3.toString();
    }

    public String identifier(final String name) {
        return name == null || name.isEmpty() ? name : delimiterToken() + name + delimiterToken();
    }

    protected String delimiterToken() {
        return "'";
    }

    protected String addOption(final  String key, final String value){
        StringBuilder sb = new StringBuilder(",");
        sb.append(" ");
        sb.append(identifier(key));
        sb.append("=");
        sb.append(identifier(value));
        return sb.toString();
    }

    protected String delimiterOptions(List<OutputConfiguration.Settings> settings){
        if(settings.size() > 0){
            StringBuilder sb = new StringBuilder();
            for (OutputConfiguration.Settings setting : settings){
                sb.append(addOption(setting.getParameter().label, setting.getValue()));
            }
            return sb.toString();
        }
        return "";

    }
}
