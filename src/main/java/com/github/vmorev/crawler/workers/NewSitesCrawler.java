package com.github.vmorev.crawler.workers;

import com.github.vmorev.amazon.AmazonService;
import com.github.vmorev.amazon.SDBDomain;
import com.github.vmorev.amazon.SQSQueue;
import com.github.vmorev.crawler.beans.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NewSitesCrawler extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(NewSitesCrawler.class);
    protected SQSQueue siteQueue;
    protected SDBDomain siteDomain;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            if (siteQueue == null)
                siteQueue = new SQSQueue(SQSQueue.getConfig().getValue(Site.VAR_SQS_QUEUE));
            if (siteDomain == null) {
                siteDomain = new SDBDomain(SDBDomain.getConfig().getValue(Site.VAR_SDB_DOMAIN));
            }

            siteQueue.receiveMessages(1, 10, Site.class, new AmazonService.ListFunc<Site>() {
                public void process(Site obj) throws Exception {
                    //do nothing, message will be deleted
                }
            });

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
        siteDomain.listObjects("select * from `" + siteDomain.getName() + "`", Site.class, new SDBDomain.ListFunc<Site>() {
            public void process(Site site) {
                sites.add(site);
            }
        });
        return sites;
    }

}
