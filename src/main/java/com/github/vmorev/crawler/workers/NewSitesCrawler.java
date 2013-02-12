package com.github.vmorev.crawler.workers;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.model.Message;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.AWSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewSitesCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(NewSitesCrawler.class);
    protected AWSHelper helper;
    protected String siteSQSName;
    protected String siteS3Name;
    protected boolean isTest;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            helper = new AWSHelper();
            if (!isTest) {
                siteSQSName = helper.getConfig().getSQSSite();
                siteS3Name = helper.getConfig().getS3Site();
            }
        } catch (Exception e) {
            String message = "FAIL. " + NewSitesCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }

        try {
            int count;
            do {
                List<Message> messages = helper.getSQS().receiveMessage(siteSQSName).getMessages();
                count = messages.size();
                for (Message m : messages)
                    helper.getSQS().deleteMessage(siteSQSName, m.getReceiptHandle());
            } while (count > 0);

            List<Site> sites = getSitesToCrawl(siteS3Name);
            for (Site site : sites) {
                helper.getSQS().sendMessage(siteSQSName, site);
                log.info("SUCCESS. " + NewSitesCrawler.class.getSimpleName() + ". SITE ADDED TO SQS " + site.getUrl());
            }
        } catch (IOException e) {
            String message = "FAIL. " + NewSitesCrawler.class.getSimpleName() + ". SITE FAILED. Can't put new sites to SQS";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }
    }

    private List<Site> getSitesToCrawl(String bucketName) throws IOException {
        List<Site> sites = new ArrayList<>();
        //get all sites
        //TODO MINOR think of moving to helper
        ObjectListing objectListing = helper.getS3().getS3().listObjects(bucketName);
        do {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                Site site = helper.getS3().getJSONObject(bucketName, objectSummary.getKey(), Site.class);
                //check if it's time to put in queue
                if (site != null && System.currentTimeMillis() > (site.getLastCheckDate() + site.getCheckInterval())) {
                    //site can be in queue already but we will re-check date in consumer
                    sites.add(site);
                }
            }
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        return sites;
    }

}
