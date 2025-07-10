package repository;

import model.NewsArticle;
import model.User;
import model.UserRole;

import java.sql.*;
import java.util.*;

import static repository.DBConnection.getConnection;

public class Database {

    public void saveUser(User user) {
        String sql = "INSERT INTO Users (username, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getRole().name());
            preparedStatement.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        UserRole.valueOf(rs.getString("role"))
                );
                String keywordStr = rs.getString("keywords");
                if (keywordStr != null && !keywordStr.isEmpty()) {
                    user.setKeywords(List.of(keywordStr.split(",")));
                }
                String catPrefs = rs.getString("categories");
                if (catPrefs != null && !catPrefs.isEmpty()) {
                    user.setCategoryPreferences(User.parseCategoryPrefs(catPrefs));
                }
                return user;
            }
        } catch (SQLException ignored) {}
        return null;
    }

    public void saveArticle(NewsArticle article) {
        String sql = "INSERT INTO Articles (title, content, source, category, url, published_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, article.getTitle());
            preparedStatement.setString(2, article.getContent());
            preparedStatement.setString(3, article.getSource());
            preparedStatement.setString(4, article.getCategory());
            preparedStatement.setString(5, article.getUrl());
            preparedStatement.setDate(6, article.getDate());
            preparedStatement.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public void saveArticleForUser(User user, NewsArticle article) {
        String sql = "INSERT INTO SavedArticles (user_id, article_id, saved_at) VALUES (?, ?, CURDATE())";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setInt(2, article.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public List<NewsArticle> getSavedArticles(User user) {
        List<NewsArticle> articles = new ArrayList<>();
        String sql = """
                    SELECT a.* FROM Articles a
                    JOIN SavedArticles s ON a.article_id = s.article_id
                    WHERE s.user_id = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, user.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                articles.add(new NewsArticle(
                        resultSet.getInt("article_id"),
                        resultSet.getString("title"),
                        resultSet.getString("content"),
                        resultSet.getString("source"),
                        resultSet.getString("category"),
                        resultSet.getString("url"),
                        resultSet.getDate("published_at")
                ));
            }
        } catch (SQLException ignored) {
        }
        return articles;
    }

    public void deleteSavedArticle(User user, int articleId) {
        String sql = "DELETE FROM SavedArticles WHERE user_id = ? AND article_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setInt(2, articleId);
            preparedStatement.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public Collection<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        UserRole.valueOf(rs.getString("role"))
                );
                String keywordStr = rs.getString("keywords");
                if (keywordStr != null && !keywordStr.isEmpty()) {
                    List<String> keywords = List.of(keywordStr.split(","));
                    user.setKeywords(keywords);
                }
                String catPrefs = rs.getString("categories");
                if (catPrefs != null && !catPrefs.isEmpty()) {
                    user.setCategoryPreferences(User.parseCategoryPrefs(catPrefs));
                }

                users.add(user);
            }
        } catch (SQLException ignored) {
        }
        return users;
    }

    public Collection<NewsArticle> getAllArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String sql = "SELECT * FROM Articles";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                articles.add(new NewsArticle(
                        resultSet.getInt("article_id"),
                        resultSet.getString("title"),
                        resultSet.getString("content"),
                        resultSet.getString("source"),
                        resultSet.getString("category"),
                        resultSet.getString("url"),
                        resultSet.getDate("published_at")
                ));
            }
        } catch (SQLException ignored) {
        }
        return articles;
    }

    public void updateUserKeywords(int userId, List<String> keywords) {
        String keywordStr = String.join(",", keywords);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET keywords = ? WHERE user_id = ?")) {
            stmt.setString(1, keywordStr);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserCategories(int userId, Map<String, Boolean> categoryPrefs) {
        String serialized = User.serializeCategoryPrefs(categoryPrefs);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET categories = ? WHERE user_id = ?")) {
            stmt.setString(1, serialized);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}