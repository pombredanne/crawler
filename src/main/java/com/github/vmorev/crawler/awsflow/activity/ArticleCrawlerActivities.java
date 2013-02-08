package com.github.vmorev.crawler.awsflow.activity;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.github.vmorev.crawler.beans.Article;

@Activities(version = "1.0")
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 60)
public interface ArticleCrawlerActivities {

    public void storeContent(Article article) throws Exception;

    public void storeMetadata(Article article);

}
