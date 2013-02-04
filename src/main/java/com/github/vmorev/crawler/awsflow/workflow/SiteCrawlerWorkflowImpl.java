package com.github.vmorev.crawler.awsflow.workflow;

import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivitiesClient;
import com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivitiesClientImpl;
import com.github.vmorev.crawler.beans.Site;

public class SiteCrawlerWorkflowImpl implements SiteCrawlerWorkflow {

    SiteCrawlerActivitiesClient client = new SiteCrawlerActivitiesClientImpl();

    public void startSiteTracking(Site site) {
        if (!site.isArchiveStored()) {
            Promise<Long> articlesStored = storeOldArticlesList(site);
            if (articlesStored.get() == 0) {
                //TODO MAJOR AWS notify of archive storing failure
            }
        }

        Promise<Long> articlesStored = storeNewArticlesList(site);
        if (articlesStored.get() == 0) {
            //TODO MAJOR AWS notify of archive storing failure
        } else {
            //TODO MAJOR AWS restart job in an hour
        }
    }

    @Asynchronous
    private Promise<Long> storeOldArticlesList(Site site) {
        return client.storeArchivedArticlesList(site);
    }

    @Asynchronous
    private Promise<Long> storeNewArticlesList(Site site) {
        return client.storeNewArticlesList(site);
    }

}