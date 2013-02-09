package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.vmorev.crawler.beans.HosterConfig;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.AWSHelper;
import com.github.vmorev.crawler.utils.ConfigStorage;
import com.github.vmorev.crawler.utils.HttpHelper;
import com.github.vmorev.crawler.utils.JsonHelper;
import com.github.vmorev.crawler.workers.WorkerService;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Hoster {
    private static WorkerService service;
    private static final Logger log = LoggerFactory.getLogger(Hoster.class);
    protected static AWSHelper helper;
    protected static String siteSQSName;
    protected static String siteS3Name;

    public static void main(String[] args) throws Exception {
        //update logger with local config if present
        ConfigStorage.updateLogger();

        helper = new AWSHelper();

        //load config
        String hosterFileName = "hoster.json";
        if (args[0] != null && args[0].length() > 0)
            hosterFileName = args[0];
        HosterConfig hoster = ConfigStorage.getInstance(hosterFileName, HosterConfig.class, false);

        //update or add sites to s3
        saveSites(hoster.getSitesFileName());

        //finish if no workers are configured
        List<HosterConfig.Worker> workers = hoster.getWorkers();
        if (workers.size() == 0) {
            System.out.println("No workers configured to be started, exiting");
            System.exit(0);
        }

        helper.getSQS().createQueue(helper.getConfig().getSQSQueueArticleContent());
        helper.getSQS().createQueue(helper.getConfig().getSQSQueueSite());

        //start workers
        service = new WorkerService();
        for (HosterConfig.Worker worker : workers)
            startWorker(worker);

        //loop for push sites to sqs, wait, log current status
        while (!service.getExecutor().isTerminated()) {
            try {
                checkSites();
                Thread.sleep(hoster.getHosterSleepInterval());
                logStatus();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected static void saveSites(String sitesFileName) {
        if (!(sitesFileName != null && sitesFileName.length() > 0))
            return;
        if (siteS3Name == null)
            siteS3Name = helper.getConfig().getS3BucketSite();
        try {
            List<Site> sites = JsonHelper.parseJson(HttpHelper.inputStreamToString(ClassLoader.getSystemResource(sitesFileName).openStream(), "UTF8"), new TypeReference<List<Site>>() {
            });
            for (Site site : sites) {
                if (helper.getS3().getObject(siteS3Name, Site.generateId(site.getUrl()), Site.class) == null) {
                    helper.getS3().saveObject(siteS3Name, Site.generateId(site.getUrl()), site);
                    log.info("SUCCESS. " + Hoster.class.getSimpleName() + ". SITE ADDED TO S3 " + site.getUrl());
                }
            }
        } catch (IOException e) {
            log.error("FAIL. " + Hoster.class.getSimpleName() + ". SITE FAILED. Can't put new sites to S3", e);
        }
    }

    private static void startWorker(HosterConfig.Worker worker) {
        String className = worker.getClassName();
        try {
            if (className != null && className.length() > 0)
                for (int i = 0; i < worker.getThreads(); i++)
                    service.getExecutor().execute((Runnable) Class.forName(className).newInstance());
            else
                throw new Exception("Worker className is null");
        } catch (Exception e) {
            log.error("FAIL. " + Hoster.class.getSimpleName() + ". WORKER FAILED. " + className, e);
        }
    }

    protected static void checkSites() {
        if (siteSQSName == null)
            siteSQSName = helper.getConfig().getSQSQueueSite();
        try {
            List<Site> sites = getSitesToCrawl();
            for (Site site : sites) {
                helper.getSQS().sendMessage(siteSQSName, site);
                log.info("SUCCESS. " + Hoster.class.getSimpleName() + ". SITE ADDED TO SQS " + site.getUrl());
            }
        } catch (IOException e) {
            log.error("FAIL. " + Hoster.class.getSimpleName() + ". SITE FAILED. Can't put new sites to SQS", e);
        }
    }

    private static List<Site> getSitesToCrawl() throws IOException {
        if (siteS3Name == null)
            siteS3Name = helper.getConfig().getS3BucketSite();
        List<Site> sites = new ArrayList<>();
        //get all sites
        //TODO think of moving to helper
        ObjectListing objectListing = helper.getS3().getS3().listObjects(siteS3Name);
        do {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                Site site = helper.getS3().getObject(siteS3Name, objectSummary.getKey(), Site.class);
                //check if it's time to put in queue
                if (System.currentTimeMillis() > (site.getLastCheckDate() + site.getCheckInterval())) {
                    //site can be in queue already but we will re-check date in consumer
                    sites.add(site);
                }
            }
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        return sites;
    }

    private static void logStatus() {
        log.trace("STATUS. " + Hoster.class.getSimpleName() + ". WORKING");
    }

}
