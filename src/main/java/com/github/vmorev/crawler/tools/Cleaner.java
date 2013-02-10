package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.github.vmorev.crawler.utils.AWSHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: valentin
 * Date: 10.02.13
 */
public class Cleaner {
    private static AWSHelper helper;
    private static List<String> exceptionsBuckets;
    private static List<String> exceptionsQueues;

    public static void main(String[] args) throws Exception {
        helper = new AWSHelper();
        exceptionsBuckets = new ArrayList<>();
//        exceptionsBuckets.add(helper.getConfig().getS3BucketSite());
//        exceptionsBuckets.add(helper.getConfig().getS3BucketArticle());
        exceptionsQueues = new ArrayList<>();
//        exceptionsQueues.add(helper.getSQS().getQueueURL(helper.getConfig().getSQSQueueSite()));
//        exceptionsQueues.add(helper.getSQS().getQueueURL(helper.getConfig().getSQSQueueArticleContent()));
        deleteAllBuckets();
        deleteAllQueues();
    }

    private static void deleteAllBuckets() throws IOException {
        for (Bucket bucket : helper.getS3().getS3().listBuckets()) {
            if (!exceptionsBuckets.contains(bucket.getName())) {
                helper.getS3().deleteBucket(bucket.getName());
                System.out.println("Bucket was deleted " + bucket.getName());
            }
        }
    }

    private static void deleteAllQueues() {
        for (String url : helper.getSQS().getSQS().listQueues().getQueueUrls()) {
            if (!exceptionsQueues.contains(url)) {
                helper.getSQS().getSQS().deleteQueue(new DeleteQueueRequest(url));
                System.out.println("Queue was deleted " + url);
            }
        }
    }
}