package com.newsaggregator.models;

import java.util.*;

public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private Date createdAt;
    private Map<String, Boolean> notificationSettings;
    private Set<String> keywords;
    private Set<String> savedArticleIds;
    private Set<String> likedArticleIds;
    private Set<String> readArticleIds;

    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.notificationSettings = new HashMap<>();
        this.keywords = new HashSet<>();
        this.savedArticleIds = new HashSet<>();
        this.likedArticleIds = new HashSet<>();
        this.readArticleIds = new HashSet<>();
        initializeNotificationSettings();
    }

    private void initializeNotificationSettings() {
        notificationSettings.put("Business", true);
        notificationSettings.put("Entertainment", true);
        notificationSettings.put("Sports", false);
        notificationSettings.put("Technology", false);
        notificationSettings.put("Keywords", true);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Date getCreatedAt() { return createdAt; }

    public Map<String, Boolean> getNotificationSettings() { return notificationSettings; }

    public Set<String> getKeywords() { return keywords; }

    public Set<String> getSavedArticleIds() { return savedArticleIds; }

    public Set<String> getLikedArticleIds() { return likedArticleIds; }

    public Set<String> getReadArticleIds() { return readArticleIds; }
}