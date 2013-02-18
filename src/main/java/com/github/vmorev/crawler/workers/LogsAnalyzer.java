package com.github.vmorev.crawler.workers;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.vmorev.amazon.log4j.support.LogCacheLine;
import com.github.vmorev.crawler.beans.Article;
import com.github.vmorev.crawler.beans.LogFileSummary;
import com.github.vmorev.crawler.beans.Site;
import com.github.vmorev.crawler.utils.amazon.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogsAnalyzer extends AbstractWorker {
    private static final Logger log = LoggerFactory.getLogger(LogsAnalyzer.class);
    protected S3Service s3;
    private S3Service.S3Bucket<Article> articleBucket;
    private S3Service.S3Bucket<Site> siteBucket;
    private S3Service.S3Bucket<List<LogCacheLine>> logsBucket;
    private S3Service.S3Bucket<LogFileSummary> logsStatBucket;

    protected void performWork() throws InterruptedException, ExecutionFailureException {
        try {
            s3 = new S3Service();
            if (articleBucket == null)
                articleBucket = s3.getBucket(s3.getConfig().getArticle(), Article.class);
            if (siteBucket == null)
                siteBucket = s3.getBucket(s3.getConfig().getSite(), Site.class);
            if (logsBucket == null)
                logsBucket = s3.getBucket(s3.getConfig().getLogs(), List.class);
            if (logsStatBucket == null)
                logsStatBucket = s3.getBucket(s3.getConfig().getLogsStat(), LogFileSummary.class);
        } catch (Exception e) {
            String message = "FAIL. " + LogsAnalyzer.class.getSimpleName() + ". Initialization failure";
            log.error(message, e);
            throw new ExecutionFailureException(message, e);
        }

        try {
            final List<LogFileSummary> stats = new ArrayList<>();
            logsBucket.listObjectSummaries(new S3Service.ListFunc<S3ObjectSummary>() {
                public void process(S3ObjectSummary summary) {
                    List<LogCacheLine> logEntries = logsBucket.getObject(summary.getKey());
                    LogFileSummary stat = analyzeLog(logEntries);
                    stats.add(stat);
                    String key = summary.getKey().replace(".json", "-stat.json");
                    try {
                        logsStatBucket.saveObject(key, stat);
                        log.info("SUCCESS. " + LogsAnalyzer.class.getSimpleName() + ". LOG STAT ADDED TO S3 " + key);
                    } catch (IOException e) {
                        log.info("FAIL. " + LogsAnalyzer.class.getSimpleName() + ". LOG STAT FAILED. Can't put new stat to S3");
                    }
                }
            });

            LogFileSummary summary = calcStat(stats);
            String key = LogsAnalyzer.class.getSimpleName() + "-" + System.currentTimeMillis() + ".json";
            logsStatBucket.saveObject(key, summary);
            log.info("SUCCESS. " + LogsAnalyzer.class.getSimpleName() + ". LOG STAT FINISHED");
        } catch (Exception e) {
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

    private LogFileSummary calcStat(List<LogFileSummary> stats) throws Exception {
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

    private List<String> getNewSites() throws Exception {
        final List<String> sites = new ArrayList<>();
        siteBucket.listObjects(new S3Service.ListFunc<Site>() {
            public void process(Site site) {
                if (site.getLastCheckDate() <= 0 || site.getExternalId() == null || site.getExternalId().length() == 0)
                    sites.add(site.getUrl());
            }
        });
        return sites;
    }
}
