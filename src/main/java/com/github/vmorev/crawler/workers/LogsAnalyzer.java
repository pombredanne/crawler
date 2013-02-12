package com.github.vmorev.crawler.workers;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.vmorev.amazon.log4j.support.LogCacheLine;
import com.github.vmorev.crawler.beans.LogFileSummary;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.AWSHelper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogsAnalyzer extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(LogsAnalyzer.class);
    protected AWSHelper helper;
    protected String siteSQSName;
    protected String siteS3Name;
    protected String articleSQSName;
    protected String articleS3Name;
    protected String logsS3Name;
    protected String logsStatS3Name;
    protected boolean isTest;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            helper = new AWSHelper();
            if (!isTest) {
                siteSQSName = helper.getConfig().getSQSSite();
                siteS3Name = helper.getConfig().getS3Site();
                articleSQSName = helper.getConfig().getSQSArticle();
                articleS3Name = helper.getConfig().getS3Article();
                logsS3Name = helper.getConfig().getS3Logs();
                logsStatS3Name = helper.getConfig().getS3LogsStat();
            }
        } catch (Exception e) {
            String message = "FAIL. " + LogsAnalyzer.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }

        List<LogFileSummary> stats = new ArrayList<>();
        try {
            ObjectListing objectListing = helper.getS3().getS3().listObjects(logsS3Name);
            do {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    List<LogCacheLine> logEntries = helper.getS3().getJSONObject(logsS3Name, objectSummary.getKey(), new TypeReference<List<LogCacheLine>>() {
                    });
                    LogFileSummary stat = analyzeLog(logEntries);
                    stats.add(stat);

                    String key = objectSummary.getKey().replace(".json", "-stat.json");
                    helper.getS3().saveJSONObject(logsStatS3Name, key, stat);
                    log.info("SUCCESS. " + LogsAnalyzer.class.getSimpleName() + ". LOG STAT ADDED TO S3 " + key);
                }
                objectListing.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

            LogFileSummary summary = calcStat(stats);
            String key = LogsAnalyzer.class.getSimpleName() + "-" + System.currentTimeMillis() + ".json";
            helper.getS3().saveJSONObject(logsStatS3Name, key, summary);

            log.info("SUCCESS. " + LogsAnalyzer.class.getSimpleName() + ". LOG STAT FINISHED");
        } catch (IOException e) {
            String message = "FAIL. " + LogsAnalyzer.class.getSimpleName() + ". LOG STAT FAILED. Can't put new stat to S3";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }
    }

    private LogFileSummary analyzeLog(List<LogCacheLine> logEntries) {
        LogFileSummary stat = new LogFileSummary();
        long minTime = 0;
        long maxTime = 0;
        for (LogCacheLine line : logEntries) {
            minTime = minTime == 0 || minTime > line.getTimeStamp() ? line.getTimeStamp() : minTime;
            maxTime = maxTime == 0 || maxTime < line.getTimeStamp() ? line.getTimeStamp() : maxTime;

            boolean isError = false;
            if (line.getLevel().equals("FATAL") || line.getLevel().equals("ERROR") || line.getLevel().equals("WARN")
                    || (line.getStackTrace() != null && line.getStackTrace().length > 0)
                    || (line.getMessage() != null && line.getMessage().startsWith("FAIL")))
                isError = true;

            stat.incLogMessagesCount();
            if (isError) {
                if (line.getMessage().contains("WORKER FAILED") || line.getMessage().contains("Initialization failure")) {
                    stat.incFailedInits();
                    stat.addFailedInitsMessage(line);
                }
                if (line.getMessage().contains("SITE FAILED")) {
                    stat.incFailedSites();
                    stat.addFailedSitesMessage(line);
                }
                if (line.getMessage().contains("ARTICLE FAILED")) {
                    stat.incFailedArticles();
                    stat.addFailedArticlesMessage(line);
                }
            } else {
                if (line.getMessage().contains("SITE ADDED TO S3") || line.getMessage().contains("SITE UPDATED IN S3"))
                    stat.incSucceedS3Sites();
                if (line.getMessage().contains("SITE ADDED TO SQS"))
                    stat.incSucceedSQSSites();
                if (line.getMessage().contains("ARTICLE ADDED TO S3"))
                    stat.incSucceedS3Articles();
                if (line.getMessage().contains("ARTICLE ADDED TO SQS"))
                    stat.incSucceedSQSArticles();
            }
        }
        stat.setGenerationTime(System.currentTimeMillis());
        stat.setStartTime(minTime);
        stat.setEndTime(maxTime);
        return stat;
    }

    private LogFileSummary calcStat(List<LogFileSummary> stats) throws IOException, ExecutionFailureException {
        LogFileSummary summary = new LogFileSummary();
        for (LogFileSummary stat : stats) {
            summary.addLogFileStat(stat);
        }
        summary.getNewSites().addAll(getNewSites());
/*
        getS3SitesCount();
        getSQSSitesCount();
        getOldestSiteCrawlDate();
        getS3ArticlesCount();
        getSQSArticlesCount();
        getDiffBotCallsCount();
*/
        return summary;
    }

    private List<String> getNewSites() throws ExecutionFailureException {
        List<String> sites = new ArrayList<>();
        try {
            ObjectListing objectListing = helper.getS3().getS3().listObjects(siteS3Name);
            do {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    List<Site> allSites = helper.getS3().getJSONObject(siteS3Name, objectSummary.getKey(), new TypeReference<List<Site>>() {
                    });

                    for (Site site : allSites)
                        if (site.getLastCheckDate() <= 0 || site.getExternalId() == null || site.getExternalId().length() == 0)
                            sites.add(site.getUrl());
                }
                objectListing.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
        } catch (IOException e) {
            String message = "FAIL. " + LogsAnalyzer.class.getSimpleName() + ". LOG STAT FAILED. Can't get list of sites in S3";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }
        return sites;
    }
}
