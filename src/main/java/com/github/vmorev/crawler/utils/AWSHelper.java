package com.github.vmorev.crawler.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.util.BinaryUtils;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 21.01.13
 */
public class AWSHelper {
    private AWSCredentials credentials;
    private SQSService sqs;
    private S3Service s3;
    private ConfigService config;

    public AWSHelper() throws IOException {
        config = new ConfigService();
        sqs = new SQSService();
        s3 = new S3Service();
    }

    private AWSCredentials getCredentials() {
        if (credentials == null)
            credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
        return credentials;
    }

    public SQSService getSQS() {
        return sqs;
    }

    public S3Service getS3() {
        return s3;
    }

    public ConfigService getConfig() {
        return config;
    }

    public class ConfigService {
        private static final String S3_ARTICLE = "s3Article";
        private static final String S3_SITE = "s3Site";
        private static final String S3_LOGS = "s3Logs";
        private static final String S3_LOGS_STAT = "s3LogsStat";
        private static final String SQS_SITE = "sqsSite";
        private static final String CONFIG_FILE = "aws.json";
        private static final String ACCESS_KEY = "accessKey";
        private static final String SECRET_KEY = "secretKey";
        private static final String SQS_ARTICLE_CONTENT = "sqsArticleContent";

        private Map<String, String> configStorage;

        public ConfigService() throws IOException {
            configStorage = ConfigStorage.getInstance(CONFIG_FILE, Map.class, false);
        }

        protected String getAccessKey() {
            return configStorage.get(ACCESS_KEY);
        }

        protected String getSecretKey() {
            return configStorage.get(SECRET_KEY);
        }

        public String getS3Logs() {
            return configStorage.get(S3_LOGS);
        }

        public String getS3Article() {
            return configStorage.get(S3_ARTICLE);
        }

        public String getS3Site() {
            return configStorage.get(S3_SITE);
        }

        public String getSQSArticle() {
            return configStorage.get(SQS_ARTICLE_CONTENT);
        }

        public String getSQSSite() {
            return configStorage.get(SQS_SITE);
        }

        public String getS3LogsStat() {
            return configStorage.get(S3_LOGS_STAT);
        }
    }

    public class S3Service {
        public static final String S3_NAME_DELIMETER = "-";
        public static final String S3_NAME_SUFFIX = ".json";

        private AmazonS3 s3;

        public AmazonS3 getS3() throws IOException {
            if (s3 == null)
                s3 = new AmazonS3Client(getCredentials());
            return s3;
        }

        public void createBucket(String bucket) throws IOException {
            //TODO MINOR try to use bucket if exist to get exception if no rights
            if (!getS3().doesBucketExist(bucket))
                getS3().createBucket(bucket);

        }

        public void deleteBucket(String bucketName) throws IOException {
            AmazonS3 s3 = getS3();
            ObjectListing objectListing = s3.listObjects(bucketName);
            do {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
                    s3.deleteObject(bucketName, objectSummary.getKey());
                objectListing.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
            s3.deleteBucket(bucketName);
        }

        public <T> T getJSONObject(String bucket, String key, Class<T> clazz) {
            T obj = null;
            try {
                obj = JsonHelper.parseJson(getS3().getObject(bucket, key).getObjectContent(), clazz);
            } catch (Exception e) {
                //do nothing and return null
            }
            return obj;
        }

        public <T> T getJSONObject(String bucket, String key, TypeReference ref) {
            T obj = null;
            try {
                obj = JsonHelper.parseJson(getS3().getObject(bucket, key).getObjectContent(), ref);
            } catch (Exception e) {
                //do nothing and return null
            }
            return obj;
        }

        public <T> void saveJSONObject(String bucket, String key, T obj) throws IOException {
            saveJSONObject(bucket, key, obj, new ObjectMetadata());
        }

        public <T> void saveJSONObject(String bucket, String key, T obj, ObjectMetadata metadata) throws IOException {
            InputStream inStream = HttpHelper.stringToInputStream(JsonHelper.parseObject(obj));
            metadata.setContentLength(inStream.available());
            getS3().putObject(bucket, key, inStream, metadata);
        }

    }

    public class SQSService {
        private AmazonSQS sqs;

        public String getQueueURL(String queueName) {
            return getSQS().getQueueUrl(new GetQueueUrlRequest(queueName)).getQueueUrl();
        }

        public AmazonSQS getSQS() {
            if (sqs == null) {
                sqs = new AmazonSQSClient(getCredentials(), new ClientConfiguration());
                sqs.setEndpoint("https://sqs.us-east-1.amazonaws.com");
            }
            return sqs;
        }

        public void createQueue(String queueName) {
            //TODO MINOR try to use queue if exist to get exception if no rights
            List<String> urls = getSQS().listQueues().getQueueUrls();
            for (String url : urls)
                if (url.equals(queueName))
                    return;

            CreateQueueRequest request = new CreateQueueRequest();
            request.setQueueName(queueName);
            getSQS().createQueue(request);
        }

        public void deleteQueue(String queueName) throws IOException {
            getSQS().deleteQueue(new DeleteQueueRequest(getQueueURL(queueName)));
        }

        public void sendMessage(String queueName, Object obj) throws IOException {
            getSQS().sendMessage(new SendMessageRequest(getQueueURL(queueName), BinaryUtils.toBase64(JsonHelper.parseObject(obj).getBytes("UTF-8"))));
        }

        public ReceiveMessageResult receiveMessage(String queueName, int timeout) {
            ReceiveMessageRequest request = new ReceiveMessageRequest(getQueueURL(queueName));
            request.setVisibilityTimeout(timeout);
            //TODO MINOR request.setMaxNumberOfMessages();
            return getSQS().receiveMessage(request);
        }

        public ReceiveMessageResult receiveMessage(String queueName) {
            ReceiveMessageRequest request = new ReceiveMessageRequest(getQueueURL(queueName));
            //TODO MINOR request.setMaxNumberOfMessages();
            return getSQS().receiveMessage(request);
        }

        public void deleteMessage(String queueName, String receiptHandle) {
            getSQS().deleteMessage(new DeleteMessageRequest(getQueueURL(queueName), receiptHandle));
        }

        public <T> T decodeMessage(Message m, Class<T> clazz) throws IOException {
            String mBody = m.getBody();
            if (!mBody.startsWith("{")) {
                mBody = new String(BinaryUtils.fromBase64(mBody));
            }
            //mBody = mBody.replace("\\\"", "\"");

            return JsonHelper.parseJson(mBody, clazz);
        }
    }
}
