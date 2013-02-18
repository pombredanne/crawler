package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.amazon.AmazonService;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import com.github.vmorev.crawler.utils.amazon.SQSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NewSitesCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(NewSitesCrawler.class);
    protected SQSService.Queue<Site> siteQueue;
    protected SDBService.Domain<Site> siteDomain;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            SQSService sqs = new SQSService();
            SDBService sdb = new SDBService();

            if (siteQueue == null)
                siteQueue = sqs.getQueue(sqs.getConfig().getSite(), Site.class);
            if (siteDomain == null) {
                siteDomain = sdb.getDomain(sdb.getConfig().getSite(), Site.class);
            }

            siteQueue.receiveMessages(new AmazonService.ListFunc<Site>() {
                public void process(Site obj) throws Exception {
                    //do nothing, message will be deleted
                }
            }, 1, 1000);

            List<Site> sites = getSitesToCrawl();
            for (Site site : sites) {
                try {
                    siteQueue.sendMessage(site);
                    log.info("SUCCESS. " + NewSitesCrawler.class.getSimpleName() + ". SITE ADDED TO SQS " + site.getUrl());
                } catch (Exception e) {
                    String message = "FAIL. " + NewSitesCrawler.class.getSimpleName() + ". SITE FAILED. Can't put new sites to SQS";
                    log.error(message, e);
                    throw new ExecutionFailureException(message, e);
                }
            }
        } catch (Exception e) {
            String message = "FAIL. " + NewSitesCrawler.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }
    }

    private List<Site> getSitesToCrawl() throws Exception {
        final List<Site> sites = new ArrayList<>();
        //get all sites
        siteDomain.listObjects("select * from " + siteDomain.getName(), new SDBService.ListFunc<Site>() {
            public void process(Site site) {
                sites.add(site);
            }
        });
        return sites;
    }

}
