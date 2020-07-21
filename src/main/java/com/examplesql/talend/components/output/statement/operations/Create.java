package com.examplesql.talend.components.output.statement.operations;

import com.examplesql.talend.components.output.Column;
import com.examplesql.talend.components.output.OutputConfiguration;
import com.examplesql.talend.components.output.Table;
import com.examplesql.talend.components.service.AvalancheComponentBulkService;
import com.examplesql.talend.components.service.I18nMessage;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.talend.sdk.component.api.record.Schema.Type.STRING;

public class Create extends QueryManagerImpl {

    private Map<Integer, Schema.Entry> namedParams;

    private final Map<String, String> queries = new HashMap<>();

    public Create(final OutputConfiguration configuration, final I18nMessage i18n) {
        super(i18n, configuration);
        namedParams = new HashMap<>();
    }


    @Override
    public void setConfigurations() {
        this.isCreateTable = true;
    }
}
