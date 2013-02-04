package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.RequestCancelWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.HttpHelper;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class WorkflowExecutionStarterTest {
    private AWSHelper awsHelper;
    private AmazonS3 s3;
    private AmazonSimpleWorkflow swfClient;
    private List<WorkflowExecution> executions;

    @Before
    public void setUp() throws IOException {
        awsHelper = new AWSHelper();
        assertTrue(awsHelper.getS3SiteBucket().contains("test"));
        assertTrue(awsHelper.getSWFDomain().contains("test"));
        s3 = awsHelper.createS3Client();
        try {
            s3.createBucket(awsHelper.getS3SiteBucket());
        } catch (Exception e) {
            //ignoring
        }
        swfClient = awsHelper.createSWFClient();
        executions = new ArrayList<>();
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

        for (WorkflowExecution execution : executions) {
            RequestCancelWorkflowExecutionRequest request = new RequestCancelWorkflowExecutionRequest();
            request.setDomain(awsHelper.getSWFDomain());
            request.setRequestCredentials(awsHelper.getCredentials());
            request.setRunId(execution.getRunId());
            request.setWorkflowId(execution.getWorkflowId());
            swfClient.requestCancelWorkflowExecution(request);
        }
    }

    @Test
    public void testGetSitesWithoutFlow() throws Exception {
        String fileName = "testSitesFlowCreation1.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);

        fileName = "testSitesFlowCreation2.json";
        Site site2 = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site2.getUrl()), site2);

        List<Site> sites = WorkflowExecutionStarter.getSitesWithoutFlow();
        assertNotNull(sites);
        assertEquals(2, sites.size());
        assertTrue(sites.get(0).getUrl().equals(site.getUrl()) || sites.get(0).getUrl().equals(site2.getUrl()));
        assertTrue(sites.get(1).getUrl().equals(site.getUrl()) || sites.get(1).getUrl().equals(site2.getUrl()));
    }

    @Test
    public void testStartSiteFlow() throws Exception {
        String fileName = "testSitesFlowCreation1.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);

        fileName = "testSitesFlowCreation2.json";
        Site site2 = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site2.getUrl()), site2);

        executions.add(WorkflowExecutionStarter.startSiteFlow(site));
        executions.add(WorkflowExecutionStarter.startSiteFlow(site2));

        ObjectMetadata siteMetadata1 = s3.getObjectMetadata(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()));
        String siteFlowId1 = siteMetadata1.getUserMetadata().get(AWSHelper.S3_METADATA_FLOWID);

        ObjectMetadata siteMetadata2 = s3.getObjectMetadata(awsHelper.getS3SiteBucket(), Site.generateId(site2.getUrl()));
        String siteFlowId2 = siteMetadata2.getUserMetadata().get(AWSHelper.S3_METADATA_FLOWID);

        assertNotNull(siteFlowId1);
        assertNotNull(siteFlowId2);
        assertTrue(!siteFlowId1.equals(siteFlowId2));
    }

}
