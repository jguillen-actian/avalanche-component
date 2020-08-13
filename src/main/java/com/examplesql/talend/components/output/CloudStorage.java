package com.examplesql.talend.components.output;


import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class CloudStorage  implements Serializable{
    private String bucket;
    private String key;
}
