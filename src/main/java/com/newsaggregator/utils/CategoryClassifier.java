package com.newsaggregator.utils;

import java.util.*;

public class CategoryClassifier {
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        CATEGORY_KEYWORDS.put("Business", Arrays.asList(
                "business", "finance", "economy", "market", "stock", "trade", "investment",
                "company", "corporate", "revenue", "profit", "earnings", "merger", "acquisition"
        ));

        CATEGORY_KEYWORDS.put("Technology", Arrays.asList(
                "technology", "tech", "software", "hardware", "computer", "internet", "ai",
                "artificial intelligence", "machine learning", "data", "cyber", "digital",
                "innovation", "startup", "app"
        ));

        CATEGORY_KEYWORDS.put("Sports", Arrays.asList(
                "sports", "game", "match", "player", "team", "championship", "tournament",
                "football", "basketball", "baseball", "soccer", "tennis", "golf", "olympic"
        ));

        CATEGORY_KEYWORDS.put("Entertainment", Arrays.asList(
                "entertainment", "movie", "film", "music", "celebrity", "actor", "actress",
                "singer", "concert", "show", "television", "tv", "series", "netflix"
        ));
    }

    public static String classifyArticle(String title, String description) {
        if (title == null && description == null) return "General";

        String content = ((title != null ? title : "") + " " +
                (description != null ? description : "")).toLowerCase();

        Map<String, Integer> scores = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (content.contains(keyword)) {
                    score++;
                }
            }
            scores.put(entry.getKey(), score);
        }

        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse("General");
    }
}