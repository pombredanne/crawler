package com.github.vmorev.crawler.sitecrawler;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
public class DiffbotCrawlerTest extends AbstractAWSTest {
    @Before
    public void setUp() throws IOException {
        String modifier = "-" + System.currentTimeMillis();
        siteS3Name = helper.getS3BucketSite() + modifier;
        helper.createS3Bucket(siteS3Name);
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
        String fileName = "DiffbotCrawlerTest.testExternalIdSave.json";
        Site site = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Site.class);
        helper.saveS3Object(helper.getS3BucketSite(), Site.generateId(site.getUrl()), site);

        SiteCrawler crawler = new DiffbotSiteCrawler();
        crawler.getNewArticles(site);

        Site siteS3 = helper.getS3Object(helper.getS3BucketSite(), Site.generateId(site.getUrl()), Site.class);
        assertEquals(site.getUrl(), siteS3.getUrl());
        assertEquals("538", siteS3.getExternalId());
    }
}
