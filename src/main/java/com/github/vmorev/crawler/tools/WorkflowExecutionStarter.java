package com.github.vmorev.crawler.tools;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpleworkflow.model.*;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.awsflow.workflow.*;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkflowExecutionStarter {

    public static void main(String[] args) throws Exception {
        if (!(args.length > 1 && args[0] != null && args[0].length() > 0 && args[1] != null && args[1].length() > 0
                && (args[0].equals("site") || args[0].equals("article") || args[0].equals("sites")))) {
            System.out.println("Two parameters required: flow name (site, sites or article) and file name of json file with site or article content");
            System.out.println("Another option is to start flows for all sites by calling \"sites all\" as parameters to this tool without quotes");
            System.exit(1);
        }
        String flowName = args[0];
        String paramName = args[1];

        WorkflowExecution workflowExecution;
        AWSHelper awsHelper = new AWSHelper();
        if ("article".equals(flowName)) {
            AmazonS3 s3 = awsHelper.createS3Client();
            if (!s3.doesBucketExist(awsHelper.getS3ArticleBucket()))
                s3.createBucket(awsHelper.getS3ArticleBucket());

            Article article = JsonHelper.parseJson(new File(paramName), Article.class);

            //Saving article on S3
            awsHelper.saveS3Object(awsHelper.getS3ArticleBucket(), Article.generateId(awsHelper.getS3ArticleBucket(), article.getUrl()), article);

            workflowExecution = startArticleFlow(article);
            System.out.println("Started article workflow with workflowId=\"" + workflowExecution.getWorkflowId()
                    + "\" and runId=\"" + workflowExecution.getRunId() + "\" for " + paramName);
        } else {
            AmazonS3 s3 = awsHelper.createS3Client();
            if (!s3.doesBucketExist(awsHelper.getS3SiteBucket()))
                s3.createBucket(awsHelper.getS3SiteBucket());

            if ("sites".equalsIgnoreCase(flowName) && "all".equalsIgnoreCase(paramName)) {
                List<Site> sites = getSitesWithoutFlow();
                for (Site site : sites) {
                    workflowExecution = startSiteFlow(site);
                    System.out.println("Started site workflow with workflowId=\"" + workflowExecution.getWorkflowId()
                            + "\" and runId=\"" + workflowExecution.getRunId() + "\" for " + site.getUrl());
                }
            } else if ("site".equalsIgnoreCase(flowName)) {
                Site site = JsonHelper.parseJson(new File(paramName), Site.class);

                //Saving site on S3
                awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);

                workflowExecution = startSiteFlow(site);
                System.out.println("Started site workflow with workflowId=\"" + workflowExecution.getWorkflowId()
                        + "\" and runId=\"" + workflowExecution.getRunId() + "\" for " + paramName);
            } else if ("sites".equalsIgnoreCase(flowName)) {
                List<Site> sites = JsonHelper.parseJson(new File(paramName), new TypeReference<List<Site>>() { });

                //Saving site on S3
                for (Site site : sites) {
                    awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);
                    workflowExecution = startSiteFlow(site);
                    System.out.println("Started site workflow with workflowId=\"" + workflowExecution.getWorkflowId()
                            + "\" and runId=\"" + workflowExecution.getRunId() + "\" for " + paramName);
                }
            }
        }

        System.out.println("Please press ENTER key to terminate service.");
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static List<Site> getSitesWithoutFlow() throws IOException {
        List<Site> sites = new ArrayList<>();
        AWSHelper awsHelper = new AWSHelper();
        AmazonS3 s3 = awsHelper.createS3Client();
        ObjectListing objectListing = s3.listObjects(awsHelper.getS3SiteBucket());
        do {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                ObjectMetadata objectMetadata = s3.getObjectMetadata(awsHelper.getS3SiteBucket(), objectSummary.getKey());
                String flowId = objectMetadata.getUserMetadata().get(AWSHelper.S3_METADATA_FLOWID);

                boolean isFlowExist = flowId != null && flowId.length() > 0;
                if (isFlowExist) {
                    CountOpenWorkflowExecutionsRequest request = new CountOpenWorkflowExecutionsRequest();
                    request.setDomain(awsHelper.getSWFDomain());
                    WorkflowExecutionFilter filter = new WorkflowExecutionFilter();
                    filter.setWorkflowId(flowId);
                    request.setExecutionFilter(filter);
                    if (awsHelper.createSWFClient().countOpenWorkflowExecutions(request).getCount() < 1)
                        isFlowExist = false;
                }
                if (!isFlowExist)
                    sites.add(JsonHelper.parseJson(s3.getObject(awsHelper.getS3SiteBucket(), objectSummary.getKey()).getObjectContent(), Site.class));
            }
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        return sites;
    }

    public static WorkflowExecution startSiteFlow(Site site) throws Exception {
        AWSHelper awsHelper = new AWSHelper();
        SiteCrawlerWorkflowClientExternalFactory clientFactory =
                new SiteCrawlerWorkflowClientExternalFactoryImpl(awsHelper.createSWFClient(), awsHelper.getSWFDomain());
        SiteCrawlerWorkflowClientExternal workflow = clientFactory.getClient();
        workflow.startSiteTracking(site);
        AmazonS3 s3 = awsHelper.createS3Client();
        ObjectMetadata objectMetadata = s3.getObjectMetadata(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()));
        objectMetadata.getUserMetadata().put(AWSHelper.S3_METADATA_FLOWID, workflow.getWorkflowExecution().getWorkflowId());
        awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site, objectMetadata);
        return workflow.getWorkflowExecution();
    }

    public static WorkflowExecution startArticleFlow(Article article) throws Exception {
        AWSHelper awsHelper = new AWSHelper();
        ArticleCrawlerWorkflowClientExternalFactory clientFactory =
                new ArticleCrawlerWorkflowClientExternalFactoryImpl(awsHelper.createSWFClient(), awsHelper.getSWFDomain());
        ArticleCrawlerWorkflowClientExternal workflow = clientFactory.getClient();
        workflow.startArticleProcessing(article);
        return workflow.getWorkflowExecution();
    }

    public static History getStatus(WorkflowExecution execution) throws IOException {
        AWSHelper awsHelper = new AWSHelper();

        GetWorkflowExecutionHistoryRequest historyRequest = new GetWorkflowExecutionHistoryRequest();
        historyRequest.setDomain(awsHelper.getSWFDomain());
        historyRequest.setExecution(execution);
        historyRequest.setReverseOrder(true);

        return awsHelper.createSWFClient().getWorkflowExecutionHistory(historyRequest);
    }

}
