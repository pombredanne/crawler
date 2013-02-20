package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.model.Bucket;
import com.github.vmorev.amazon.S3Bucket;
import com.github.vmorev.amazon.SDBDomain;
import com.github.vmorev.amazon.SQSQueue;

/**
 * User: valentin
 * Date: 10.02.13
 */
public class Cleaner {

    public static void main(String[] args) throws Exception {
        //cleanS3();
        cleanSQS();
        cleanSDB();
    }

    private static void cleanS3() {
        for (Bucket bucket : S3Bucket.listBuckets()) {
            try {
                new S3Bucket(bucket.getName()).deleteBucket();
                System.out.println("SUCCESS. S3 bucket was deleted " + bucket.getName());
            } catch (Exception e) {
                System.out.println("FAIL. S3 bucket was NOT deleted " + bucket.getName());
            }
        }
    }

    private static void cleanSDB() {
        try {
            SDBDomain.listDomains(new SDBDomain.ListFunc<String>() {
                public void process(String domainName) {
                    new SDBDomain(domainName).deleteDomain();
                    System.out.println("SDB domain was deleted " + domainName);
                }
            });
        } catch (Exception e) {
            System.out.println("FAIL. SQS queues were NOT deleted");
        }
    }

    private static void cleanSQS() {
        for (String url : SQSQueue.listQueues()) {
            String name = url.substring(url.lastIndexOf("/") + 1, url.length());
            new SQSQueue(name).deleteQueue();
            System.out.println("QSQ queue was deleted " + url);
        }
    }
}