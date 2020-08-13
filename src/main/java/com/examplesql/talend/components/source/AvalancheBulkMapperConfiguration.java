package com.examplesql.talend.components.source;

import com.examplesql.talend.components.dataset.AvalancheTableDataset;
import com.examplesql.talend.components.output.SQLDataTypes;
import com.examplesql.talend.components.service.I18nMessage;
import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@GridLayout({
        // the generated layout put one configuration entry per line,
        // customize it as much as needed
        @GridLayout.Row({ "dataset" }),
        @GridLayout.Row({ "useExistingTableSchema" }),
        @GridLayout.Row({ "columns"}),
        @GridLayout.Row({ "actionOnTable","varcharLength" }),
        @GridLayout.Row({ "format", "header", "fieldDelimiter" }),
        @GridLayout.Row({ "dateFormat" , "timestampFormat"}),
        @GridLayout.Row({ "settings" }),
        @GridLayout.Row({ "s3Bucket", "key" }),
})
public class AvalancheBulkMapperConfiguration implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private AvalancheTableDataset dataset;

    @Option
    @Documentation("Use existing table schema if selected or select schema ")
    private Boolean useExistingTableSchema = false;

    @Option
    @Documentation("Schema field")
    private List<Column> columns = new ArrayList<>();

    @Option
    @Required
    @Documentation("The action on data to be performed")
    private ActionOnTable actionOnTable = ActionOnTable.NONE;

    @Option
    @Documentation("Format of input file")
    @Required
    private Format format = Format.CSV;

    @Option
    @Required
    @ActiveIf(target = "format", value =  "CSV" )
    @Documentation("Only if [File format=CSV], specifies Field Delimiter character ")
    private String fieldDelimiter = "\",\"";

    @Option
    @ActiveIf(target = "format", value =  "CSV" )
    @Documentation("Only if [File format=CSV], sets string that specifies Date Format. Custom date formats follow the formats at java.text.SimpleDateFormat. This applies to date type. ")
    private String dateFormat = "\"\"";

    @Option
    @ActiveIf(target = "format", value =  "CSV" )
    @Documentation("Only if [File format=CSV], sets string that specifies Timestamp Format. Custom date formats follow the formats at java.text.SimpleDateFormat. This applies to timestamp type. ")
    private String timestampFormat = "\"\"";

    @Option
    @Required
    @ActiveIf(target = "format", value =  "CSV" )
    @Documentation("Only if [File format=CSV], specifies if header is present in file ")
    private Boolean header = true;

    @Option
    @ActiveIf(target = "format", value =  "CSV" )
    @Documentation("Only if [File format=CSV], specifies supported delimiter text file switches.")
    private List<Settings> settings = new ArrayList<>();

    @Option
    @Required
    @Documentation("S3 bucket name")
    private String s3Bucket = "\"\"";

    @Option
    @Required
    @ActiveIf(target = "actionOnTable", value = { "CREATE", "DROP_AND_CREATE","CREATE_TABLE_IF_NOT_EXISTS","DROP_TABLE_IF_EXISTS_AND_CREATE" })
    @Documentation("The length of varchar types. This value will be used to create varchar columns in this table."
            + "\n-1 means that the max supported length of the targeted database will be used.")
    private int varcharLength = -1;

    @Option
    @Required
    @Documentation("Key name, or file list for S3 input file(s)")
    private String key = "\"\"";

    public String  getS3Bucket() { return this.s3Bucket; }

    public AvalancheBulkMapperConfiguration setS3Bucket(String s3Bucket){
        this.s3Bucket = s3Bucket;
        return this;
    }
    public String getKey() { return this.key; }

    public AvalancheBulkMapperConfiguration setKey(String key) {
        this.key = key;
        return this;
    }


    public AvalancheTableDataset getDataset() {
        return dataset;
    }

    public AvalancheBulkMapperConfiguration setDataset(AvalancheTableDataset dataset) {
        this.dataset = dataset;
        return this;
    }

    public int getVarcharLength() { return this.varcharLength; }

    public AvalancheBulkMapperConfiguration setVarcharLength(int varcharLength)
    {
        this.varcharLength = varcharLength;
        return this;
    }

    public Boolean getUseExistingTableSchema(){return this.useExistingTableSchema;}

    public AvalancheBulkMapperConfiguration setUseExistingTableSchema(Boolean useExistingTableSchema)  {
        this.useExistingTableSchema = useExistingTableSchema;
        return this;
    }

    public List<Column> getSchema(){return this.columns;}

    public AvalancheBulkMapperConfiguration setSchema(List<Column> columns){
        this.columns = columns;
        return  this;
    }

    public Format getFormat() { return this.format; }

    public AvalancheBulkMapperConfiguration setS3Bucket(Format format){
        this.format = format;
        return this;
    }

    public String  getFieldDelimiter() { return this.fieldDelimiter; }

    public AvalancheBulkMapperConfiguration setFieldDelimiter(String fieldDelimiter){
        this.fieldDelimiter = fieldDelimiter;
        return this;
    }

    public String  getDateFormat() { return this.dateFormat; }

    public AvalancheBulkMapperConfiguration setDateFormat(String dateFormat){
        this.dateFormat = dateFormat;
        return this;
    }

    public String  getTimestampFormat() { return this.timestampFormat; }

    public AvalancheBulkMapperConfiguration setTimestampFormat(String timestampFormat){
        this.timestampFormat = timestampFormat;
        return this;
    }

    public Boolean  getHeader() { return this.header; }

    public AvalancheBulkMapperConfiguration setHeader(Boolean header){
        this.header = header;
        return this;
    }

    public List<Settings>  getSettings() { return this.settings; }

    public AvalancheBulkMapperConfiguration setSettings(List<Settings> settings){
        this.settings = settings;
        return this;
    }

//    public DelimeterSwitches  getSettings() { return this.settings; }
//
//    public AvalancheBulkMapperConfiguration setSettings(DelimeterSwitches settings){
//        this.settings = settings;
//        return this;
//    }

    public ActionOnTable getActionOnTable() { return this.actionOnTable; }

    public AvalancheBulkMapperConfiguration setActionOnTable(ActionOnTable actionOnTable)
    {
        this.actionOnTable = actionOnTable;
        return this;
    }

    public ActionOnTable isActionOnTable() {
        if (actionOnTable == null ) {
            throw new IllegalArgumentException("label on data is required");
        }

        return actionOnTable;
    }


    public enum ActionOnTable {

        NONE(I18nMessage::actionOnTableNone),
        CREATE(I18nMessage::actionOnTableCreate),
        DROP_AND_CREATE(I18nMessage::actionOnTableDropAndCreate),
        CREATE_TABLE_IF_NOT_EXISTS(I18nMessage::actionOnTableCreateTableIfNotExists),
        DROP_TABLE_IF_EXISTS_AND_CREATE(I18nMessage::actionOnTableDropTableIfExistsAndCreate);

        private ActionOnTable(Function<I18nMessage, String> labelExtractor)
        {
            this.labelExtractor = labelExtractor;
        }
        private final Function<I18nMessage, String> labelExtractor;



        public String label(final I18nMessage messages) {
            return labelExtractor.apply(messages);
        }

        public static ActionOnTable getValueOf(String value)
        {
            for (ActionOnTable e : values())
            {
                if (e.name().equals(value))
                    return e;
            }

            throw new IllegalArgumentException();
        }
    }

    public enum Format {
        CSV("CSV"),
        PARQUET("PARQUET");

        public final String label;

        private Format(String label) {
            this.label = label;
        }
    }

    @Data
    public static class Settings{
        @Option
        @Documentation("")
        private DelimiterSwitches parameter;

        @Option
        @Documentation("")
        private String value = "\"\"";
    }

    public enum DelimiterSwitches {
        EMPTY_VALUE("emptyValue"),
        NAN_VALUE("nanValue"),
        NULL_VALUE("nullvalue"),
        ESCAPE("escape"),
        QUOTE("quote"),
        COMMENT("comment"),
        CHAR_TO_ESCAPE_QUOTE_ESCAPING("charToEscapeQuoteEscaping"),
        CHAR_NAME_OF_CORRUPT_RECORD("columnNameOfCorruptRecord"),
        ENFORCE_SCHEMA("enforceSchema"),
        INFER_SCHEMA("inferSchema"),
        MULTI_LINE("multiLine"),
        IGNORE_LEADING_WHITE_SPACE("ignoreLeadingWhiteSpace"),
        IGNORE_TRAILING_WHITE_SPACE("ignoreTrailingWhiteSpace"),
        MODE("mode"),
        POSITIVE_INF("positiveInf"),
        NEGATIVE_INF("negativeInf"),
        SAMPLING_RATIO("samplingRatio");




        public final String label;

        private DelimiterSwitches(String label) {
            this.label = label;
        }
    }

    @Data
    public static class Column{
        @Option
        @Documentation("Avalanche Field Name ")
        @Required
        private String columnName = "\"\"";

        @Option
        @Documentation("Data type field")
        private SQLDataTypes type;

        @Option
        @Documentation("Nullable field")
        private Boolean nullable;

        @Option
        @Documentation("Schema field")
        @ActiveIf(target = "type", value =  "STRING" )
        private String length = "\"\"";


    }

    public enum AvalancheDataTypes{
        CHAR,
        VARCHAR,
        INTEGER
    }
}