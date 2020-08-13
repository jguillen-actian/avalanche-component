package com.examplesql.talend.components.output;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AvalancheSchema {
    private List<Field>  fields;

}
