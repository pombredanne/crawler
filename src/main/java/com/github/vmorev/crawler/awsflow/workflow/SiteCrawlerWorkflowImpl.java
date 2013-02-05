package com.github.vmorev.crawler.awsflow.workflow;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.WorkflowClock;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivitiesClient;
import com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivitiesClientImpl;
import com.github.vmorev.crawler.beans.Site;

public class SiteCrawlerWorkflowImpl implements SiteCrawlerWorkflow {

    private DecisionContextProvider contextProvider = new DecisionContextProviderImpl();
    private WorkflowClock clock = contextProvider.getDecisionContext().getWorkflowClock();
    private SiteCrawlerActivitiesClient client = new SiteCrawlerActivitiesClientImpl();
    private SiteCrawlerWorkflowSelfClient selfClient = new SiteCrawlerWorkflowSelfClientImpl();

    public void startSiteTracking(Site site) {
        Promise<Long> isStored = storeNewArticlesList(site);

        if (!site.isArchiveStored()) {
            isStored = storeOldArticlesList(site, isStored);
        }

        //once per 2 hours
        Promise<Void> timer = clock.createTimer(60 * 60 * 2);
        Promise<Site> updatedSite = getUpdatedSite(site);
        continueAsNew(updatedSite, timer, isStored);
    }

    @Asynchronous
    private void continueAsNew(Promise<Site> site, Promise<?>... timers) {
        selfClient.startSiteTracking(site);
    }

    @Asynchronous
    private Promise<Site> getUpdatedSite(Site site) {
        return client.getUpdatedSite(site);
    }

    @Asynchronous
    private Promise<Long> storeOldArticlesList(Site site, Promise<?>... timers) {
        return client.storeArchivedArticlesList(site);
    }

    @Asynchronous
    private Promise<Long> storeNewArticlesList(Site site, Promise<?>... timers) {
        return client.storeNewArticlesList(site);
    }

}