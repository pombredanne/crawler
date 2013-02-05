package com.github.vmorev.crawler.awsflow.workflow;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;
import com.github.vmorev.crawler.beans.Site;

@Workflow
@WorkflowRegistrationOptions(defaultExecutionStartToCloseTimeoutSeconds = 600)
public interface SiteCrawlerWorkflow {

    @Execute(version = "1.0")
    public void startSiteTracking(Site site);
}
