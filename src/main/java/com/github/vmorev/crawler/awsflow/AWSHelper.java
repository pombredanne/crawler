package com.github.vmorev.crawler.awsflow;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.github.vmorev.crawler.utils.ConfigStorage;
import com.github.vmorev.crawler.utils.HttpHelper;
import com.github.vmorev.crawler.utils.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 21.01.13
 */
public class AWSHelper {
    public static final String S3_NAME_DELIMETER = "-";
    public static final String S3_NAME_SUFFIX = ".json";
    public static final String S3_METADATA_FLOWID = "flow-id";

    private static final String CONFIG_FILE = "aws.json";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final String SWF_URL = "swfUrl";
    private static final String SWF_DOMAIN = "swfDomain";
    private static final String SWF_TASKLIST = "swfTaskList";
    private static final String S3_ARTICLE_BUCKET = "s3ArticleBucket";
    private static final String S3_SITE_BUCKET = "s3SiteBucket";
    private static final String S3_REWRITE = "s3rewrite";

    private Map<String, String> config;

    public AWSHelper() throws IOException {
        config = ConfigStorage.getInstance(CONFIG_FILE, Map.class, false);
    }

    public AWSCredentials getCredentials() {
        return new BasicAWSCredentials(config.get(ACCESS_KEY), config.get(SECRET_KEY));
    }

    public AmazonSimpleWorkflow createSWFClient() throws IOException {
        AmazonSimpleWorkflow client = new AmazonSimpleWorkflowClient(getCredentials());
        client.setEndpoint(config.get(SWF_URL));
        return client;
    }

    public AmazonS3 createS3Client() throws IOException {
        return new AmazonS3Client(getCredentials());
    }

    public <T> void saveS3Object(String bucket, String key, T obj) throws IOException {
        saveS3Object(bucket, key, obj, new ObjectMetadata());
    }

    public <T> void saveS3Object(String bucket, String key, T obj, ObjectMetadata metadata) throws IOException {
        InputStream inStream = HttpHelper.stringToInputStream(JsonHelper.parseObject(obj));
        metadata.setContentLength(inStream.available());
        createS3Client().putObject(bucket, key, inStream, metadata);
    }

    public String getSWFDomain() {
        return config.get(SWF_DOMAIN);
    }

    public String getSwfTasklist() {
        return config.get(SWF_TASKLIST);
    }

    public String getS3ArticleBucket() {
        return config.get(S3_ARTICLE_BUCKET);
    }

    public String getS3SiteBucket() {
        return config.get(S3_SITE_BUCKET);
    }

    public boolean isS3RewriteAllowed() {
        return Boolean.valueOf(config.get(S3_REWRITE));
    }
}
