package com.examplesql.talend.components.output.statement.operations;

import com.examplesql.talend.components.source.AvalancheBulkMapperConfiguration;
import com.examplesql.talend.components.output.AvalancheSchema;
import com.examplesql.talend.components.output.Field;
import com.examplesql.talend.components.output.Table;
import com.examplesql.talend.components.output.statement.QueryManager;
import com.examplesql.talend.components.service.AvalancheComponentBulkService;
import com.examplesql.talend.components.service.I18nMessage;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.joining;


public abstract class QueryManagerImpl implements QueryManager {

    public static final transient Logger LOG = LoggerFactory.getLogger(QueryManagerImpl.class);

    private final AvalancheBulkMapperConfiguration configuration;

    private final I18nMessage i18n;

    private final Integer maxRetry = 10;

    private Integer retryCount = 0;

    private Boolean isExternalTableCreated = false;

    protected Boolean isCreateTable = false;
    protected Boolean isDropTable = false;
    protected Boolean isCreateNotExists = false;
    protected Boolean isDropExists = false;

    private Table externalTable;
    private Table table;

    private int varcharLength = 255;

//    abstract protected String buildQuery(Table table);
//
//    abstract protected Map<Integer, Schema.Entry> getQueryParams();
//
//    abstract protected boolean validateQueryParam(Record record);
//
//    abstract public void load(final Connection connection) throws SQLException;
    abstract public  void setConfigurations();

    public QueryManagerImpl(final I18nMessage i18n, final AvalancheBulkMapperConfiguration configuration)
    {
        this.i18n = i18n;
        this.configuration = configuration;
    }

    public AvalancheBulkMapperConfiguration getConfiguration() { return this.configuration; }


    public I18nMessage getI18n() { return this.i18n; }
    @Override
    public int execute(final AvalancheComponentBulkService.DataSource dataSource, AvalancheSchema  schema) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {

            setConfigurations();

            LOG.debug(new Gson().toJson(configuration));

            if(varcharLength <= -1){
                this.varcharLength = varcharLength;
            }

            String externalTableName = UUID.randomUUID().toString();
            externalTableName = externalTableName.replaceAll("[-\\d]+", "");
            this.table =  getTableModel(configuration.getDataset().getTableName(), schema);
            this.externalTable = getTableModel(externalTableName, schema);
            //Load data to External Table
            executeUpdate(connection, createExternalTableQuery(externalTable));
            this.isExternalTableCreated  = true;

            if(isDropTable){
                executeUpdate(connection, dropTableQuery(table));
            }
            if(isCreateTable) {
                executeUpdate(connection,
                   createTableQuery(table));
            }

            //Load data to Table from External Table
            return executeInsertUpdate(connection, insertIntoQuery(table, externalTable));

        } finally {
            if(isExternalTableCreated){
                try (final Connection connection = dataSource.getConnection()) {
                    executeUpdate(connection, dropTableQuery(externalTable));
                    this.isExternalTableCreated = false;
                }
            }
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

    private int executeInsertUpdate(Connection connection, String sql) throws SQLException {
        int records = 0;
        try (final Statement statement = connection.createStatement()) {
            records = statement.executeUpdate(sql);

            connection.commit();
        } catch (final Throwable e) {
            connection.rollback();
            if (!isTableExistsCreationError(e)) {
                throw new IllegalStateException(e);
            }

            LOG.trace("create table issue was ignored. The table and it's name space has been created by an other worker", e);
        }

        return records;
    }


    private String createExternalTableQuery(Table table){
        AvalancheBulkMapperConfiguration.Format fileFormat = configuration.getFormat();
        final StringBuilder sql = new StringBuilder("CREATE EXTERNAL TABLE");
        sql.append(" ");
//        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
//            sql.append(table.getSchema()).append(".");
//        }
        sql.append(table.getName());
        sql.append("(");
        sql.append(createExternalColumns(table.getColumns()));
        sql.append(")");
        sql.append(" ");
        sql.append("USING SPARK");
        sql.append(" ");
        sql.append("WITH REFERENCE=");
        sql.append(identifier(awsS3Path(configuration.getS3Bucket(), configuration.getKey())));
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
//        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
//            sql.append(table.getSchema()).append(".");
//        }
        sql.append(table.getName());
        sql.append("(");
        sql.append(createColumns(table.getColumns()));
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




    protected Table getTableModel(final String name,
                                  AvalancheSchema schema) {
        final Table.TableBuilder builder = Table.builder().name(name);

        return builder.columns(schema.getFields()).build();
    }

    protected String createColumns(final List<Field> fields) {
        return fields.stream().map(this::createColumn).collect(joining(","));
    }
    protected String createExternalColumns(final List<Field> fields) {
        return fields.stream().map(this::createExternalColumn).collect(joining(","));
    }
    protected String createSchemaColumns(final List<Field> fields)  {
        return fields.stream().map(this::createSchemaColumn).collect(joining(","));
    }

    private String createColumn(final Field field) {
        return field.getColumnName()//
                + " " +toDBType(field)//
                + " " + isRequired(field)//
                ;
    }

    private String createExternalColumn(final Field field) {
        return field.getColumnName()//
                + " " + toDBType(field)
                + " " + isRequired(field)//
                ;
    }

    private String createSchemaColumn(final Field field) {
        return field.getColumnName()//
                + " " + toSparkType(field)
                + " " + isSparkRequired(field)//
                ;
    }

    private String isRequired(final Field field) {
        return field.getNullable()? "WITH NULL" : "NOT NULL";
    }
    private String isSparkRequired(final Field field) {
        return field.getNullable()? "" : "NOT NULL";
    }


    private String toDBType(final Field field) {
        switch (field.getType()) {
            case VARCHAR:
                return field.getLength() <= -1 ?  "VARCHAR("+this.varcharLength+")"
                        : "VARCHAR(" +field.getLength() + ")";
            case DECIMAL:
                return "FLOAT";
//            case ANSIDATE:
//            case TIME:
//            case TIMESTAMP:
//                return "VARCHAR(20)";
            default:
               return "" + field.getType();
        }
    }

    private String toSparkType(final Field field) {
        switch (field.getType()){
            case TINYINT:
            case SMALLINT:
            case INTEGER:
                return "integer";
            case BIGINT:
                return  "long";
            case DECIMAL:
            case FLOAT:
                return "double";
            case ANSIDATE:
                return "date";
            case TIME:
                return "time";
            case TIMESTAMP:
                return  "timestamp";
            case BOOLEAN:
                return "boolean";
            case VARCHAR:
            case CHAR:
            default:
                return "string";

        }
    }


    protected boolean isTableExistsCreationError(Throwable e) {
        return false;
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

    protected String delimiterOptions(List<AvalancheBulkMapperConfiguration.Settings> settings){
        if(settings.size() > 0){
            StringBuilder sb = new StringBuilder();
            for (AvalancheBulkMapperConfiguration.Settings setting : settings){
                sb.append(addOption(setting.getParameter().label, setting.getValue()));
            }
            return sb.toString();
        }
        return "";

    }
}
