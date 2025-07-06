package model;

import java.util.*;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private List<String> keywords = new ArrayList<>();
    private List<NewsArticle> savedArticles = new ArrayList<>();
    private Map<String, Boolean> categoryPreferences = new HashMap<>();

    public User(int id, String username, String email, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public List<NewsArticle> getSavedArticles() { return savedArticles; }
    public void setSavedArticles(List<NewsArticle> savedArticles) { this.savedArticles = savedArticles; }

    public Map<String, Boolean> getCategoryPreferences() { return categoryPreferences; }
    public void setCategoryPreferences(Map<String, Boolean> preferences) { this.categoryPreferences = preferences; }

    public void toggleCategoryPreference(String category) {
        categoryPreferences.put(category, !categoryPreferences.getOrDefault(category, false));
    }

    public static Map<String, Boolean> parseCategoryPrefs(String stored) {
        Map<String, Boolean> prefs = new HashMap<>();
        if (stored == null || stored.isEmpty()) return prefs;
        String[] parts = stored.split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                prefs.put(kv[0].trim(), Boolean.parseBoolean(kv[1].trim()));
            }
        }
        return prefs;
    }

    public static String serializeCategoryPrefs(Map<String, Boolean> prefs) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : prefs.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }
}
