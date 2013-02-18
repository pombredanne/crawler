package com.github.vmorev.crawler.sitecrawler;

import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;

import java.util.List;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
public interface SiteCrawler {

    public List<Article> getNewArticles(Site site) throws Exception;

    public List<Article> getArchivedArticles(Site site) throws Exception;

    public Article getArticle(Article article) throws Exception;
}
