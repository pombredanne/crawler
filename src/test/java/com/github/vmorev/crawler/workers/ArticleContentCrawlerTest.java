package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ArticleContentCrawlerTest extends AbstractAWSTest {
    private ArticleContentCrawler crawler;

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + random.nextLong();
        articleSQSName = helper.getConfig().getSQSQueueArticleContent() + modifier;
        articleS3Name = helper.getConfig().getS3BucketArticle() + modifier;
        helper.getS3().createBucket(articleS3Name);
        helper.getSQS().createQueue(articleSQSName);
        crawler = new ArticleContentCrawler();
        crawler.articleSQSName = articleSQSName;
        crawler.articleS3Name = articleS3Name;
    }

    @Test
    public void testArticleCrawl() throws Exception {
        String fileName = "ArticleContentCrawlerTest.testArticleCrawl.json";
        Article article = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Article.class);
        String key = Article.generateId(article.getSiteId(), article.getUrl());

        helper.getSQS().sendMessage(articleSQSName, article);
        crawler.performWork();

        assertEquals(0, helper.getSQS().receiveMessage(articleSQSName).getMessages().size());
        Article resultedArticle = helper.getS3().getObject(articleS3Name, key, Article.class);
        assertEquals(article.getSiteId(), resultedArticle.getSiteId());
        assertEquals(article.getUrl(), resultedArticle.getUrl());
        assertNotNull(resultedArticle.getText());
    }
}
