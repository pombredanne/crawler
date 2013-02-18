package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.model.Bucket;
import com.github.vmorev.crawler.beans.SDBItem;
import com.github.vmorev.crawler.utils.amazon.S3Service;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import com.github.vmorev.crawler.utils.amazon.SQSService;

/**
 * User: valentin
 * Date: 10.02.13
 */
public class Cleaner {

    public static void main(String[] args) throws Exception {
        cleanS3();
        cleanSQS();
        cleanSDB();
    }

    private static void cleanS3() {
        S3Service s3 = new S3Service();
        for (Bucket bucket : s3.listBuckets()) {
            try {
                s3.getBucket(bucket.getName(), Object.class).deleteBucket();
                System.out.println("SUCCESS. S3 bucket was deleted " + bucket.getName());
            } catch (Exception e) {
                System.out.println("FAIL. S3 bucket was NOT deleted " + bucket.getName());
            }
        }
    }

    private static void cleanSDB() {
        final SDBService sdb = new SDBService();
        try {
            sdb.listDomains(new SDBService.ListFunc<String>() {
                public void process(String domainName) {
                    sdb.getDomain(domainName, SDBItem.class).deleteDomain();
                    System.out.println("SDB domain was deleted " + domainName);
                }
            });
        } catch (Exception e) {
            System.out.println("FAIL. SQS queues were NOT deleted");
        }
    }

    private static void cleanSQS() {
        SQSService sqs = new SQSService();
        for (String url : sqs.listQueues()) {
            sqs.getQueue(url, Object.class).deleteQueue();
            System.out.println("QSQ queue was deleted " + url);
        }
    }
}