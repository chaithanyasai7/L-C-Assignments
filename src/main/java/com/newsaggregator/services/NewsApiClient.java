package com.newsaggregator.services;

import com.newsaggregator.models.NewsArticle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewsApiClient {
    private static final String NEWS_API_KEY = "0dd2aef2d01f47fe822ccdb0065cc2ec";
    private static final String THE_NEWS_API_KEY = "Xl5eByxfSQC51qNJmqF2CkUR1gOIWOpWQYy6YFDW";

    public List<NewsArticle> fetchFromNewsApi() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            String urlString = "https://newsapi.org/v2/top-headlines?country=us&apiKey=" + NEWS_API_KEY;
            String response = makeHttpRequest(urlString);

            JSONObject json = new JSONObject(response);
            JSONArray articlesArray = json.getJSONArray("articles");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            for (int i = 0; i < articlesArray.length(); i++) {
                JSONObject articleJson = articlesArray.getJSONObject(i);

                NewsArticle article = new NewsArticle();
                article.setId("newsapi-" + UUID.randomUUID().toString());
                article.setTitle(articleJson.optString("title", ""));
                article.setDescription(articleJson.optString("description", ""));
                article.setContent(articleJson.optString("content", ""));
                article.setUrl(articleJson.optString("url", ""));

                JSONObject source = articleJson.optJSONObject("source");
                if (source != null) {
                    article.setSource(source.optString("name", ""));
                }

                String publishedAt = articleJson.optString("publishedAt");
                if (!publishedAt.isEmpty()) {
                    article.setPublishedAt(dateFormat.parse(publishedAt));
                } else {
                    article.setPublishedAt(new Date());
                }

                articles.add(article);
            }

        } catch (Exception e) {
            System.err.println("Error fetching from News API: " + e.getMessage());
        }

        return articles;
    }

    public List<NewsArticle> fetchFromTheNewsApi() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            String urlString = "https://api.thenewsapi.com/v1/news/top?api_token=" +
                    THE_NEWS_API_KEY + "&locale=us&limit=50";
            String response = makeHttpRequest(urlString);

            JSONObject json = new JSONObject(response);
            JSONArray dataArray = json.getJSONArray("data");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject articleJson = dataArray.getJSONObject(i);

                NewsArticle article = new NewsArticle();
                article.setId("thenewsapi-" + articleJson.optString("uuid", UUID.randomUUID().toString()));
                article.setTitle(articleJson.optString("title", ""));
                article.setDescription(articleJson.optString("description", ""));
                article.setContent(articleJson.optString("snippet", ""));
                article.setUrl(articleJson.optString("url", ""));
                article.setSource(articleJson.optString("source", ""));

                JSONArray categories = articleJson.optJSONArray("categories");
                if (categories != null && categories.length() > 0) {
                    article.setCategory(categories.getString(0));
                }

                String publishedAt = articleJson.optString("published_at");
                if (!publishedAt.isEmpty()) {
                    article.setPublishedAt(dateFormat.parse(publishedAt));
                } else {
                    article.setPublishedAt(new Date());
                }

                articles.add(article);
            }

        } catch (Exception e) {
            System.err.println("Error fetching from The News API: " + e.getMessage());
        }

        return articles;
    }

    private String makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "NewsAggregator/1.0");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new Exception("HTTP request failed with response code: " + responseCode);
        }
    }
}