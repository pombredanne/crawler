package com.github.vmorev.crawler.sitecrawler;

import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.AWSHelper;
import com.github.vmorev.crawler.utils.DiffbotHelper;
import com.github.vmorev.crawler.utils.HttpHelper;
import com.github.vmorev.crawler.utils.JsonHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
public class DiffbotSiteCrawler implements SiteCrawler {
    private DiffbotHelper diffbotHelper;

    public DiffbotSiteCrawler() throws IOException {
        this.diffbotHelper = new DiffbotHelper();
    }

    /**
     * This method return list of articles received from site
     *
     * @param site - bean with site URL or ID to use
     * @return - list of articles
     * @throws IOException - in case of communication errors
     */
    public List<Article> getNewArticles(Site site) throws IOException {
        String token = diffbotHelper.getToken();

        if (site.getExternalId() == null || site.getExternalId().length() <= 0) {
            String apiUrl = "http://www.diffbot.com/api/add";
            String params = "output=rss&token=" + token + "&url=" + HttpHelper.encode(site.getUrl());
            String response = HttpHelper.postResponse(apiUrl, params);
            site.setExternalId(response.substring(response.indexOf("id=\"") + 4, response.indexOf("\">")));

            AWSHelper helper = new AWSHelper();
            helper.getS3().saveObject(helper.getConfig().getS3BucketSite(), Site.generateId(site.getUrl()), site);
        }

        String apiUrl = "http://www.diffbot.com/api/dfs/dml/archive?output=json&token=" + token + "&id=" + site.getExternalId();
        String response = HttpHelper.getResponse(apiUrl);
        Map responseData = JsonHelper.parseJson(response, Map.class);

        //TODO MINOR DIFFBOT implement jackson dependant streaming api usage
        List<Article> articles = new ArrayList<>();
        for (Map iterItem : ((List<Map>) ((Map) ((List) responseData.get("childNodes")).get(0)).get("childNodes"))) {
            if ("item".equals(iterItem.get("tagName"))) {
                Article article = new Article();
                for (Map props : ((List<Map>) iterItem.get("childNodes"))) {
                    if ("title".equals(props.get("tagName"))) {
                        article.setTitle((String) ((List) props.get("childNodes")).get(0));
                    } else if ("link".equals(props.get("tagName"))) {
                        article.setUrl((String) ((List) props.get("childNodes")).get(0));
                    } else if ("textSummary".equals(props.get("tagName"))) {
                        article.setText((String) ((List) props.get("childNodes")).get(0));
                    } else if ("pubDate".equals(props.get("tagName"))) {
                        article.setcDate((String) ((List) props.get("childNodes")).get(0));
                    }
                }
                article.setSiteId(Site.generateId(site.getUrl()));
                article.setArticleCrawler(site.getNewArticlesCrawler());
                if (article.getUrl() != null)
                    articles.add(article);
            }
        }
        return articles;
    }

    public List<Article> getArchivedArticles(Site site) throws Exception {
        throw new Exception("Not implemented");
    }

    /**
     * This method returns article with filled content crawled from site by url
     *
     * @param article - Article containing url of the article
     * @return - article with filled text
     * @throws IOException in case of communication errors
     */
    public Article getArticle(Article article) throws IOException {
        String token = diffbotHelper.getToken();
        String apiUrl = "http://www.diffbot.com/api/article?token=" + token + "&tags=1&comments=1&summary=1&url=" + HttpHelper.encode(article.getUrl());
        String response = HttpHelper.getResponse(apiUrl);
        Article newArticle = JsonHelper.parseJson(response, Article.class);
        newArticle.setUrl(article.getUrl());
        newArticle.setSiteId(article.getSiteId());
        return newArticle;
    }

}
