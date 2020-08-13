package com.examplesql.talend.components.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Field {
    private String columnName;
    private SQLDataTypes type;
    private Boolean nullable;
    private Integer length;
}
