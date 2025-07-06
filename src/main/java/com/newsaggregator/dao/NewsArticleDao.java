package com.newsaggregator.dao;

import com.newsaggregator.models.NewsArticle;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface NewsArticleDao {
    void save(NewsArticle article);
    void saveAll(List<NewsArticle> articles);
    Optional<NewsArticle> findById(String id);
    List<NewsArticle> findAll();
    List<NewsArticle> findByCategory(String category);
    List<NewsArticle> findByDateRange(Date start, Date end);
    List<NewsArticle> findByCategoryAndDateRange(String category, Date start, Date end);
    List<NewsArticle> search(String query);
    void update(NewsArticle article);
    void delete(String id);
    void incrementLikes(String articleId);
    void incrementDislikes(String articleId);
    void reportArticle(String articleId, String userId);
    int getReportCount(String articleId);
    void hideArticle(String articleId);
    List<NewsArticle> findVisibleArticles();
}
