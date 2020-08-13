package com.examplesql.talend.components.dataset;

import com.examplesql.talend.components.datastore.AvalancheDatastore;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.constraint.Min;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import static org.talend.sdk.component.api.configuration.ui.layout.GridLayout.FormType.ADVANCED;

@DataSet("AvalancheTableDataset")
@GridLayout({
        // the generated layout put one configuration entry per line,
        // customize it as much as needed
        @GridLayout.Row({ "datastore" }),
        @GridLayout.Row({ "tableName" })
})
//@GridLayout(names = ADVANCED, value = { @GridLayout.Row("fetchSize") })
@Documentation("TODO fill the documentation for this configuration")
public class AvalancheTableDataset {
    @Option
    @Documentation("TODO fill the documentation for this parameter")
    private AvalancheDatastore datastore;

    @Option
    @Required
    @Documentation("Table name in Avalanche")
    @Suggestable(value = "listTables", parameters = "datastore")
    private String tableName = "\"\"";


    public AvalancheDatastore getDatastore() {
        return datastore;
    }

    public AvalancheTableDataset setDatastore(AvalancheDatastore datastore) {
        this.datastore = datastore;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public AvalancheTableDataset setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getQuery() {
        return "select * from " + this.getTableName();
    }
}
