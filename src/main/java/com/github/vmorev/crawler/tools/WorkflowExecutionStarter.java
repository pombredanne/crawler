package com.github.vmorev.crawler.tools;

import com.amazonaws.services.simpleworkflow.model.GetWorkflowExecutionHistoryRequest;
import com.amazonaws.services.simpleworkflow.model.History;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.awsflow.workflow.*;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;

import java.io.File;
import java.io.IOException;

public class WorkflowExecutionStarter {

    public static void main(String[] args) throws Exception {
        if (!(args.length > 1 && args[0] != null && args[0].length() > 0 && args[1] != null && args[1].length() > 0)) {
            System.out.println("Two parameters required: flow name (site or article) and file name of json file with site or article content");
            System.exit(1);
        }
        String flowName = args[0];
        String paramName = args[1];

        WorkflowExecution workflowExecution;
        if ("article".equals(flowName)) {
            Article article = JsonHelper.parseJson(new File(paramName), Article.class);
            workflowExecution = startArticleFlow(article);
        } else {
            Site site = JsonHelper.parseJson(new File(paramName), Site.class);
            workflowExecution = startSiteFlow(site);
        }

        System.out.println("Started helloWorld workflow with workflowId=\"" + workflowExecution.getWorkflowId()
                + "\" and runId=\"" + workflowExecution.getRunId() + "\"");

        System.out.println(ActivityHoster.class.getSimpleName() + " Service Started...");
        System.out.println("Please press any key to terminate service.");
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static WorkflowExecution startSiteFlow(Site site) throws Exception {
        AWSHelper awsHelper = new AWSHelper();
        SiteCrawlerWorkflowClientExternalFactory clientFactory =
                new SiteCrawlerWorkflowClientExternalFactoryImpl(awsHelper.createSWFClient(), awsHelper.getSWFDomain());
        SiteCrawlerWorkflowClientExternal workflow = clientFactory.getClient();
        workflow.startSiteTracking(site);
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
