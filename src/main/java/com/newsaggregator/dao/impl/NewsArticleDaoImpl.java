package com.newsaggregator.dao.impl;

import com.newsaggregator.dao.DatabaseConnection;
import com.newsaggregator.dao.NewsArticleDao;
import com.newsaggregator.models.NewsArticle;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class NewsArticleDaoImpl implements NewsArticleDao {

    @Override
    public void save(NewsArticle article) {
        String sql = """
            INSERT INTO news_articles (id, title, description, content, url, source, 
                                     category, published_at, likes, dislikes, hidden) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, article.getId());
            stmt.setString(2, article.getTitle());
            stmt.setString(3, article.getDescription());
            stmt.setString(4, article.getContent());
            stmt.setString(5, article.getUrl());
            stmt.setString(6, article.getSource());
            stmt.setString(7, article.getCategory());
            stmt.setTimestamp(8, new Timestamp(article.getPublishedAt().getTime()));
            stmt.setInt(9, article.getLikes());
            stmt.setInt(10, article.getDislikes());
            stmt.setBoolean(11, article.isHidden());

            stmt.executeUpdate();

        } catch (SQLException e) {
            // Ignore duplicate key errors
            if (!e.getMessage().contains("Unique index or primary key violation")) {
                throw new RuntimeException("Error saving article", e);
            }
        }
    }

    @Override
    public void saveAll(List<NewsArticle> articles) {
        for (NewsArticle article : articles) {
            save(article);
        }
    }

    @Override
    public Optional<NewsArticle> findById(String id) {
        String sql = "SELECT * FROM news_articles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToArticle(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding article by id", e);
        }
    }

    @Override
    public List<NewsArticle> findAll() {
        return findArticles("SELECT * FROM news_articles ORDER BY published_at DESC");
    }

    @Override
    public List<NewsArticle> findByCategory(String category) {
        return findArticles(
                "SELECT * FROM news_articles WHERE category = ? AND hidden = false ORDER BY published_at DESC",
                category
        );
    }

    @Override
    public List<NewsArticle> findByDateRange(Date start, Date end) {
        return findArticles(
                "SELECT * FROM news_articles WHERE published_at BETWEEN ? AND ? AND hidden = false ORDER BY published_at DESC",
                new Timestamp(start.getTime()),
                new Timestamp(end.getTime())
        );
    }

    @Override
    public List<NewsArticle> findByCategoryAndDateRange(String category, Date start, Date end) {
        return findArticles(
                "SELECT * FROM news_articles WHERE category = ? AND published_at BETWEEN ? AND ? AND hidden = false ORDER BY published_at DESC",
                category,
                new Timestamp(start.getTime()),
                new Timestamp(end.getTime())
        );
    }

    @Override
    public List<NewsArticle> search(String query) {
        String searchQuery = "%" + query.toLowerCase() + "%";
        return findArticles(
                "SELECT * FROM news_articles WHERE (LOWER(title) LIKE ? OR LOWER(description) LIKE ?) AND hidden = false ORDER BY published_at DESC",
                searchQuery,
                searchQuery
        );
    }

    @Override
    public void update(NewsArticle article) {
        String sql = """
            UPDATE news_articles SET title = ?, description = ?, content = ?, 
                                   url = ?, source = ?, category = ?, 
                                   published_at = ?, likes = ?, dislikes = ?, hidden = ? 
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, article.getTitle());
            stmt.setString(2, article.getDescription());
            stmt.setString(3, article.getContent());
            stmt.setString(4, article.getUrl());
            stmt.setString(5, article.getSource());
            stmt.setString(6, article.getCategory());
            stmt.setTimestamp(7, new Timestamp(article.getPublishedAt().getTime()));
            stmt.setInt(8, article.getLikes());
            stmt.setInt(9, article.getDislikes());
            stmt.setBoolean(10, article.isHidden());
            stmt.setString(11, article.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating article", e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM news_articles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting article", e);
        }
    }

    @Override
    public void incrementLikes(String articleId) {
        updateCounter("UPDATE news_articles SET likes = likes + 1 WHERE id = ?", articleId);
    }

    @Override
    public void incrementDislikes(String articleId) {
        updateCounter("UPDATE news_articles SET dislikes = dislikes + 1 WHERE id = ?", articleId);
    }

    @Override
    public void reportArticle(String articleId, String userId) {
        String sql = "INSERT INTO article_reports (article_id, user_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, articleId);
            stmt.setString(2, userId);
            stmt.executeUpdate();

            // Check if report threshold is exceeded
            int reportCount = getReportCount(articleId);
            if (reportCount >= 5) { // Threshold of 5 reports
                hideArticle(articleId);
            }

        } catch (SQLException e) {
            if (!e.getMessage().contains("Unique index or primary key violation")) {
                throw new RuntimeException("Error reporting article", e);
            }
        }
    }

    @Override
    public int getReportCount(String articleId) {
        String sql = "SELECT COUNT(*) FROM article_reports WHERE article_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, articleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error getting report count", e);
        }
    }

    @Override
    public void hideArticle(String articleId) {
        updateCounter("UPDATE news_articles SET hidden = true WHERE id = ?", articleId);
    }

    @Override
    public List<NewsArticle> findVisibleArticles() {
        return findArticles("SELECT * FROM news_articles WHERE hidden = false ORDER BY published_at DESC");
    }

    private void updateCounter(String sql, String articleId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, articleId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating counter", e);
        }
    }

    private List<NewsArticle> findArticles(String sql, Object... params) {
        List<NewsArticle> articles = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                articles.add(mapResultSetToArticle(rs));
            }

            return articles;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding articles", e);
        }
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

        // Set likes and dislikes directly
        int likes = rs.getInt("likes");
        int dislikes = rs.getInt("dislikes");
        for (int i = 0; i < likes; i++) article.incrementLikes();
        for (int i = 0; i < dislikes; i++) article.incrementDislikes();

        return article;
    }
}