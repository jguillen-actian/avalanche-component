package com.examplesql.talend.components;//package com.examplesql.talend.components;
//
//import com.examplesql.talend.components.datastore.AvalancheDatastore;
//import com.examplesql.talend.components.service.AvalancheComponentBulkService;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Order;
//import org.talend.sdk.component.api.service.Service;
//import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
//import org.talend.sdk.component.junit.environment.Environment;
//import org.talend.sdk.component.junit.environment.EnvironmentConfiguration;
//import org.talend.sdk.component.junit.environment.builtin.ContextualEnvironment;
//import org.talend.sdk.component.junit5.WithComponents;
//import org.talend.sdk.component.junit5.WithMavenServers;
//import org.talend.sdk.component.junit5.environment.EnvironmentalTest;
//
//import java.util.Properties;
//
//@Environment(ContextualEnvironment.class)
//@EnvironmentConfiguration(environment = "Contextual", systemProperties = {}) // EnvironmentConfiguration is necessary for each
//                                                                             // @Environment
//
//@WithMavenServers
//@WithComponents("com.examplesql.talend.components")
//public class ExampleSQLTests extends ExampleSQLBaseTest {
//
//    @Service
//    private AvalancheComponentBulkService service;
//
//    private AvalancheDatastore datastore;
//
//    private Properties props;
//
//
//    @BeforeAll
//    public static void initialize()
//    {
//        String[] args = {"start"};
//        ExampleSQLDocker.main(args);
//
//    }
//
//    @AfterAll
//    public static void deinitialize()
//    {
//        String[] args = {"stop"};
//        ExampleSQLDocker.main(args);
//    }
//
//    @BeforeEach
//    public void setup()
//    {
//        props = ExampleSQLProps();
//        datastore = buildDataStore(props);
//    }
//
//    @EnvironmentalTest
//    @Order(1)
//    public void validateConnection() throws NoSuchFieldException, IllegalAccessException {
//
//        final HealthCheckStatus status = service.validateConnection(datastore);
//        Assertions.assertNotNull(status);
//        Assertions.assertEquals(HealthCheckStatus.Status.OK, status.getStatus(),"ExampleSQLDatastore Connection Valid");
//
//    }
//
//
////    @EnvironmentalTest
////    @Order(2)
////    public void tableNameDatasetTest() {
////        Properties props = ExampleSQLProps();
////        ExampleSQLDatastore datastore = buildDataStore(props);
////        TableNameDataset dataset = new TableNameDataset();
////        dataset.setDatastore(datastore);
////        dataset.setFetchSize(1000);
////        dataset.setTableName(props.getProperty("ExampleSQL.table"));
////
////        Assertions.assertEquals("select * from " + props.getProperty("ExampleSQL.table"),dataset.getQuery());
////    }
////
////    @EnvironmentalTest
////    @Order(3)
////    public void tableNameInputSourceTest() throws NoSuchFieldException, IllegalAccessException {
////            Properties props = ExampleSQLProps();
////            ExampleSQLDatastore datastore = buildDataStore(props);
////            TableNameDataset dataset = new TableNameDataset();
////            dataset.setDatastore(datastore);
////            dataset.setFetchSize(1000);
////            dataset.setTableName(props.getProperty("ExampleSQL.table"));
////
////            I18nMessage i18n = getI18n();
////            final Field internationalField = ExampleSQLComponentService.class.getDeclaredField("i18n");
////            internationalField.setAccessible(true);
////            internationalField.set(service, i18n);
////            TableNameInputMapperConfiguration configuration = new TableNameInputMapperConfiguration();
////            configuration.setDataset(dataset);
////            RecordBuilderFactory factory = new RecordBuilderFactoryImpl(null);
////            TableNameInputSource source = new TableNameInputSource(configuration, service, factory , i18n);
////            source.init();
////            Record record = source.next();
////            Assertions.assertNotNull(record);
////            source.release();
////    }
////
////    @EnvironmentalTest
////    @Order(4)
////    public void sqlQueryDatasetTest()
////    {
////        Properties props = ExampleSQLProps();
////        ExampleSQLDatastore datastore = buildDataStore(props);
////        SQLQueryDataset dataset = new SQLQueryDataset();
////        dataset.setDatastore(datastore);
////        dataset.setFetchSize(1000);
////        dataset.setSqlQuery("select * from " + props.getProperty("ExampleSQL.table"));
////
////        Assertions.assertEquals("select * from " + props.getProperty("ExampleSQL.table"),dataset.getQuery());
////    }
////
////    @EnvironmentalTest
////    @Order(5)
////    public void sqlQueryInputSourceTest() throws NoSuchFieldException, IllegalAccessException
////    {
////
////            Properties props = ExampleSQLProps();
////            ExampleSQLDatastore datastore = buildDataStore(props);
////            SQLQueryDataset dataset = new SQLQueryDataset();
////            dataset.setDatastore(datastore);
////            dataset.setFetchSize(1000);
////            dataset.setSqlQuery("select * from " + props.getProperty("ExampleSQL.table"));
////
////            I18nMessage i18n = getI18n();
////            final Field internationalField = ExampleSQLComponentService.class.getDeclaredField("i18n");
////            internationalField.setAccessible(true);
////            internationalField.set(service, i18n);
////            SQLQueryInputMapperConfiguration configuration = new SQLQueryInputMapperConfiguration();
////            configuration.setDataset(dataset);
////            RecordBuilderFactory factory = new RecordBuilderFactoryImpl(null);
////            SQLQueryInputSource source = new SQLQueryInputSource(configuration, service, factory , i18n);
////            source.init();
////            Record record = source.next();
////            Assertions.assertNotNull(record);
////            source.release();
////
////    }
////
////    @EnvironmentalTest
////    @Order(6)
////    public void outputInsertTest() throws NoSuchFieldException, IllegalAccessException, SQLException
////    {
////
////            Properties props = ExampleSQLProps();
////            ExampleSQLDatastore datastore = buildDataStore(props);
////            // Output table
////            TableNameDataset dataset = new TableNameDataset();
////            dataset.setDatastore(datastore);
////            dataset.setFetchSize(1000);
////            dataset.setTableName(props.getProperty("ExampleSQL.insert.table"));
////            // Input table
////            TableNameDataset input = new TableNameDataset();
////            input.setDatastore(datastore);
////            input.setFetchSize(1000);
////            input.setTableName(props.getProperty("ExampleSQL.table"));
////
////            I18nMessage i18n = getI18n();
////            final Field internationalField = ExampleSQLComponentService.class.getDeclaredField("i18n");
////            internationalField.setAccessible(true);
////            internationalField.set(service, i18n);
////            OutputConfiguration outputConfiguration = new OutputConfiguration();
////            outputConfiguration.setActionOnData("INSERT");
////            outputConfiguration.setCreateTableIfNotExists(true);
////            outputConfiguration.setDataset(dataset);
////            outputConfiguration.setRewriteBatchedStatements(true);
////            Output output = new Output(outputConfiguration, service, i18n);
////            // Read input table
////            TableNameInputMapperConfiguration configuration = new TableNameInputMapperConfiguration();
////            configuration.setDataset(input);
////            RecordBuilderFactory factory = new RecordBuilderFactoryImpl(null);
////            TableNameInputSource source = new TableNameInputSource(configuration, service, factory , i18n);
////            source.init();
////            Record record;
////            output.beforeGroup();
////            while ((record = source.next()) != null)
////            {
////                output.onNext(record);
////            }
////            output.afterGroup();
////            source.release();
////            output.release();
////
////            Assertions.assertEquals(true, true, "outputInsert Success!");
////
////
////    }
////
////    @EnvironmentalTest
////    @Order(7)
////    public void outputBulkTest() throws NoSuchFieldException, IllegalAccessException, SQLException
////    {
////
////            Properties props = ExampleSQLProps();
////            ExampleSQLDatastore datastore = buildDataStore(props);
////            // Output table
////            TableNameDataset dataset = new TableNameDataset();
////            dataset.setDatastore(datastore);
////            dataset.setFetchSize(1000);
////            dataset.setTableName(props.getProperty("ExampleSQL.bulk.table"));
////            // Input table
////            TableNameDataset input = new TableNameDataset();
////            input.setDatastore(datastore);
////            input.setFetchSize(1000);
////            input.setTableName(props.getProperty("ExampleSQL.table"));
////
////            I18nMessage i18n = getI18n();
////            final Field internationalField = ExampleSQLComponentService.class.getDeclaredField("i18n");
////            internationalField.setAccessible(true);
////            internationalField.set(service, i18n);
////            OutputConfiguration outputConfiguration = new OutputConfiguration();
////            outputConfiguration.setActionOnData("BULK_LOAD");
////            outputConfiguration.setCreateTableIfNotExists(true);
////            outputConfiguration.setDataset(dataset);
////            outputConfiguration.setRewriteBatchedStatements(true);
////            Output output = new Output(outputConfiguration, service, i18n);
////            // Read input table
////            TableNameInputMapperConfiguration configuration = new TableNameInputMapperConfiguration();
////            configuration.setDataset(input);
////            RecordBuilderFactory factory = new RecordBuilderFactoryImpl(null);
////            TableNameInputSource source = new TableNameInputSource(configuration, service, factory , i18n);
////            source.init();
////            Record record;
////            output.beforeGroup();
////            while ((record = source.next()) != null)
////            {
////                output.onNext(record);
////            }
////            output.afterGroup();
////            source.release();
////            output.release();
////
////            Assertions.assertEquals(true, true, "outputInsert Success!");
////
////    }
//
//}