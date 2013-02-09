package com.github.vmorev.crawler.workers;

import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ArticleContentCrawlerTest extends AbstractAWSTest {

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + System.currentTimeMillis();
        articleSQSName = helper.getSQSQueueArticleContent() + modifier;
        articleS3Name = helper.getS3BucketArticle() + modifier;
        helper.createS3Bucket(articleS3Name);
        helper.createSQSQueue(articleSQSName);
    }

    @Test
    public void testArticleCrawl() throws Exception {
        String fileName = "ArticleContentCrawlerTest.testArticleCrawl.json";
        Article article = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Article.class);
        String key = Article.generateId(article.getSiteId(), article.getUrl());

        helper.getSQS().sendMessage(new SendMessageRequest(articleSQSName, JsonHelper.parseObject(article)));
        (new ArticleContentCrawler()).performWork();

        assertEquals(0, helper.getSQS().receiveMessage(new ReceiveMessageRequest(articleSQSName)).getMessages().size());
        Article resultedArticle = helper.getS3Object(articleS3Name, key, Article.class);
        assertEquals(article.getSiteId(), resultedArticle.getSiteId());
        assertEquals(article.getUrl(), resultedArticle.getUrl());
        assertNotNull(resultedArticle.getText());
    }
}
