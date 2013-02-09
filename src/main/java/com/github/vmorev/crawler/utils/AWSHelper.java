package com.github.vmorev.crawler.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
    private static final String SQS_ARTICLE_CONTENT = "sqsArticleContent";
    private static final String S3_ARTICLE = "s3Article";
    private static final String S3_SITE = "s3Site";
    private static final String SQS_SITE = "sqsSite";

    private Map<String, String> config;
    private AWSCredentials credentials;
    private AmazonSQSClient sqs;
    private AmazonS3 s3;

    public AWSHelper() throws IOException {
        config = ConfigStorage.getInstance(CONFIG_FILE, Map.class, false);
    }

    public AWSCredentials getCredentials() {
        if (credentials == null)
        credentials = new BasicAWSCredentials(config.get(ACCESS_KEY), config.get(SECRET_KEY));
        return credentials;
    }

    public AmazonSQSClient getSQS() {
        if (sqs == null)
            sqs = new AmazonSQSClient(getCredentials());
        return sqs;
    }

    public AmazonS3 getS3() throws IOException {
        if (s3 == null)
            s3 = new AmazonS3Client(getCredentials());
        return s3;
    }

    public <T> void saveS3Object(String bucket, String key, T obj) throws IOException {
        saveS3Object(bucket, key, obj, new ObjectMetadata());
    }

    public <T> void saveS3Object(String bucket, String key, T obj, ObjectMetadata metadata) throws IOException {
        InputStream inStream = HttpHelper.stringToInputStream(JsonHelper.parseObject(obj));
        metadata.setContentLength(inStream.available());
        getS3().putObject(bucket, key, inStream, metadata);
    }

    public <T> T getS3Object(String bucket, String key, Class<T> clazz) {
        T obj = null;
        try {
            obj = JsonHelper.parseJson(getS3().getObject(bucket, key).getObjectContent(), clazz);
        } catch (Exception e) {
            //do nothing and return null
        }
        return obj;
    }

    public void deleteS3Bucket(String bucketName) throws IOException {
        AmazonS3 s3 = getS3();
        ObjectListing objectListing = s3.listObjects(bucketName);
        do {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
                s3.deleteObject(bucketName, objectSummary.getKey());
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        s3.deleteBucket(bucketName);

    }

    public void createSQSQueue(String name) {
        AmazonSQSClient sqs = getSQS();
        List<String> urls = sqs.listQueues().getQueueUrls();
        for (String url : urls)
            if (url.endsWith(name))
                return;

        CreateQueueRequest request = new CreateQueueRequest();
        request.setQueueName(name);
        sqs.createQueue(request);
    }

    public String getS3BucketArticle() {
        return config.get(S3_ARTICLE);
    }

    public String getS3BucketSite() {
        return config.get(S3_SITE);
    }

    public String getSQSQueueArticleContent() {
        return config.get(SQS_ARTICLE_CONTENT);
    }

    public String getSQSQueueSite() {
        return config.get(SQS_SITE);
    }

    public void createS3Bucket(String bucket) throws IOException {
        if (!getS3().doesBucketExist(bucket))
            getS3().createBucket(bucket);

    }
}
