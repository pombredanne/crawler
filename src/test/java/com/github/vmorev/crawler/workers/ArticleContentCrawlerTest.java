package com.github.vmorev.crawler.workers;

import com.github.vmorev.amazon.AmazonService;
import com.github.vmorev.amazon.S3Bucket;
import com.github.vmorev.amazon.SQSQueue;
import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Article;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ArticleContentCrawlerTest extends AbstractAWSTest {
    private ArticleContentCrawler crawler;
    private S3Bucket articleBucket;
    private SQSQueue articleQueue;

    @Before
    public void setUp() throws IOException {
        String modifier = "-" + random.nextLong();
        articleBucket = new S3Bucket(S3Bucket.getConfig().getValue(Article.VAR_S3_BUCKET) + modifier);
        articleQueue = new SQSQueue(SQSQueue.getConfig().getValue(Article.VAR_SQS_QUEUE) + modifier);
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
        Article article = new ObjectMapper().readValue(ClassLoader.getSystemResource(fileName), Article.class);
        String key = Article.generateId(article.getUrl());

        articleQueue.sendMessage(article);
        crawler.performWork();

        final long[] count = new long[1];
        articleQueue.receiveMessages(1, 1, Article.class, new AmazonService.ListFunc<Article>() {
            public void process(Article article) throws Exception {
                count[0]++;
            }
        });
        assertEquals(0, count[0]);

        Article resultedArticle = articleBucket.getObject(key, Article.class);
        assertNotNull(resultedArticle);
        assertEquals(article.getSiteId(), resultedArticle.getSiteId());
        assertEquals(article.getUrl(), resultedArticle.getUrl());
        assertNotNull(resultedArticle.getText());
    }
}
