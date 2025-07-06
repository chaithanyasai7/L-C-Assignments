package com.newsaggregator.models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NewsArticle {
    private String id;
    private String title;
    private String description;
    private String content;
    private String url;
    private String source;
    private String category;
    private Date publishedAt;
    private int likes;
    private int dislikes;
    private Set<String> reportedBy;
    private boolean hidden;

    public NewsArticle() {
        this.reportedBy = new HashSet<>();
        this.hidden = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Date publishedAt) { this.publishedAt = publishedAt; }

    public int getLikes() { return likes; }
    public void incrementLikes() { this.likes++; }

    public int getDislikes() { return dislikes; }
    public void incrementDislikes() { this.dislikes++; }

    public Set<String> getReportedBy() { return reportedBy; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
}