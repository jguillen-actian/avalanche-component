package com.examplesql.talend.components.output.statement;

import com.examplesql.talend.components.output.OutputConfiguration;
import com.examplesql.talend.components.output.statement.operations.*;
import com.examplesql.talend.components.service.I18nMessage;

public final class QueryManagerFactory {

    private QueryManagerFactory() {
    }

    public static QueryManagerImpl getQueryManager(final I18nMessage i18n,
                                                   final OutputConfiguration configuration) {
        switch (configuration.isActionOnTable()) {
            case NONE:
                return new None(configuration, i18n);
            case CREATE:
                return new Create(configuration, i18n);
            case DROP_AND_CREATE: ;
                return new DropAndCreate(configuration, i18n);
            case CREATE_TABLE_IF_NOT_EXISTS:
                return new CreateTableIfNotExists(configuration, i18n);
            case DROP_TABLE_IF_EXISTS_AND_CREATE:
                return new DropTableIfExistsAndCreate(configuration, i18n);
            default:
                throw new IllegalStateException(i18n.errorUnsupportedDatabaseAction());
        }

    }

}
