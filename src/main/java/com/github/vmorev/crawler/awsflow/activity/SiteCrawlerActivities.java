package com.github.vmorev.crawler.awsflow.activity;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.github.vmorev.crawler.beans.Site;

import java.io.IOException;

/**
 * Contract of the hello world activities
 */
@Activities(version = "1.0")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
public interface SiteCrawlerActivities {
    //TODO MAJOR AWS configure timeouts

    public long storeNewArticlesList(Site site) throws Exception;

    public long storeArchivedArticlesList(Site site) throws Exception;

}
