package com.github.vmorev.crawler.beans;

import com.github.vmorev.crawler.AbstractAWSTest;
import com.github.vmorev.crawler.utils.JsonHelper;
import com.github.vmorev.crawler.utils.amazon.SDBService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * User: Valentin_Morev
 * Date: 18.02.13
 */
public class SDBItemTest extends AbstractAWSTest {
    private SDBService.Domain<Article> articleDomain;

    @Before
    public void setUp() throws Exception {
        String modifier = "-" + random.nextLong();
        articleName = sdb.getConfig().getArticle() + modifier;
        articleDomain = sdb.getDomain(articleName, Article.class);
        articleDomain.createDomain();
    }

    @After
    public void cleanUp() {
        articleDomain.deleteDomain();
    }

    @Test
    public void testFromSDB() throws Exception {

    }
    @Test
    public void testToSDB() throws Exception {
        String fileName = "SDBItemTest.testToSDB.json";
        Article article = JsonHelper.parseJson(ClassLoader.getSystemResource(fileName), Article.class);
        articleDomain.saveObject("testToSDB", article);
        Article article2 = (Article) articleDomain.getObject("testToSDB", true);
    }
}
