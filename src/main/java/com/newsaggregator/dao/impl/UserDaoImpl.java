package com.newsaggregator.dao.impl;

import com.newsaggregator.dao.DatabaseConnection;
import com.newsaggregator.dao.UserDao;
import com.newsaggregator.models.User;
import com.newsaggregator.models.UserRole;

import java.sql.*;
import java.util.*;

public class UserDaoImpl implements UserDao {

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, username, email, password, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().name());

            stmt.executeUpdate();

            // Save notification settings
            saveNotificationSettings(user);

        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    private void saveNotificationSettings(User user) {
        String sql = "INSERT INTO notification_settings (user_id, category, enabled) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Map.Entry<String, Boolean> entry : user.getNotificationSettings().entrySet()) {
                stmt.setString(1, user.getId());
                stmt.setString(2, entry.getKey());
                stmt.setBoolean(3, entry.getValue());
                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving notification settings", e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                loadUserRelatedData(user);
                return Optional.of(user);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                loadUserRelatedData(user);
                return Optional.of(user);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ?, role = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getId());

            stmt.executeUpdate();

            // Update notification settings
            updateNotificationSettings(user);
            updateKeywords(user);

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    private void updateNotificationSettings(User user) {
        String deleteSql = "DELETE FROM notification_settings WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

            deleteStmt.setString(1, user.getId());
            deleteStmt.executeUpdate();

            saveNotificationSettings(user);

        } catch (SQLException e) {
            throw new RuntimeException("Error updating notification settings", e);
        }
    }

    private void updateKeywords(User user) {
        String deleteSql = "DELETE FROM keywords WHERE user_id = ?";
        String insertSql = "INSERT INTO keywords (user_id, keyword) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            deleteStmt.setString(1, user.getId());
            deleteStmt.executeUpdate();

            for (String keyword : user.getKeywords()) {
                insertStmt.setString(1, user.getId());
                insertStmt.setString(2, keyword);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating keywords", e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error checking username existence", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error checking email existence", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        return user;
    }

    private void loadUserRelatedData(User user) {
        loadNotificationSettings(user);
        loadKeywords(user);
        loadSavedArticles(user);
    }

    private void loadNotificationSettings(User user) {
        String sql = "SELECT * FROM notification_settings WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                user.getNotificationSettings().put(
                        rs.getString("category"),
                        rs.getBoolean("enabled")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading notification settings", e);
        }
    }

    private void loadKeywords(User user) {
        String sql = "SELECT keyword FROM keywords WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                user.getKeywords().add(rs.getString("keyword"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading keywords", e);
        }
    }

    private void loadSavedArticles(User user) {
        String sql = "SELECT article_id FROM saved_articles WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                user.getSavedArticleIds().add(rs.getString("article_id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading saved articles", e);
        }
    }
}