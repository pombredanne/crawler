package com.github.vmorev.crawler.workers;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.utils.JsonHelper;
import com.github.vmorev.crawler.utils.amazon.AmazonService;
import com.github.vmorev.crawler.utils.amazon.S3Service;
import com.github.vmorev.crawler.utils.amazon.SQSService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ArticleContentCrawlerTest extends AbstractAWSTest {
    private ArticleContentCrawler crawler;
    private S3Service.S3Bucket<Article> articleBucket;
    private SQSService.Queue<Article> articleQueue;

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + random.nextLong();
        articleName = s3.getConfig().getArticle() + modifier;
        articleBucket = s3.getBucket(articleName, Article.class);
        articleQueue = sqs.getQueue(articleName, Article.class);
        articleBucket.createBucket();
        articleQueue.createQueue();
        crawler = new ArticleContentCrawler();
        crawler.articleQueue = articleQueue;
        crawler.articleBucket = articleBucket;
    }

    @After
    public void cleanUp() throws Exception {
        articleQueue.deleteQueue();
        articleBucket.deleteBucket();
    }

    @Test
    public void testArticleCrawl() throws Exception {
        String fileName = "ArticleContentCrawlerTest.testArticleCrawl.json";
        Article article = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Article.class);
        String key = Article.generateId(article.getUrl());

        articleQueue.sendMessage(article);
        crawler.performWork();

        final long[] count = new long[1];
        articleQueue.receiveMessages(new AmazonService.ListFunc<Article>() {
            public void process(Article article) throws Exception {
                count[0]++;
            }
        }, 1, 1);
        assertEquals(0, count[0]);

        Article resultedArticle = articleBucket.getObject(key);
        assertNotNull(resultedArticle);
        assertEquals(article.getSiteId(), resultedArticle.getSiteId());
        assertEquals(article.getUrl(), resultedArticle.getUrl());
        assertNotNull(resultedArticle.getText());
    }
}
