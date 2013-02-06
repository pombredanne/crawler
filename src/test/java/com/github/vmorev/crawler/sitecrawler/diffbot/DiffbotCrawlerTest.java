package com.github.vmorev.crawler.sitecrawler.diffbot;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.vmorev.crawler.awsflow.AWSHelper;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import com.github.vmorev.crawler.utils.ConfigStorage;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
public class DiffbotCrawlerTest {
    private AWSHelper awsHelper;
    private AmazonS3 s3;

    @BeforeClass
    public void setUpClass() throws Exception {
        ConfigStorage.updateLogger();
    }

    @Before
    public void setUp() throws IOException {
        awsHelper = new AWSHelper();
        assertTrue(awsHelper.getS3SiteBucket().contains("test"));
        s3 = awsHelper.createS3Client();
        try {
            s3.createBucket(awsHelper.getS3SiteBucket());
        } catch (Exception e) {
            //ignoring
        }
    }

    @After
    public void cleanUp() throws IOException {
        ObjectListing objectListing = s3.listObjects(awsHelper.getS3SiteBucket());
        do {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
                s3.deleteObject(awsHelper.getS3SiteBucket(), objectSummary.getKey());
            objectListing.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        //s3.deleteBucket(awsHelper.getS3SiteBucket());
    }

    /**
     * This test checks if specific article can be crawled by url
     *
     * @throws IOException in case of crawl issue
     */
    @Test
    public void testArticleContent() throws Exception {
        SiteCrawler crawler = new DiffbotSiteCrawler();
        Article requestArticle = new Article();
        requestArticle.setUrl("http://edition.cnn.com/interactive_legal.html");
        Article article = crawler.getArticle(requestArticle);
        assertNotNull(article);
        assertNotNull(article.getText());
        assertTrue(article.getText().contains("(A) Governing Terms"));
        assertTrue(article.getText().contains("consequential, punitive or exemplary) resulting therefrom"));
    }

    /**
     * This test checks if list of articles can be crawled by url
     *
     * @throws IOException in case of crawl issue
     */
    @Test
    public void testArticleList() throws Exception {
        SiteCrawler crawler = new DiffbotSiteCrawler();
        Site site = new Site();
        site.setUrl("http://www.cnn.com");
        site.setExternalId("538");
        List<Article> articles = crawler.getNewArticles(site);
        assertNotNull(articles);
        assertTrue(articles.size() > 0);
        assertNotNull(articles.get(0));
        assertNotNull(articles.get(0).getUrl());
    }

    /**
     * This test checks if list of articles can be crawled by url
     *
     * @throws IOException in case of crawl issue
     */
    @Test
    public void testExternalIdSave() throws Exception {
        String fileName = "testExternalIdSave.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        awsHelper.saveS3Object(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()), site);

        SiteCrawler crawler = new DiffbotSiteCrawler();
        crawler.getNewArticles(site);

        S3Object object = s3.getObject(awsHelper.getS3SiteBucket(), Site.generateId(site.getUrl()));
        Site siteS3 = JsonHelper.parseJson(object.getObjectContent(), Site.class);
        assertEquals(site.getUrl(), siteS3.getUrl());
        assertEquals("538", siteS3.getExternalId());
    }
}
