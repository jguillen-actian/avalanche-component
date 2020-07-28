package com.examplesql.talend.components.output;

import com.examplesql.talend.components.dataset.AvalancheTableDataset;
import com.examplesql.talend.components.service.I18nMessage;
import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.service.completion.Suggestions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
        @GridLayout.Row({ "dataset" }),
        @GridLayout.Row({ "actionOnTable","varcharLength" }),
        @GridLayout.Row({ "format", "header", "fieldDelimiter" }),
        @GridLayout.Row({ "dateFormat" , "timestampFormat"}),
        @GridLayout.Row({ "settings" }),
        @GridLayout.Row({ "s3Bucket", "filename" }),
})
@Documentation("TODO fill the documentation for this configuration")
public class OutputConfiguration implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private AvalancheTableDataset dataset;

    @Option
    @Required
//    @Suggestable(value = "actionsList", parameters = { "../dataset" })
    @Documentation("The action on data to be performed")
    private ActionOnTable actionOnTable = ActionOnTable.NONE;

//    @Option
//    @Suggestable(value = "suggestTableColumnNames", parameters = { "dataset" })
//    @Documentation("List of columns to be used as keys for this operation")
//    private List<String> keys = new ArrayList<>();

    @Option
//    @Suggestable(value = "formatList")
    @Documentation("Format of input file")
    @Required
    private Format format = Format.CSV;

    @Option
    @Required
    @Documentation("S3 bucket name")
    private String s3Bucket;

    @Option
    @Required
    @Documentation("File name, or file list for S3 input file(s)")
    private String filename;

    @Option
    @Required
    @ActiveIf(target = "format", value =  "CSV" )
    @Documentation("Only if [File format=CSV], specifies Field Delimiter character ")
    private String fieldDelimiter = ",";

    @Option
    @ActiveIf(target = "format", value =  "CSV" )
    @Documentation("Only if [File format=CSV], sets string that specifies Date Format. Custom date formats follow the formats at java.text.SimpleDateFormat. This applies to date type. ")
    private String dateFormat = null;

    @Option
    @ActiveIf(target = "format", value =  "CSV" )
    @Documentation("Only if [File format=CSV], sets string that specifies Timestamp Format. Custom date formats follow the formats at java.text.SimpleDateFormat. This applies to timestamp type. ")
    private String timestampFormat = null;

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
    @ActiveIf(target = "actionOnTable", value = { "CREATE", "DROP_AND_CREATE","CREATE_TABLE_IF_NOT_EXISTS","DROP_TABLE_IF_EXISTS_AND_CREATE" })
    @Documentation("The length of varchar types. This value will be used to create varchar columns in this table."
            + "\n-1 means that the max supported length of the targeted database will be used.")
    private int varcharLength = -1;


    public AvalancheTableDataset getDataset() {
        return dataset;
    }

    public OutputConfiguration setDataset(AvalancheTableDataset dataset) {
        this.dataset = dataset;
        return this;
    }

    public int getVarcharLength() { return this.varcharLength; }

    public OutputConfiguration setVarcharLength(int varcharLength)
    {
        this.varcharLength = varcharLength;
        return this;
    }

//    public List<String>  getKeys() { return this.keys; }
//
//    public OutputConfiguration setKeys(List<String> keys){
//        this.keys = keys;
//        return this;
//    }

    public Format  getFormat() { return this.format; }

    public OutputConfiguration setS3Bucket(Format format){
        this.format = format;
        return this;
    }

    public String  getFieldDelimiter() { return this.fieldDelimiter; }

    public OutputConfiguration setFieldDelimiter(String fieldDelimiter){
        this.fieldDelimiter = fieldDelimiter;
        return this;
    }

    public String  getDateFormat() { return this.dateFormat; }

    public OutputConfiguration setDateFormat(String dateFormat){
        this.dateFormat = dateFormat;
        return this;
    }

    public String  getTimestampFormat() { return this.timestampFormat; }

    public OutputConfiguration setTimestampFormat(String timestampFormat){
        this.timestampFormat = timestampFormat;
        return this;
    }

    public Boolean  getHeader() { return this.header; }

    public OutputConfiguration setHeader(Boolean header){
        this.header = header;
        return this;
    }

    public List<Settings>  getSettings() { return this.settings; }

    public OutputConfiguration setSettings(List<Settings> settings){
        this.settings = settings;
        return this;
    }

//    public DelimeterSwitches  getSettings() { return this.settings; }
//
//    public OutputConfiguration setSettings(DelimeterSwitches settings){
//        this.settings = settings;
//        return this;
//    }

    public String  getS3Bucket() { return this.s3Bucket; }

    public OutputConfiguration setS3Bucket(String s3Bucket){
        this.s3Bucket = s3Bucket;
        return this;
    }
    public String getFilename() { return this.filename; }

    public OutputConfiguration setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public ActionOnTable getActionOnTable() { return this.actionOnTable; }

    public OutputConfiguration setActionOnTable(ActionOnTable actionOnTable)
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
        private String value;
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

//    @Data
//    public static class DelimeterSwitches{
//        @Option
//        @Documentation("")
//        private String emptyValue;
//        @Option
//        @Documentation("")
//        private String nanValue;
//        @Option
//        @Documentation("")
//        private String nullvalue;
//        @Option
//        @Documentation("")
//        private String escape;
//        @Option
//        @Documentation("")
//        private String quote;
//        @Option
//        @Documentation("")
//        private String comment;
//        @Option
//        @Documentation("")
//        private String charToEscapeQuoteEscaping;
//        @Option
//        @Documentation("")
//        private String columnNameOfCorruptRecord;
//        @Option
//        @Documentation("")
//        private Boolean enforceSchema;
//        @Option
//        @Documentation("")
//        private Boolean inferSchema;
//        @Option
//        @Documentation("")
//        private Boolean multiLine;
//        @Option
//        @Documentation("")
//        private Boolean ignoreLeadingWhiteSpace;
//        @Option
//        @Documentation("")
//        private Boolean ignoreTrailingWhiteSpace;
//        @Option
//        @Documentation("")
//        private Mode mode;
//        @Option
//        @Documentation("")
//        private String positiveInf;
//        @Option
//        @Documentation("")
//        private String negativeInf;
//        @Option
//        @Documentation("")
//        private double samplingRatio;
//
//        public enum Mode{
//             PERMISSIVE("PERMISSIVE"),
//             DROPMALFORMED("DROPMALFORMED"),
//             FAILFAST("FAILFAST");
//
//             public final String label;
//
//             private Mode(String label) {
//                 this.label = label;
//             }
//
//         }

//    }

//    public boolean isCreateTableIfNotExists() {
//        return createTableIfNotExists && isActionOnData().isAllowTableCreation();
//    }
}