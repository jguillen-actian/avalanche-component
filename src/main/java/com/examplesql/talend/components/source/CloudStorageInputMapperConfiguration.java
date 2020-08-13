package com.examplesql.talend.components.source;//package com.examplesql.talend.components.source;
//
//
//import com.examplesql.talend.components.output.OutputConfiguration;
//import org.talend.sdk.component.api.configuration.Option;
//import org.talend.sdk.component.api.configuration.constraint.Required;
//import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
//import org.talend.sdk.component.api.meta.Documentation;
//
//@GridLayout({
//        @GridLayout.Row({ "s3Bucket", "filename" })
//})
//public class CloudStorageInputMapperConfiguration {
//    @Option
//    @Required
//    @Documentation("S3 bucket name")
//    private String s3Bucket;
//
//    @Option
//    @Required
//    @Documentation("File name, or file list for S3 input file(s)")
//    private String filename;
//
//    public String  getS3Bucket() { return this.s3Bucket; }
//
//    public CloudStorageInputMapperConfiguration setS3Bucket(String s3Bucket){
//        this.s3Bucket = s3Bucket;
//        return this;
//    }
//    public String getFilename() { return this.filename; }
//
//    public CloudStorageInputMapperConfiguration setFilename(String filename) {
//        this.filename = filename;
//        return this;
//    }
//
//
//}
//
////@GridLayout({
////        // the generated layout put one configuration entry per line,
////        // customize it as much as needed
////        @GridLayout.Row({ "dataset" })
////})
////@Documentation("TODO fill the documentation for this configuration")
////public class SQLQueryInputMapperConfiguration implements Serializable {
////    @Option
////    @Documentation("TODO fill the documentation for this parameter")
////    private SQLQueryDataset dataset;
////
////    public SQLQueryDataset getDataset() {
////        return dataset;
////    }
////
////    public SQLQueryInputMapperConfiguration setDataset(SQLQueryDataset dataset) {
////        this.dataset = dataset;
////        return this;
////    }
////}
