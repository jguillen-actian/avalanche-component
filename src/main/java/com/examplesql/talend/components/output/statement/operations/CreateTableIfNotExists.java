package com.examplesql.talend.components.output.statement.operations;

import com.examplesql.talend.components.output.Column;
import com.examplesql.talend.components.output.OutputConfiguration;
import com.examplesql.talend.components.output.Table;
import com.examplesql.talend.components.service.I18nMessage;
import org.talend.sdk.component.api.record.Schema;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateTableIfNotExists extends QueryManagerImpl {

    private Map<Integer, Schema.Entry> namedParams;

    private final Map<String, String> queries = new HashMap<>();

    public CreateTableIfNotExists(final OutputConfiguration configuration, final I18nMessage i18n) {
        super(i18n, configuration);
        namedParams = new HashMap<>();
    }


    @Override
    public void setConfigurations() {
        this.isCreateTable = true;
        this.isCreateNotExists = true;
    }
}
