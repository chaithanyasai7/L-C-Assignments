package src.test.java.model;

import java.util.Date;

public class NewsServer {
    private int id;
    private String name;
    private String apiKey;
    private Date lastAccessed;


    public NewsServer(int id, String name, String apiKey, ServerStatus active) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
        this.lastAccessed = new Date();
    }
}
