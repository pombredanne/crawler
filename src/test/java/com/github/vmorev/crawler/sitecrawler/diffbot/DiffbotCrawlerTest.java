package com.github.vmorev.crawler.sitecrawler.diffbot;

import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.sitecrawler.SiteCrawler;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
public class DiffbotCrawlerTest {

    /**
     * This test checks if specific article can be crawled by url
     * @throws IOException in case of crawl issue
     */
    @Test public void testArticleContent() throws Exception {
        SiteCrawler crawler = new DiffbotSiteCrawler();
        Article requestArticle = new Article();
        requestArticle.setUrl("http://edition.cnn.com/interactive_legal.html");
        Article article = crawler.getArticle(requestArticle);
        Assert.assertNotNull(article);
        Assert.assertNotNull(article.getText());
        Assert.assertTrue(article.getText().contains("(A) Governing Terms"));
        Assert.assertTrue(article.getText().contains("consequential, punitive or exemplary) resulting therefrom"));
    }

    /**
     * This test checks if list of articles can be crawled by url
     * @throws IOException in case of crawl issue
     */
    @Test public void testArticleList() throws Exception {
        SiteCrawler crawler = new DiffbotSiteCrawler();
        Site site = new Site();
        site.setUrl("http://www.cnn.com");
        site.setExternalId("538");
        List<Article> articles = crawler.getNewArticles(site);
        Assert.assertNotNull(articles);
        Assert.assertTrue(articles.size() > 0);
        Assert.assertNotNull(articles.get(0));
        Assert.assertNotNull(articles.get(0).getUrl());
    }
}
