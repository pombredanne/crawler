package com.github.vmorev.crawler.awsflow.activity;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;
import com.github.vmorev.crawler.beans.Site;

import java.io.IOException;

@Activities(version = "1.0")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 600)
public interface SiteCrawlerActivities {

    public long storeNewArticlesList(Site site) throws Exception;

    public long storeArchivedArticlesList(Site site) throws Exception;

    public Site getUpdatedSite(Site site) throws IOException;
}
