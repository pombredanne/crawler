package com.github.vmorev.crawler.sitecrawler;

import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.DiffbotHelper;
import com.github.vmorev.crawler.utils.HttpHelper;
import com.github.vmorev.crawler.utils.JsonHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 10.01.13
 */
public class DiffbotSiteCrawler implements SiteCrawler {
    private DiffbotHelper diffbotHelper;
    protected String externalId;

    public DiffbotSiteCrawler() {
        this.diffbotHelper = new DiffbotHelper();
    }

    public String getExternalId() {
        return externalId;
    }

    /**
     * This method return list of articles received from site
     *
     * @param site - bean with site URL or ID to use
     * @return - list of articles
     * @throws IOException - in case of communication errors
     */
    public List<Article> getNewArticles(Site site) throws Exception {
        String token = diffbotHelper.getToken();

        String response = "";
        try {
            if (site.getExternalId() == null || site.getExternalId().length() <= 0) {
                String apiUrl = "http://www.diffbot.com/api/add";
                String params = "output=rss&token=" + token + "&url=" + HttpHelper.encode(site.getUrl());
                response = HttpHelper.postResponse(apiUrl, params);
                externalId = response.substring(response.indexOf("id=\"") + 4, response.indexOf("\">"));
            }
        } catch (IOException e) {
            throw new Exception("Can't get external ID for site, response was: " + response, e);
        }

        List<Article> articles = null;
        try {
            String apiUrl = "http://www.diffbot.com/api/dfs/dml/archive?output=json&token=" + token + "&id=" + (site.getExternalId() == null || site.getExternalId().length() <= 0 ? externalId : site.getExternalId());
            response = HttpHelper.getResponse(apiUrl);
            Map responseData = JsonHelper.parseJson(response, Map.class);

            //TODO MINOR DIFFBOT implement jackson dependant streaming api usage
            articles = new ArrayList<>();
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
                            String formattedDate = (String) ((List) props.get("childNodes")).get(0);
                            //Thu, 10 Jan 2013 11:57:24 GMT
                            article.setDate(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US).parse(formattedDate).getTime());
                        } else if ("sp".equals(props.get("tagName"))) {
                            article.setSpamScore((Double) ((List) props.get("childNodes")).get(0));
                        } else if ("sr".equals(props.get("tagName"))) {
                            article.setStaticRank((Double) ((List) props.get("childNodes")).get(0));
                        } else if ("fresh".equals(props.get("tagName"))) {
                            article.setFresh((Double) ((List) props.get("childNodes")).get(0));
                        }
                    }
                    article.setSiteId(Site.generateId(site.getUrl()));
                    article.setArticleCrawler(site.getNewArticlesCrawler());
                    if (article.getUrl() != null)
                        articles.add(article);
                }
            }
        } catch (Exception e) {
            throw new Exception("Can't get list of articles for site " + site.getUrl() + " response was: " + response, e);
        }
        return articles;
    }

    public List<Article> getArchivedArticles(Site site) throws IOException {
        throw new IOException("Not implemented");
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
        if (newArticle.getText() == null || newArticle.getText().length() <= 0)
            throw new IOException("Diffbot returned Article without content. Response was: " + response);
        newArticle.setUrl(article.getUrl());
        newArticle.setSiteId(article.getSiteId());
        return newArticle;
    }

}
