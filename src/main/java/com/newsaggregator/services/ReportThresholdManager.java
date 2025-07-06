package com.newsaggregator.services;

import com.newsaggregator.dao.NewsArticleDao;
import com.newsaggregator.dao.impl.NewsArticleDaoImpl;

public class ReportThresholdManager {
    private static final int REPORT_THRESHOLD = 5;
    private final NewsArticleDao articleDao;

    public ReportThresholdManager() {
        this.articleDao = new NewsArticleDaoImpl();
    }

    public boolean shouldHideArticle(String articleId) {
        return articleDao.getReportCount(articleId) >= REPORT_THRESHOLD;
    }
}