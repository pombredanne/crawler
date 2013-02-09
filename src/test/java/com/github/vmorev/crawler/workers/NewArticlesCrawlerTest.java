package com.github.vmorev.crawler.workers;

import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NewArticlesCrawlerTest extends AbstractAWSTest {

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + System.currentTimeMillis();
        siteSQSName = helper.getSQSQueueSite() + modifier;
        articleSQSName = helper.getSQSQueueArticleContent() + modifier;
        helper.createSQSQueue(siteSQSName);
        helper.createSQSQueue(articleSQSName);
    }

    @Test
    public void testSiteCrawl() throws Exception {
        String fileName = "NewArticlesCrawler.testSiteCrawl.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        String key = Site.generateId(site.getUrl());

        helper.getSQS().sendMessage(new SendMessageRequest(siteSQSName, JsonHelper.parseObject(site)));
        (new NewArticlesCrawler()).performWork();

        assertEquals(0, helper.getSQS().receiveMessage(new ReceiveMessageRequest(siteSQSName)).getMessages().size());
        assertTrue(helper.getSQS().receiveMessage(new ReceiveMessageRequest(articleSQSName)).getMessages().size() > 0);
    }
}
