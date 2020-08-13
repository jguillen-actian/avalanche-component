package com.examplesql.talend.components.source;//package com.examplesql.talend.components.source;
//
//import com.examplesql.talend.components.output.CloudStorage;
//import com.examplesql.talend.components.service.AvalancheComponentBulkService;
//import com.examplesql.talend.components.service.I18nMessage;
//import org.talend.sdk.component.api.configuration.Option;
//import org.talend.sdk.component.api.input.Producer;
//import org.talend.sdk.component.api.meta.Documentation;
//import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
//
//import java.io.Serializable;
//
//@Documentation("TODO fill the documentation for this source")
//public class CloudStorageInputSource implements Serializable {
//    private final CloudStorageInputMapperConfiguration configuration;
//    private final AvalancheComponentBulkService service;
//    private final I18nMessage i18n;
//
//    public CloudStorageInputSource(@Option("configuration") final CloudStorageInputMapperConfiguration configuration,
//                               final AvalancheComponentBulkService service,
//                               final I18nMessage i18nMessage) {
//        this.configuration = configuration;
//        this.service = service;
//        this.i18n = i18nMessage;
//    }
//
//    @Producer
//    public CloudStorage next(){
//        return CloudStorage.builder().bucket(configuration.getS3Bucket()).key(configuration.getFilename()).build();
//    }
//}
//
