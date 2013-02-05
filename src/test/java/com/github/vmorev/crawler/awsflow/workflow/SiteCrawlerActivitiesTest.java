package com.github.vmorev.crawler.awsflow.workflow;

import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.amazonaws.services.simpleworkflow.model.History;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivities;
import com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivitiesImpl;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.tools.ActivityHoster;
import com.github.vmorev.crawler.tools.WorkflowExecutionStarter;
import com.github.vmorev.crawler.tools.WorkflowHoster;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SiteCrawlerActivitiesTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testStoreNewArticlesList() throws Exception {
        String fileName = "testStoreNewArticlesList.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        SiteCrawlerActivities activities = new SiteCrawlerActivitiesImpl();
        activities.storeNewArticlesList(site);
    }

    @Test
    public void storeArchivedArticlesList() {

    }

    @Test
    public void testActualFlow() throws Exception {
        String fileName = "testActualFlow.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);

        WorkflowExecution wfExecution = WorkflowExecutionStarter.startSiteFlow(site);
        WorkflowWorker workflowWorker = WorkflowHoster.hostWorkflow(SiteCrawlerWorkflowImpl.class);
        ActivityWorker activityWorker = ActivityHoster.hostActivity(SiteCrawlerActivitiesImpl.class);
        synchronized (this) {
            wait(4000);
        }
        History history = WorkflowExecutionStarter.getStatus(wfExecution);
        Assert.assertEquals("WorkflowExecutionCompleted", history.getEvents().get(0).getEventType());
    }
}
