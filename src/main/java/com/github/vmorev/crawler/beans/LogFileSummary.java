package com.github.vmorev.crawler.beans;

import com.github.vmorev.amazon.log4j.support.LogCacheLine;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Valentin_Morev
 * Date: 11.02.13
 */
@org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
public class LogFileSummary {
    public static final String VAR_S3_BUCKET = "logStat.s3";
    public static final String VAR_LOG_S3_BUCKET = "log.s3";

    private long generationTime;
    private long startTime;
    private long endTime;
    private long logMessagesCount;
    private long succeedS3Articles;
    private long succeedSQSArticles;
    private long succeedS3Sites;
    private long succeedSQSSites;
    private long failedInits;
    private long failedSites;
    private long failedArticles;
    private List<LogCacheLine> failedInitsMessages = new ArrayList<>();
    private List<LogCacheLine> failedSitesMessages = new ArrayList<>();
    private List<LogCacheLine> failedArticlesMessages = new ArrayList<>();

    private List<String> newSites = new ArrayList<>();

    public void addLogFileStat(LogFileSummary stat) {
        generationTime = generationTime < stat.getGenerationTime() ? stat.getGenerationTime() : generationTime;
        startTime = startTime == 0 || startTime > stat.getStartTime() ? stat.getStartTime() : startTime;
        endTime = endTime == 0 || endTime < stat.getEndTime() ? stat.getEndTime() : endTime;

        logMessagesCount += stat.getLogMessagesCount();
        succeedS3Articles += stat.getSucceedS3Articles();
        succeedSQSArticles += stat.getSucceedSQSArticles();
        succeedS3Sites += stat.getSucceedS3Sites();
        succeedSQSSites += stat.getSucceedSQSSites();
        failedInits += stat.getFailedInits();
        failedSites += stat.getFailedSites();
        failedArticles += stat.getFailedArticles();

        failedInitsMessages.addAll(stat.getFailedInitsMessages());
        failedSitesMessages.addAll(stat.getFailedSitesMessages());
        failedArticlesMessages.addAll(stat.getFailedArticlesMessages());
    }

    public void addNewSite(String siteId) {
        newSites.add(siteId);
    }

    public void addFailedInitsMessage(LogCacheLine line) {
        failedInitsMessages.add(line);
    }

    public void addFailedSitesMessage(LogCacheLine line) {
        failedSitesMessages.add(line);
    }

    public void addFailedArticlesMessage(LogCacheLine line) {
        failedArticlesMessages.add(line);
    }

    public List<LogCacheLine> getFailedInitsMessages() {
        return failedInitsMessages;
    }

    public void setFailedInitsMessages(List<LogCacheLine> failedInitsMessages) {
        this.failedInitsMessages = failedInitsMessages;
    }

    public List<LogCacheLine> getFailedSitesMessages() {
        return failedSitesMessages;
    }

    public void setFailedSitesMessages(List<LogCacheLine> failedSitesMessages) {
        this.failedSitesMessages = failedSitesMessages;
    }

    public List<LogCacheLine> getFailedArticlesMessages() {
        return failedArticlesMessages;
    }

    public void setFailedArticlesMessages(List<LogCacheLine> failedArticlesMessages) {
        this.failedArticlesMessages = failedArticlesMessages;
    }

    public void incFailedSites() {
        failedSites++;
    }

    public void incFailedArticles() {
        failedArticles++;
    }

    public void incFailedInits() {
        failedInits++;
    }

    public void incSucceedS3Articles() {
        succeedS3Articles++;
    }

    public void incSucceedSQSArticles() {
        succeedSQSArticles++;
    }

    public void incSucceedS3Sites() {
        succeedS3Sites++;
    }

    public void incSucceedSQSSites() {
        succeedSQSSites++;
    }

    public void incLogMessagesCount() {
        logMessagesCount++;
    }

    public long getGenerationTime() {
        return generationTime;
    }

    public void setGenerationTime(long generationTime) {
        this.generationTime = generationTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getLogMessagesCount() {
        return logMessagesCount;
    }

    public void setLogMessagesCount(long logMessagesCount) {
        this.logMessagesCount = logMessagesCount;
    }

    public long getSucceedS3Articles() {
        return succeedS3Articles;
    }

    public void setSucceedS3Articles(long succeedS3Articles) {
        this.succeedS3Articles = succeedS3Articles;
    }

    public long getSucceedSQSArticles() {
        return succeedSQSArticles;
    }

    public void setSucceedSQSArticles(long succeedSQSArticles) {
        this.succeedSQSArticles = succeedSQSArticles;
    }

    public long getSucceedS3Sites() {
        return succeedS3Sites;
    }

    public void setSucceedS3Sites(long succeedS3Sites) {
        this.succeedS3Sites = succeedS3Sites;
    }

    public long getSucceedSQSSites() {
        return succeedSQSSites;
    }

    public void setSucceedSQSSites(long succeedSQSSites) {
        this.succeedSQSSites = succeedSQSSites;
    }

    public long getFailedInits() {
        return failedInits;
    }

    public void setFailedInits(long failedInits) {
        this.failedInits = failedInits;
    }

    public long getFailedSites() {
        return failedSites;
    }

    public void setFailedSites(long failedSites) {
        this.failedSites = failedSites;
    }

    public long getFailedArticles() {
        return failedArticles;
    }

    public void setFailedArticles(long failedArticles) {
        this.failedArticles = failedArticles;
    }

    public List<String> getNewSites() {
        return newSites;
    }

    public void setNewSites(List<String> newSites) {
        this.newSites = newSites;
    }
}
