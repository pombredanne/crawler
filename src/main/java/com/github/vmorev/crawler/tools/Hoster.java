package com.github.vmorev.crawler.tools;

import com.github.vmorev.crawler.beans.HosterConfig;
import com.github.vmorev.crawler.beans.SDBItem;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.ConfigStorage;
import com.github.vmorev.crawler.utils.HttpHelper;
import com.github.vmorev.crawler.utils.JsonHelper;
import com.github.vmorev.crawler.utils.amazon.S3Service;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import com.github.vmorev.crawler.utils.amazon.SQSService;
import com.github.vmorev.crawler.workers.AbstractWorker;
import com.github.vmorev.crawler.workers.WorkerService;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Hoster {
    private static WorkerService service;
    private static final Logger log = LoggerFactory.getLogger(Hoster.class);
    protected static SDBService.Domain<Site> siteDomain;

    public static void main(String[] args) throws Exception {
        //update logger with local config if present
        ConfigStorage.updateLogger();

        initAmazon();

        //load config
        String hosterFileName = "hoster.json";
        if (args.length > 0 && args[0] != null && args[0].length() > 0)
            hosterFileName = args[0];
        HosterConfig hoster = ConfigStorage.getInstance(hosterFileName, HosterConfig.class, false);

        //update or add sites to sdb
        saveSites(hoster.getSitesFileName());

        //finish if no workers are configured
        List<Map> workers = hoster.getWorkers();
        if (workers.size() == 0) {
            System.out.println("No workers configured to be started, exiting");
            System.exit(0);
        }

        //start workers
        service = new WorkerService();
        for (Map worker : workers)
            startWorker(worker);

        //loop for log heartbeat and wait
        while (!service.getExecutor().isTerminated()) {
            try {
                Thread.sleep(hoster.getHosterSleepInterval());
                logHeartBeat();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void initAmazon() throws Exception {
        S3Service s3 = new S3Service();

        s3.getBucket(s3.getConfig().getLogs(), Object.class).createBucket();
        s3.getBucket(s3.getConfig().getLogsStat(), Object.class).createBucket();
        s3.getBucket(s3.getConfig().getSite(), Object.class).createBucket();
        s3.getBucket(s3.getConfig().getArticle(), Object.class).createBucket();

        SQSService sqs = new SQSService();
        sqs.getQueue(sqs.getConfig().getArticle(), Object.class).createQueue();
        sqs.getQueue(sqs.getConfig().getSite(), Object.class).createQueue();

        SDBService sdb = new SDBService();
        sdb.getDomain(sdb.getConfig().getSite(), SDBItem.class).createDomain();
        sdb.getDomain(sdb.getConfig().getArticle(), SDBItem.class).createDomain();
    }

    protected static void saveSites(String sitesFileName) {
        if (!(sitesFileName != null && sitesFileName.length() > 0))
            return;

        try {
            List<Site> sites = JsonHelper.parseJson(HttpHelper.inputStreamToString(ClassLoader.getSystemResource(sitesFileName).openStream(), "UTF8"), new TypeReference<List<Site>>() {
            });

            SDBService sdb = new SDBService();
            if (siteDomain == null)
                sdb.getDomain(sdb.getConfig().getSite(), Site.class);

            for (Site site : sites) {
                //TODO do not rewrite if exist
                siteDomain.saveObject(Site.generateId(site.getUrl()), site);
                log.info("SUCCESS. " + Hoster.class.getSimpleName() + ". SITE ADDED TO SDB " + site.getUrl());
            }
        } catch (IOException e) {
            log.error("FAIL. " + Hoster.class.getSimpleName() + ". SITE FAILED. Can't put new sites to SDB", e);
        }
    }

    private static void startWorker(Map worker) {
        String className = (String) worker.get("className");
        try {
            if (className != null && className.length() > 0)
                for (int i = 0; i < (Integer) worker.get("threads"); i++) {
                    AbstractWorker workerThread = (AbstractWorker) Class.forName(className).newInstance();
                    try {
                        int sleepTime = (Integer) worker.get("sleepTime");
                        workerThread.setSleepTime(sleepTime);
                    } catch (Exception e) {
                        //just ignore
                    }
                    service.getExecutor().execute(workerThread);
                }
            else
                throw new Exception("Worker className is null");
        } catch (Exception e) {
            log.error("FAIL. " + Hoster.class.getSimpleName() + ". WORKER FAILED. " + className, e);
        }
    }

    private static void logHeartBeat() {
        log.trace("STATUS. " + Hoster.class.getSimpleName() + ". WORKING");
    }

}
