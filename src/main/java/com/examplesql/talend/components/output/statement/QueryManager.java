package com.examplesql.talend.components.output.statement;

import com.examplesql.talend.components.output.Reject;
import com.examplesql.talend.components.service.AvalancheComponentBulkService;
import org.talend.sdk.component.api.record.Record;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

public interface QueryManager extends Serializable {

    void execute(List<Record> records, AvalancheComponentBulkService.DataSource dataSource) throws SQLException, IOException;
}
