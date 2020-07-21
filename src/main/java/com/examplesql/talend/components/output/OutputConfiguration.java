package com.examplesql.talend.components.output;

import com.examplesql.talend.components.dataset.AvalancheTableDataset;
import com.examplesql.talend.components.service.I18nMessage;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
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
        @GridLayout.Row({ "actionOnTable" }),
        @GridLayout.Row({"keys"}),
        @GridLayout.Row({"varcharLength"})
})
@Documentation("TODO fill the documentation for this configuration")
public class OutputConfiguration implements Serializable {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private AvalancheTableDataset dataset;

    @Option
    @Required
    @Suggestable(value = "actionsList", parameters = { "../dataset" })
    @Documentation("The action on data to be performed")
    private String actionOnTable = "NONE";

    @Option
    @Suggestable(value = "suggestTableColumnNames", parameters = { "dataset" })
    @Documentation("List of columns to be used as keys for this operation")
    private List<String> keys = new ArrayList<>();

    @Option
    @Required
//    @ActiveIf(target = "../createTableIfNotExists", value = { "true" })
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

    public List<String>  getKeys() { return this.keys; }

    public OutputConfiguration setKeys(List<String> keys){
        this.keys = keys;
        return this;
    }

    public String getActionOnTable() { return this.actionOnTable; }

    public OutputConfiguration setActionOnTable(String actionOnTable)
    {
        this.actionOnTable = actionOnTable;
        return this;
    }

    public ActionOnTable isActionOnTable() {
        if (actionOnTable == null || actionOnTable.isEmpty()) {
            throw new IllegalArgumentException("label on data is required");
        }

        return ActionOnTable.getValueOf(actionOnTable);
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

//    public boolean isCreateTableIfNotExists() {
//        return createTableIfNotExists && isActionOnData().isAllowTableCreation();
//    }
}