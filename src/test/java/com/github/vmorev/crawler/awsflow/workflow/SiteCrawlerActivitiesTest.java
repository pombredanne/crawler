package com.github.vmorev.crawler.awsflow.workflow;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.amazonaws.services.simpleworkflow.model.History;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivities;
import com.github.vmorev.crawler.awsflow.activity.SiteCrawlerActivitiesImpl;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.tools.ActivityHoster;
import com.github.vmorev.crawler.tools.WorkflowExecutionStarter;
import com.github.vmorev.crawler.tools.WorkflowHoster;
import com.github.vmorev.crawler.utils.ConfigStorage;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import static org.junit.Assert.*;

public class SiteCrawlerActivitiesTest {
    private AWSHelper awsHelper;
    private AmazonS3 s3;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigStorage.updateLogger();
    }

    @Before
    public void setUp() throws IOException {
        awsHelper = new AWSHelper();
        assertTrue(awsHelper.getS3SiteBucket().contains("test"));
        s3 = awsHelper.createS3Client();
        if (!s3.doesBucketExist(awsHelper.getS3SiteBucket()))
            s3.createBucket(awsHelper.getS3SiteBucket());
    }

    @After
    public void cleanUp() throws IOException {
        ObjectListing objectListing = s3.listObjects(awsHelper.getS3SiteBucket());
        do {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
                s3.deleteObject(awsHelper.getS3SiteBucket(), objectSummary.getKey());
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        //s3.deleteBucket(awsHelper.getS3SiteBucket());
    }

    @Test
    public void testStoreNewArticlesList() throws Exception {
        String fileName = "testStoreNewArticlesList.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);
        SiteCrawlerActivities activities = new SiteCrawlerActivitiesImpl();
        activities.storeNewArticlesList(site);
    }

    @Test
    public void testActualFlow() throws Exception {
        String fileName = "testActualFlow.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);

        WorkflowExecution wfExecution = WorkflowExecutionStarter.startSiteFlow(site);
        WorkflowWorker workflowWorker = WorkflowHoster.hostWorkflow(SiteCrawlerWorkflowImpl.class);
        ActivityWorker activityWorker = ActivityHoster.hostActivity(SiteCrawlerActivitiesImpl.class);

        int retryCount = 0;
        String result = "";
        while (retryCount < 10) {
            synchronized (this) {
                wait(4000);
            }
            History history = WorkflowExecutionStarter.getStatus(wfExecution);
            result = history.getEvents().get(0).getEventType();
            if (result.startsWith("WorkflowExecution"))
                retryCount = 10;
            retryCount++;
        }
        Assert.assertEquals("WorkflowExecutionFailed", result);
    }

    @Test
    public void storeArchivedArticlesList() {

    }

}
