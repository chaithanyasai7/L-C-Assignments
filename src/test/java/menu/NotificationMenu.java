package menu;

import model.NewsArticle;
import model.User;
import service.ArticleService;
import service.NotificationService;
import util.EmailSender;

import java.util.*;

import static newsAggregationApplication.NewsAggregationApplication.*;

public class NotificationMenu {

    public static void show(User user, NotificationService notificationService, ArticleService articleService, Scanner scanner) {
        while (true) {
            System.out.println("================ NOTIFICATIONS MENU ================");
            System.out.println("1. View Notifications");
            System.out.println("2. Configure Notifications");
            System.out.println("3. Back");
            System.out.println("4. Logout");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> notificationService.sendNotification(user, articleService.getAllArticles());
                case 2 -> {
                    System.out.print("Enter comma-separated keywords: ");
                    String input = scanner.nextLine();
                    List<String> keywords = new ArrayList<>();
                    for (String keyword : input.split(",")) {
                        String trimmed = keyword.trim();
                        if (!trimmed.isEmpty()) {
                            keywords.add(trimmed);
                        }
                    }

                    notificationService.configure(user, keywords);

                    List<NewsArticle> matchingArticles = new ArrayList<>();
                    for (String keyword : keywords) {
                        List<NewsArticle> searchResults = articleService.search(keyword);
                        for (NewsArticle article : searchResults) {
                            if (!matchingArticles.contains(article)) {
                                matchingArticles.add(article);
                            }
                        }
                    }

                    try {
                        if (!matchingArticles.isEmpty()) {
                            StringBuilder emailBody = new StringBuilder();
                            emailBody.append("Here are articles matching your keywords: ")
                                    .append(String.join(", ", keywords))
                                    .append("\n\n");

                            for (NewsArticle article : matchingArticles) {
                                emailBody.append("Title: ").append(article.getTitle()).append("\n");
                                emailBody.append("Content: ").append(article.getContent()).append("\n");
                                emailBody.append("Category: ").append(article.getCategory()).append("\n");
                                emailBody.append("---\n\n");
                            }

                            EmailSender.sendEmail(user.getEmail(), "News Articles - Keyword Notifications", emailBody.toString());
                            System.out.println("Notification sent to user with " + matchingArticles.size() + " matching articles.");
                        } else {
                            System.out.println("No articles found matching your keywords.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error sending notification: " + e.getMessage());
                    }
                }
                case 3 -> { return; }
                case 4 -> System.exit(0);
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
