package com.newsaggregator.services;

import com.newsaggregator.dao.DatabaseConnection;
import com.newsaggregator.dao.NewsArticleDao;
import com.newsaggregator.dao.impl.NewsArticleDaoImpl;
import com.newsaggregator.models.NewsArticle;
import com.newsaggregator.models.User;
import com.newsaggregator.utils.CategoryClassifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class NewsAggregationService {
    private final NewsArticleDao articleDao;
    private final NewsApiClient newsApiClient;
    private final EmailService emailService;
    private final ScheduledExecutorService scheduler;
    private final ReportThresholdManager reportManager;

    public NewsAggregationService() {
        this.articleDao = new NewsArticleDaoImpl();
        this.newsApiClient = new NewsApiClient();
        this.emailService = new EmailService();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.reportManager = new ReportThresholdManager();

        startPeriodicFetch();
    }

    private void startPeriodicFetch() {
        // Fetch news every 3 hours
        scheduler.scheduleAtFixedRate(this::fetchNewsFromAllSources, 0, 3, TimeUnit.HOURS);
    }

    public void fetchNewsFromAllSources() {
        System.out.println("Fetching news from all sources...");

        CompletableFuture<List<NewsArticle>> newsApiFuture =
                CompletableFuture.supplyAsync(() -> newsApiClient.fetchFromNewsApi());

        CompletableFuture<List<NewsArticle>> theNewsApiFuture =
                CompletableFuture.supplyAsync(() -> newsApiClient.fetchFromTheNewsApi());

        CompletableFuture.allOf(newsApiFuture, theNewsApiFuture).join();

        try {
            List<NewsArticle> allArticles = new ArrayList<>();
            allArticles.addAll(newsApiFuture.get());
            allArticles.addAll(theNewsApiFuture.get());

            // Classify articles without category
            for (NewsArticle article : allArticles) {
                if (article.getCategory() == null || article.getCategory().isEmpty()) {
                    String category = CategoryClassifier.classifyArticle(
                            article.getTitle(),
                            article.getDescription()
                    );
                    article.setCategory(category);
                }
            }

            articleDao.saveAll(allArticles);

            System.out.println("Fetched and saved " + allArticles.size() + " articles");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<NewsArticle> getHeadlines(String category, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();

        if ("All".equalsIgnoreCase(category)) {
            return articleDao.findByDateRange(startOfDay, endOfDay);
        } else {
            return articleDao.findByCategoryAndDateRange(category, startOfDay, endOfDay);
        }
    }

    public List<NewsArticle> getHeadlinesByDateRange(String category, Date start, Date end) {
        if ("All".equalsIgnoreCase(category)) {
            return articleDao.findByDateRange(start, end);
        } else {
            return articleDao.findByCategoryAndDateRange(category, start, end);
        }
    }

    public List<NewsArticle> searchArticles(String query) {
        return articleDao.search(query);
    }

    public void saveArticleForUser(String userId, String articleId) {
        String sql = "INSERT INTO saved_articles (user_id, article_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, articleId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            if (!e.getMessage().contains("Unique index or primary key violation")) {
                throw new RuntimeException("Error saving article for user", e);
            }
        }
    }

    public void unsaveArticleForUser(String userId, String articleId) {
        String sql = "DELETE FROM saved_articles WHERE user_id = ? AND article_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, articleId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error unsaving article for user", e);
        }
    }

    public List<NewsArticle> getSavedArticles(String userId) {
        String sql = """
            SELECT na.* FROM news_articles na 
            JOIN saved_articles sa ON na.id = sa.article_id 
            WHERE sa.user_id = ? 
            ORDER BY sa.saved_at DESC
        """;

        List<NewsArticle> articles = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                articles.add(mapResultSetToArticle(rs));
            }

            return articles;

        } catch (SQLException e) {
            throw new RuntimeException("Error getting saved articles", e);
        }
    }

    public void likeArticle(String articleId) {
        articleDao.incrementLikes(articleId);
    }

    public void dislikeArticle(String articleId) {
        articleDao.incrementDislikes(articleId);
    }

    public void reportArticle(String articleId, String userId) {
        articleDao.reportArticle(articleId, userId);

        // Check if threshold exceeded
        if (reportManager.shouldHideArticle(articleId)) {
            articleDao.hideArticle(articleId);
            notifyAdminAboutHiddenArticle(articleId);
        }
    }

    public List<NewsArticle> getPersonalizedArticles(User user) {
        List<NewsArticle> allArticles = articleDao.findVisibleArticles();

        // Filter based on user preferences
        return allArticles.stream()
                .filter(article -> isRelevantForUser(article, user))
                .sorted((a1, a2) -> calculateRelevanceScore(a2, user) - calculateRelevanceScore(a1, user))
                .collect(Collectors.toList());
    }

    private boolean isRelevantForUser(NewsArticle article, User user) {
        // Check notification settings
        Boolean categoryEnabled = user.getNotificationSettings().get(article.getCategory());
        if (categoryEnabled != null && !categoryEnabled) {
            return false;
        }

        // Check keywords
        if (user.getNotificationSettings().get("Keywords") && !user.getKeywords().isEmpty()) {
            String content = (article.getTitle() + " " + article.getDescription()).toLowerCase();
            return user.getKeywords().stream()
                    .anyMatch(keyword -> content.contains(keyword.toLowerCase()));
        }

        return true;
    }

    private int calculateRelevanceScore(NewsArticle article, User user) {
        int score = 0;

        // Boost if article is in liked articles
        if (user.getLikedArticleIds().contains(article.getId())) {
            score += 10;
        }

        // Boost if article is saved
        if (user.getSavedArticleIds().contains(article.getId())) {
            score += 5;
        }

        // Boost based on keyword matches
        String content = (article.getTitle() + " " + article.getDescription()).toLowerCase();
        for (String keyword : user.getKeywords()) {
            if (content.contains(keyword.toLowerCase())) {
                score += 3;
            }
        }

        return score;
    }

    private void notifyAdminAboutHiddenArticle(String articleId) {
        // Send notification to admin about auto-hidden article
        emailService.sendAdminNotification(
                "Article Auto-Hidden",
                "Article " + articleId + " has been automatically hidden due to excessive reports."
        );
    }

    private NewsArticle mapResultSetToArticle(ResultSet rs) throws SQLException {
        NewsArticle article = new NewsArticle();
        article.setId(rs.getString("id"));
        article.setTitle(rs.getString("title"));
        article.setDescription(rs.getString("description"));
        article.setContent(rs.getString("content"));
        article.setUrl(rs.getString("url"));
        article.setSource(rs.getString("source"));
        article.setCategory(rs.getString("category"));
        article.setPublishedAt(rs.getTimestamp("published_at"));
        article.setHidden(rs.getBoolean("hidden"));

        int likes = rs.getInt("likes");
        int dislikes = rs.getInt("dislikes");
        for (int i = 0; i < likes; i++) article.incrementLikes();
        for (int i = 0; i < dislikes; i++) article.incrementDislikes();

        return article;
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}