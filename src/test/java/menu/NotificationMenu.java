package menu;

import model.User;
import service.ArticleService;
import service.NotificationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static newsAggregationApplication.NewsAggregationApplication.*;

public class NotificationMenu {
    private static final List<String> CATEGORIES = List.of("business", "entertainment", "sports", "technology");

    public static void show(User user, NotificationService notificationService, ArticleService articleService, Scanner scanner) {
        while (true) {
            System.out.println("\n\033[1m================ NOTIFICATIONS MENU ================\033[0m");
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
                    notificationService.sendNotification(user, articleService.getAllArticles());
                }
                case 3 -> { return; }
                case 4 -> System.exit(0);
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void configureNotifications(User user, Scanner scanner, NotificationService notificationService) {
        while (true) {
            System.out.println("=============== CONFIGURE-NOTIFICATIONS ================");
            System.out.printf("Welcome to the News Application, %s! Date: %s Time: %s\n\n",
                    user.getUsername(), LocalDate.now(), LocalTime.now().withNano(0));

            Map<String, Boolean> prefs = user.getCategoryPreferences();
            for (int i = 0; i < CATEGORIES.size(); i++) {
                String cat = CATEGORIES.get(i);
                boolean status = prefs.getOrDefault(cat, false);
                System.out.printf("%d. %s - %s\n", i + 1, capitalize(cat), status ? "Enabled" : "Disabled");
            }

            System.out.println("5. Keywords - " + (user.getKeywords().isEmpty() ? "Disabled" : "Enabled"));
            System.out.println("6. Back");
            System.out.println("7. Logout");

            int choice = getIntInput("\nEnter your option: ");

            switch (choice) {
                case 1, 2, 3, 4 -> {
                    String selectedCategory = CATEGORIES.get(choice - 1);
                    user.toggleCategoryPreference(selectedCategory);
                    database.updateUserCategories(user.getId(), user.getCategoryPreferences());
                    System.out.println(capitalize(selectedCategory) + " notifications toggled.");
                }
                case 5 -> {
                    System.out.print("Enter comma-separated keywords: ");
                    String input = scanner.nextLine();
                    List<String> keywords = Arrays.stream(input.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                    notificationService.configure(user, keywords);
                    System.out.println("Keywords updated.");
                }


                case 6 -> { return; }
                case 7 -> System.exit(0);
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }
}
