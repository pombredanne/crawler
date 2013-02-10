package com.github.vmorev.crawler.beans;

import java.util.List;
import java.util.Map;

/**
 * User: valentin
 * Date: 09.02.13
 */
public class HosterConfig {
    private long defaultSiteCheckInterval;
    private long hosterSleepInterval;
    private String sitesFileName;
    private List<Map> workers;

    public long getDefaultSiteCheckInterval() {
        return defaultSiteCheckInterval;
    }

    public void setDefaultSiteCheckInterval(long defaultSiteCheckInterval) {
        this.defaultSiteCheckInterval = defaultSiteCheckInterval;
    }

    public long getHosterSleepInterval() {
        if (hosterSleepInterval <= 1000)
            return 1000;
        else
            return hosterSleepInterval;
    }

    public void setHosterSleepInterval(long hosterSleepInterval) {
        this.hosterSleepInterval = hosterSleepInterval;
    }

    public String getSitesFileName() {
        return sitesFileName;
    }

    public void setSitesFileName(String sitesFileName) {
        this.sitesFileName = sitesFileName;
    }

    public List<Map> getWorkers() {
        return workers;
    }

    public void setWorkers(List<Map> workers) {
        this.workers = workers;
    }
}
