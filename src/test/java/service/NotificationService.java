package service;

import model.NewsArticle;
import model.User;
import util.EmailSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static newsAggregationApplication.NewsAggregationApplication.database;

public class NotificationService {

    private final Map<String, List<String>> userKeywords = new HashMap<>();

    public void configure(User user, List<String> keywords) {
        userKeywords.put(user.getEmail(), keywords);
        System.out.println("Notification preferences saved for: " + user.getUsername());
    }

    public void sendNotification(User user, List<NewsArticle> articles) {
        List<String> keywords = userKeywords.get(user.getEmail());
        if (keywords == null || keywords.isEmpty()) {
            System.out.println("No keywords configured for notifications.");
            return;
        }

        StringBuilder matchedContent = new StringBuilder();
        for (NewsArticle article : articles) {
            for (String keyword : keywords) {
                if (article.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || article.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                    matchedContent.append("- ").append(article.getTitle())
                            .append(" [").append(article.getCategory()).append("]\n")
                            .append(article.getUrl()).append("\n\n");
                    break;
                }
            }
        }

        if (matchedContent.length() == 0) {
            System.out.println("No articles matched your preferences, " + user.getUsername());
        } else {
            String subject = "News Notification - Articles Matching Your Preferences";
            EmailSender.sendEmail(user.getEmail(), subject, matchedContent.toString());
            System.out.println("Notification email sent to: " + user.getEmail());
        }
    }

    public List<NewsArticle> getMatchingArticles(User user, List<NewsArticle> articles) {
        List<String> keywords = user.getKeywords();
        List<NewsArticle> matchedArticles = new ArrayList<>();

        for (NewsArticle article : articles) {
            String title = article.getTitle().toLowerCase();
            String content = article.getContent().toLowerCase();
            for (String keyword : keywords) {
                if (title.contains(keyword.toLowerCase()) || content.contains(keyword.toLowerCase())) {
                    matchedArticles.add(article);
                    break;
                }
            }
        }

        return matchedArticles;
    }
}
