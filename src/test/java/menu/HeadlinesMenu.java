package menu;

import model.NewsArticle;
import model.User;
import model.UserRole;
import repository.Database;
import service.ArticleService;
import util.EmailSender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.*;

import static newsAggregationApplication.NewsAggregationApplication.articleService;

public class HeadlinesMenu {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void show(User user, ArticleService articleService, Database database, Scanner scanner) {
        while (true) {
            System.out.println("================ HEADLINES ================");
            System.out.println("1. Today");
            System.out.println("2. Date range");
            System.out.println("3. Logout");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> showTodayMenu(user, articleService, database, scanner);
                case "2" -> showDateRangeMenu(user, articleService, database, scanner);
                case "3" -> { return; }
                default -> System.out.println("Invalid option");
            }
        }
    }

    private static void showTodayMenu(User user, ArticleService articleService, Database database, Scanner scanner) {
        articleService.clearArticles();
        articleService.loadTodayArticles();

        java.sql.Date sqlToday = java.sql.Date.valueOf(LocalDate.now());
        List<NewsArticle> todayArticles = articleService.getArticlesByDate(sqlToday);

        List<NewsArticle> filteredToday = new ArrayList<>();
        for (NewsArticle article : todayArticles) {
            if (article.getDate().toLocalDate().isEqual(LocalDate.now()) && articleService.isVisible(article)) {
                filteredToday.add(article);
            }
        }


        if (filteredToday.isEmpty()) {
            System.out.println("No articles found for today.");
            return;
        }

        displayArticleOptions(user, database, scanner, filteredToday);
    }


    private static void showDateRangeMenu(User user, ArticleService articleService, Database database, Scanner scanner) {
        try {
            System.out.print("Enter start date (yyyy-MM-dd): ");
            Date start = dateFormat.parse(scanner.nextLine().trim());
            System.out.print("Enter end date (yyyy-MM-dd): ");
            Date end = dateFormat.parse(scanner.nextLine().trim());

            LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            List<NewsArticle> rangeArticles = articleService.getArticlesByDateRange(startDate, endDate);
            if (rangeArticles.isEmpty()) {
                System.out.println("No articles found in given date range.");
                return;
            }

            while (true) {
                System.out.println("1. All");
                System.out.println("2. Business");
                System.out.println("3. Entertainment");
                System.out.println("4. Sports");
                System.out.println("5. Technology");
                System.out.println("6. Back");

                String choice = scanner.nextLine().trim();
                List<NewsArticle> filtered;

                switch (choice) {
                    case "1" -> filtered = rangeArticles;
                    case "2" -> filtered = filterByCategory(rangeArticles, "business");
                    case "3" -> filtered = filterByCategory(rangeArticles, "entertainment");
                    case "4" -> filtered = filterByCategory(rangeArticles, "sports");
                    case "5" -> filtered = filterByCategory(rangeArticles, "technology");
                    case "6" -> { return; }
                    default -> {
                        System.out.println("Invalid option");
                        continue;
                    }
                }

                displayArticleOptions(user, database, scanner, filtered);
            }

        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd.");
        }
    }

    private static List<NewsArticle> filterByCategory(List<NewsArticle> articles, String category) {
        List<NewsArticle> filtered = new ArrayList<>();
        for (NewsArticle article : articles) {
            if (article.getCategory().equalsIgnoreCase(category)) {
                filtered.add(article);
            }
        }
        return filtered;
    }

    private static void displayArticleOptions(User user, Database database, Scanner scanner, List<NewsArticle> articles) {
        if (articles.isEmpty()) {
            System.out.println("No matching articles.");
            return;
        }

        for (NewsArticle article : articles) {
            System.out.println("Article Id: " + article.getId());
            System.out.println(article.getTitle());
            System.out.println(article.getContent());
            System.out.println("source: " + article.getSource());
            System.out.println("URL: " + article.getUrl());
            System.out.println("Category: " + article.getCategory());
            System.out.println();
        }

        while (true) {
            System.out.println("1. Back");
            System.out.println("2. Logout");
            System.out.println("3. Save Article");
            System.out.println("4. Report Article");

            String option = scanner.nextLine().trim();

            switch (option) {
                case "1" -> { return; }
                case "2" -> System.exit(0);
                case "3" -> {
                    System.out.print("Article Id: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        NewsArticle selected = null;
                        for (NewsArticle article : articles) {
                            if (article.getId() == id) {
                                selected = article;
                                break;
                            }
                        }
                        if (selected != null) {
                            database.saveArticleForUser(user, selected);
                            System.out.println("Article saved.");
                        } else {
                            System.out.println("Article ID not found.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                }
                case "4" -> {
                    System.out.print("Enter Article Id to report: ");
                    try {
                        int articleId = Integer.parseInt(scanner.nextLine().trim());
                        articleService.reportArticle(user, articleId);

                        for (User admin : database.getAllUsers()) {
                            if (admin.getRole() == UserRole.ADMIN) {
                                String body = "Article ID: " + articleId + " was reported by user: " + user.getUsername();
                                EmailSender.sendEmail(admin.getEmail(), "Article Reported", body);
                            }
                        }

                        System.out.println("Article reported. Admin notified.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID.");
                    }
                }
                default -> System.out.println("Invalid option");
            }
        }
    }
}
