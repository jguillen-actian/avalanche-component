package com.examplesql.talend.components.service;

import com.examplesql.talend.components.dataset.AvalancheTableDataset;
import com.examplesql.talend.components.datastore.AvalancheDatastore;
import com.examplesql.talend.components.source.AvalancheBulkMapperConfiguration;
import com.examplesql.talend.components.output.AvalancheSchema;
import com.examplesql.talend.components.output.Field;
import com.examplesql.talend.components.output.SQLDataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.Suggestions;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.sql.ResultSetMetaData.columnNoNulls;
import static java.util.Optional.ofNullable;

@Service
public class AvalancheComponentBulkService {

    private static final transient Logger LOG = LoggerFactory.getLogger(AvalancheComponentBulkService.class);

    @Service
    private I18nMessage i18n;


    public static boolean checkTableExistence(final String tableName, final DataSource dataSource)
            throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
            try (final ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(),
                    tableName, new String[] { "TABLE", "SYNONYM" })) {
                while (resultSet.next()) {
                    if (ofNullable(ofNullable(resultSet.getString("TABLE_NAME")).orElseGet(() -> {
                        try {
                            return resultSet.getString("SYNONYM_NAME");
                        } catch (final SQLException e) {
                            return null;
                        }
                    })).filter(tableName::equals).isPresent()) {
                        return true;
                    }
                }
                return false;
            }
        }
    }


    @HealthCheck("validateConnection")
    public HealthCheckStatus validateConnection(@Option final AvalancheDatastore datastore)
    {
        DataSource dataSource = createDataSource(datastore);
        try {
            dataSource.createConnection();
            dataSource.testConnection();
            return new HealthCheckStatus(HealthCheckStatus.Status.OK,i18n.successConnection());
        } catch(SQLException e)
        {
            return new HealthCheckStatus(HealthCheckStatus.Status.KO,i18n.errorInvalidConnection());
        }
    }

    public DataSource createDataSource(final AvalancheDatastore datastore)
    {
        return new DataSource(i18n, datastore, false, false);
    }



    public static class DataSource implements AutoCloseable
    {
        private Connection connection;
        private final I18nMessage i18nMessage;
        private final AvalancheDatastore datastore;
        private final boolean isAutoCommit;
        private final boolean rewriteBatchedStatements;
        public DataSource(final I18nMessage i18nMessage, final AvalancheDatastore datastore,
                          final boolean isAutoCommit, final boolean rewriteBatchedStatements)
        {
            this.i18nMessage = i18nMessage;
            this.datastore = datastore;
            this.isAutoCommit = isAutoCommit;
            this.rewriteBatchedStatements = rewriteBatchedStatements;
        }

        private void createConnection()
        {
            LOG.debug("Before finding class name.");
            try {
//                Driver driver = new com.ingres.jdbc.IngresDriver();
//                DriverManager.registerDriver(driver);
//                getClass().getClassLoader().loadClass("com.ingres.jdbc.IngresDriver").newInstance();
                Class.forName( "com.ingres.jdbc.IngresDriver" ).newInstance();
                LOG.debug("After finding class name.");
                connection = DriverManager.getConnection("jdbc:ingres://"+ datastore.getHost() +":"+ datastore.getPort() + "/"+ datastore.getDatabase() + ";",
                        datastore.getUsername(), datastore.getPassword());
                LOG.debug("After connection.");
                connection.setAutoCommit(isAutoCommit);

            } catch (
                    InstantiationException | IllegalAccessException |
                    ClassNotFoundException|
                SQLException e) {
                LOG.error(e.getMessage());
            }
        }

        public Connection getConnection() {
            try {
                if (connection == null || connection.isClosed())
                    createConnection();
            } catch (SQLException e) {
                LOG.error(e.getMessage());
            }

            return this.connection;
        }

        public void testConnection() throws SQLException {
            connection.isClosed();
        }

        @Override
        public void close() throws Exception {
            connection.close();
        }
    }

    @Suggestions("suggestTableColumnNames")
    public SuggestionValues getTableColumns(@Option final AvalancheTableDataset dataset) {

        try (Connection conn = createDataSource(dataset.getDatastore()).getConnection()) {
            try (final Statement statement = conn.createStatement()) {
                statement.setMaxRows(1);
                try (final ResultSet result = statement.executeQuery("select * from " + dataset.getTableName())) {
                    return new SuggestionValues(true,
                            IntStream.rangeClosed(1, result.getMetaData().getColumnCount()).mapToObj(i -> {
                                try {
                                    return result.getMetaData().getColumnName(i);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }).filter(Objects::nonNull).map(columnName -> new SuggestionValues.Item(columnName, columnName))
                                    .collect(Collectors.toSet()));
                }
            }
        } catch (final Exception unexpected) {
            // catch all exceptions for this ui label to return empty list
            LOG.error(i18n.errorCantLoadTableSuggestions(), unexpected);
        }

        return new SuggestionValues(false, Collections.emptyList());
    }

    @Suggestions("actionsList")
    public SuggestionValues getActionOnData(@Option final AvalancheTableDataset dataset) {
        SuggestionValues suggestionValues = new SuggestionValues();
        List<SuggestionValues.Item> items = new ArrayList<>();
        items.add(new SuggestionValues.Item("NONE","NONE"));
        items.add(new SuggestionValues.Item("CREATE","CREATE"));
        items.add(new SuggestionValues.Item("DROP_AND_CREATE","DROP_AND_CREATE"));
        items.add(new SuggestionValues.Item("CREATE_TABLE_IF_NOT_EXISTS","CREATE_TABLE_IF_NOT_EXISTS"));
        items.add(new SuggestionValues.Item("DROP_TABLE_IF_EXISTS_AND_CREATE","DROP_TABLE_IF_EXISTS_AND_CREATE"));

        suggestionValues.setItems(items);

        return suggestionValues;
    }

    @Suggestions("listTables")
    public SuggestionValues getTableFromDatabase(@Option final AvalancheDatastore datastore) {
        final Collection<SuggestionValues.Item> items = new HashSet<>();
        try (Connection connection = createDataSource(datastore).getConnection()) {
            final DatabaseMetaData dbMetaData = connection.getMetaData();
            try (ResultSet tables = dbMetaData.getTables(connection.getCatalog(), connection.getSchema(), null,
                    getAvailableTableTypes(dbMetaData).toArray(new String[0]))) {
                while (tables.next()) {
                    ofNullable(ofNullable(tables.getString("TABLE_NAME")).orElseGet(() -> {
                        try {
                            return tables.getString("SYNONYM_NAME");
                        } catch (final SQLException e) {
                            return null;
                        }
                    })).ifPresent(t -> items.add(new SuggestionValues.Item(t, t)));
                }
            }
        } catch (final Exception unexpected) { // catch all exceptions for this ui label to return empty list
            LOG.error(i18n.errorCantLoadTableSuggestions(), unexpected);
        }
        return new SuggestionValues(true, items);

    }
        private Set<String> getAvailableTableTypes(DatabaseMetaData dbMetaData) throws SQLException {
        Set<String> result = new HashSet<>();
        try (ResultSet tables = dbMetaData.getTableTypes()) {
            while (tables.next()) {
                ofNullable(tables.getString("TABLE_TYPE")).map(String::trim)
                        .map(t -> ("BASE TABLE".equalsIgnoreCase(t)) ? "TABLE" : t)
                        .filter(t -> getSupportedTableTypes().contains(t)).ifPresent(result::add);
            }
        }
        return result;
    }

        private Set<String> getSupportedTableTypes()
    {
        Set<String> tableTypes = new HashSet<String>();
        tableTypes.add("TABLE");
        tableTypes.add("VIEW");
        tableTypes.add("SYNONYM");

        return tableTypes;
    }

    public Field convert(AvalancheBulkMapperConfiguration.Column column){
       Integer length;
        try {
            length = Integer.valueOf(column.getLength());
        }catch (NumberFormatException e){
           length = -1;
        }

        return Field.builder()
                .columnName(column.getColumnName())
                .type(column.getType())
                .length(length)
                .nullable(column.getNullable())
                .build();
    }

    public AvalancheSchema convert(List<AvalancheBulkMapperConfiguration.Column> columns){

        List<Field> fields = new ArrayList<>();
        for(AvalancheBulkMapperConfiguration.Column column : columns){
            fields.add(convert(column));
        }
        return  AvalancheSchema.builder().fields(fields).build();
    }

    public AvalancheBulkMapperConfiguration.Column fieldToColumn (Field field){
        AvalancheBulkMapperConfiguration.Column column = new AvalancheBulkMapperConfiguration.Column();
        column.setColumnName(field.getColumnName());
        column.setType(field.getType());
        column.setNullable(field.getNullable());
        column.setLength(String.valueOf(field.getLength()));
        return  column;
    }

    public List<AvalancheBulkMapperConfiguration.Column> schemaToColumns(AvalancheSchema schema){
        List<AvalancheBulkMapperConfiguration.Column> columns = new ArrayList<>();
        for(Field field : schema.getFields()){
            columns.add(fieldToColumn(field));
        }
        return columns;
    }

    public void addField(final List<Field> fields, final ResultSetMetaData metaData, final  int columnIndex){
        try {
            final String javaType = metaData.getColumnClassName(columnIndex);
            final int sqlType = metaData.getColumnType(columnIndex);
            final Field.FieldBuilder fieldBuilder = Field.builder().columnName(metaData.getColumnName(columnIndex))
                    .nullable(metaData.isNullable(columnIndex) != columnNoNulls);
            switch (sqlType) {
                case Types.SMALLINT:
                    fields.add(fieldBuilder.type(SQLDataTypes.SMALLINT).build());
                    break;
                case Types.TINYINT:
                    fields.add(fieldBuilder.type(SQLDataTypes.TINYINT).build());
                    break;
                case Types.INTEGER:
                    fields.add(fieldBuilder.type(SQLDataTypes.INTEGER).build());
                    break;
                case Types.DOUBLE:
                case Types.FLOAT:
                case Types.DECIMAL:
                case Types.NUMERIC:
                    fields.add(fieldBuilder.type(SQLDataTypes.FLOAT).build());
                    break;
                case Types.BOOLEAN:
                    fields.add(fieldBuilder.type(SQLDataTypes.BOOLEAN).build());
                    break;
                case Types.TIME:
                    fields.add(fieldBuilder.type(SQLDataTypes.TIME).build());
                    break;
                case Types.DATE:
                    fields.add(fieldBuilder.type(SQLDataTypes.ANSIDATE).build());
                    break;
                case Types.TIMESTAMP:
                    fields.add(fieldBuilder.type(SQLDataTypes.TIMESTAMP).build());
                    break;
                case Types.BIGINT:
                    fields.add(fieldBuilder.type(SQLDataTypes.BIGINT).build());
                    break;
                case Types.CHAR:
                    fields.add(fieldBuilder.type(SQLDataTypes.CHAR).build());
                    break;
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                default:
                    fields.add(fieldBuilder.type(SQLDataTypes.VARCHAR).length(metaData.getColumnDisplaySize(columnIndex)).build());
                    break;
            }
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }


}
