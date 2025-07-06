package model;

import java.util.Date;

public class NewsServer {
    private int id;
    private String name;
    private String apiKey;
    private Date lastAccessed;
    private ServerStatus status;

    public NewsServer(int id, String name, String apiKey, ServerStatus status) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
        this.status = status;
        this.lastAccessed = new Date();
    }

    public void updateLastAccessed() {
        this.lastAccessed = new Date();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getApiKey() { return apiKey; }
    public Date getLastAccessed() { return lastAccessed; }
    public ServerStatus getStatus() { return status; }

    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setStatus(ServerStatus status) { this.status = status; }
}
