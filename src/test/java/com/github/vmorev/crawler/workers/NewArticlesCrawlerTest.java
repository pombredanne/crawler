package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NewArticlesCrawlerTest extends AbstractAWSTest {
    private NewArticlesCrawler crawler;

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + random.nextLong();
        siteSQSName = helper.getConfig().getSQSSite() + modifier;
        articleSQSName = helper.getConfig().getSQSArticle() + modifier;
        siteS3Name = helper.getConfig().getS3Site() + modifier;
        helper.getSQS().createQueue(siteSQSName);
        helper.getSQS().createQueue(articleSQSName);
        helper.getS3().createBucket(siteS3Name);
        crawler = new NewArticlesCrawler();
        crawler.siteS3Name = siteS3Name;
        crawler.siteSQSName = siteSQSName;
        crawler.articleSQSName = articleSQSName;
        crawler.isTest = true;
    }

    @Test
    public void testSiteCrawl() throws Exception {
        String fileName = "NewArticlesCrawler.testSiteCrawl.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);

        helper.getSQS().sendMessage(siteSQSName, site);
        crawler.performWork();

        assertEquals(0, helper.getSQS().receiveMessage(siteSQSName).getMessages().size());
        assertTrue(helper.getSQS().receiveMessage(articleSQSName).getMessages().size() > 0);
    }
}
