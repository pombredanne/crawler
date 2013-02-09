package com.github.vmorev.crawler.beans;

import java.util.List;

/**
 * User: valentin
 * Date: 09.02.13
 */
public class HosterConfig {
    private long defaultSiteCheckInterval;
    private long hosterSleepInterval;
    private String sitesFileName;
    private List<Worker> workers;

    public class Worker {
        private String className;
        private long threads;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public long getThreads() {
            return threads;
        }

        public void setThreads(long threads) {
            this.threads = threads;
        }
    }

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

    public List<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }
}
