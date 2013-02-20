package com.github.vmorev.crawler.beans;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * User: Valentin_Morev
 * Date: 19.02.13
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleMedia {
    private String type;
    private boolean primary;
    private String link;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
