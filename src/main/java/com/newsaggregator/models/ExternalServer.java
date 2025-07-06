package com.newsaggregator.models;

import java.util.Date;

public class ExternalServer {
    private String id;
    private String name;
    private String apiKey;
    private String baseUrl;
    private boolean active;
    private Date lastAccessed;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Date getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(Date lastAccessed) { this.lastAccessed = lastAccessed; }
}