package com.github.vmorev.crawler.awsflow.activity;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.github.vmorev.crawler.beans.Article;

/**
 * Contract of the hello world activities
 */
@Activities(version = "1.0")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
public interface ArticleCrawlerActivities {
    //TODO MAJOR AWS configure timeouts

    public boolean storeContent(Article article);

    public boolean storeMetadata(Article article);

}
