package com.examplesql.talend.components.output.statement;

import com.examplesql.talend.components.output.AvalancheSchema;
import com.examplesql.talend.components.service.AvalancheComponentBulkService;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

public interface QueryManager extends Serializable {

    int execute(AvalancheComponentBulkService.DataSource dataSource, AvalancheSchema schema) throws SQLException, IOException;
}

