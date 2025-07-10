package menu;

import model.NewsArticle;
import model.User;
import repository.Database;

import java.util.List;
import java.util.Scanner;

public class SavedArticlesMenu {
    public static void show(User user, Database database, Scanner scanner) {
        while (true) {
            List<NewsArticle> savedArticles = database.getSavedArticles(user);
            if (savedArticles.isEmpty()) {
                System.out.println("No saved articles.");
            } else {
                for (NewsArticle article : savedArticles) {
                    System.out.println("- ID: " + article.getId() + " | " + article.getTitle());
                }
            }

            System.out.println("\n\033[1m============= SAVED ARTICLES =============\033[0m");
            System.out.println("1. Back");
            System.out.println("2. Logout");
            System.out.println("3. Delete Article");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> { return; }
                case 2 -> { System.exit(0); }
                case 3 -> {
                    System.out.print("Enter Article ID to delete: ");
                    int articleId = Integer.parseInt(scanner.nextLine());
                    database.deleteSavedArticle(user, articleId);
                    System.out.println("Article deleted from saved list.");
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
