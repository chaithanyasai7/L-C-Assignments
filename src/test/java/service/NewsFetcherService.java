package service;

import com.google.gson.*;
import model.NewsArticle;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NewsFetcherService {
    private final Gson gson = new Gson();
    private static int idCounter = 1;
    private final Set<String> urlSeen = new HashSet<>();

    public List<NewsArticle> fetchTopHeadlines() {
        return new ArrayList<>();
    }

    public List<NewsArticle> fetchFromNewsAPI(String apiKey) {
        List<NewsArticle> articles = new ArrayList<>();
        String[] categories = { "business", "entertainment", "sports", "technology", "health", "science" };

        for (String category : categories) {
            String url = "https://newsapi.org/v2/top-headlines?country=us&pageSize=100&category=" + category + "&apiKey=" + apiKey;

            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                JsonObject response = gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
                JsonArray items = response.getAsJsonArray("articles");

                for (JsonElement item : items) {
                    JsonObject obj = item.getAsJsonObject();
                    String publishedAt = getSafeString(obj, "publishedAt");

                    LocalDateTime dateTime = LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
                    Date date = Date.valueOf(dateTime.toLocalDate());

                    String articleUrl = getSafeString(obj, "url");
                    if (urlSeen.contains(articleUrl)) continue;

                    NewsArticle article = new NewsArticle(
                            obj.hashCode(),
                            getSafeString(obj, "title"),
                            getSafeString(obj, "description"),
                            obj.get("source").getAsJsonObject().get("name").getAsString(),
                            category,
                            articleUrl,
                            date
                    );

                    articles.add(article);
                    urlSeen.add(articleUrl);

                    System.out.println("NewsAPI Article: " + article.getTitle() + " | " + category + " | " + date);
                }

            } catch (Exception e) {
                System.err.println("Error fetching from NewsAPI (category: " + category + "): " + e.getMessage());
            }
        }

        return articles;
    }

    public List<NewsArticle> fetchFromTheNewsAPI(String apiKey) {
        List<NewsArticle> articles = new ArrayList<>();
        String url = "https://api.thenewsapi.com/v1/news/top?api_token=" + apiKey + "&locale=us&limit=50";

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            JsonObject response = gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
            JsonArray items = response.getAsJsonArray("data");

            for (JsonElement item : items) {
                JsonObject obj = item.getAsJsonObject();
                String articleUrl = getSafeString(obj, "url");
                if (urlSeen.contains(articleUrl)) continue;

                String publishedAt = getSafeString(obj, "published_at");
                LocalDateTime dateTime = LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
                Date date = Date.valueOf(dateTime.toLocalDate());

                NewsArticle article = new NewsArticle(
                        idCounter++,
                        getSafeString(obj, "title"),
                        getSafeString(obj, "description"),
                        getSafeString(obj, "source"),
                        getSafeString(obj, "category"),
                        articleUrl,
                        date
                );

                articles.add(article);
                urlSeen.add(articleUrl);

                System.out.println("TheNewsAPI Article: " + article.getTitle() + " | " + article.getCategory() + " | " + date);
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch from TheNewsAPI: " + e.getMessage());
        }

        return articles;
    }

    private String getSafeString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "N/A";
    }
}
