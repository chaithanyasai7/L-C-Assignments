package menu;

import model.NewsArticle;
import model.User;
import repository.Database;
import service.ArticleService;

import java.util.List;
import java.util.Scanner;

public class SearchMenu {
    public static void show(User user, ArticleService articleService, Database database, Scanner scanner) {
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().toLowerCase();

        List<NewsArticle> results = articleService.search(keyword);

        if (results.isEmpty()) {
            System.out.println("No articles found.");
        } else {
            System.out.println("Results for \"" + keyword + "\":");
            for (NewsArticle article : results) {
                System.out.println("- ID: " + article.getId() + " | " + article.getTitle());
            }
        }

        System.out.println("1. Back");
        System.out.println("2. Logout");
        System.out.println("3. Save Article");

        int choice = Integer.parseInt(scanner.nextLine());
        switch (choice) {
            case 1 -> { return; }
            case 2 -> System.exit(0);
            case 3 -> {
                System.out.print("Enter Article ID to save: ");
                int id = Integer.parseInt(scanner.nextLine());
                NewsArticle match = results.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
                if (match != null) {
                    database.saveArticleForUser(user, match);
                    System.out.println("Article saved.");
                } else {
                    System.out.println("Invalid Article ID.");
                }
            }
        }
    }
}
