package com.newsaggregator.client;

import com.newsaggregator.dao.UserDao;
import com.newsaggregator.dao.impl.UserDaoImpl;
import com.newsaggregator.exceptions.AuthenticationException;
import com.newsaggregator.models.NewsArticle;
import com.newsaggregator.models.User;
import com.newsaggregator.models.UserRole;
import com.newsaggregator.services.AuthenticationService;
import com.newsaggregator.services.NewsAggregationService;

import java.text.SimpleDateFormat;
import java.util.*;

public class NewsAggregatorClient {
    private final Scanner scanner;
    private final AuthenticationService authService;
    private NewsAggregationService newsService;
    private User currentUser;

    public NewsAggregatorClient() {
        this.scanner = new Scanner(System.in);
        this.authService = new AuthenticationService();
    }

    public void start() {
        while (true) {
            showMainMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            switch (choice) {
                case 1 -> login();
                case 2 -> signup();
                case 3 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\nWelcome to the News Aggregator application. Please choose the options below.");
        System.out.println("1. Login");
        System.out.println("2. Sign up");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }

    private void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        try {
            this.currentUser = authService.login(username, password);
           // this.newsService = new NewsAggregationService();
            System.out.println("Login successful!");
            // proceed to news dashboard if needed
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void signup() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        try {
            authService.register(username, email, password);
            System.out.println("Registration successful! Please log in.");
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void showAdminMenu() {
        while (true) {
            System.out.println("\n1. View the list of external servers and status");
            System.out.println("2. View the external server's details");
            System.out.println("3. Update/Edit the external server's details");
            System.out.println("4. Add new News Category");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> viewExternalServers();
                case 2 -> viewServerDetails();
                case 3 -> updateServerDetails();
                case 4 -> addNewsCategory();
                case 5 -> {
                    currentUser = null;
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showUserMenu() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        while (true) {
            System.out.println("\nWelcome to the News Application, " + currentUser.getUsername() +
                    "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
            System.out.println("Please choose the options below");
            System.out.println("1. Headlines");
            System.out.println("2. Saved Articles");
            System.out.println("3. Search");
            System.out.println("4. Notifications");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> showHeadlinesMenu();
                case 2 -> showSavedArticles();
                case 3 -> searchArticles();
                case 4 -> showNotificationMenu();
                case 5 -> {
                    currentUser = null;
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showHeadlinesMenu() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        while (true) {
            System.out.println("\nWelcome to the News Application, " + currentUser.getUsername() +
                    "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
            System.out.println("Please choose the options below");
            System.out.println("1. Today");
            System.out.println("2. Date range");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> {
                    showTodayHeadlines();
                    return;
                }
                case 2 -> {
                    showDateRangeHeadlines();
                    return;
                }
                case 3 -> {
                    currentUser = null;
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showTodayHeadlines() {
        showCategoryMenu(new Date());
    }

    private void showDateRangeHeadlines() {
        System.out.print("Enter start date (yyyy-MM-dd): ");
        Date startDate = parseDate(scanner.nextLine());

        System.out.print("Enter end date (yyyy-MM-dd): ");
        Date endDate = parseDate(scanner.nextLine());

        if (startDate == null || endDate == null) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd");
            return;
        }

        showCategoryMenuForDateRange(startDate, endDate);
    }

    private void showCategoryMenu(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        System.out.println("\nWelcome to the News Application, " + currentUser.getUsername() +
                "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
        System.out.println("Please choose the options below for Headlines");
        System.out.println("1. All");
        System.out.println("2. Business");
        System.out.println("3. Entertainment");
        System.out.println("4. Sports");
        System.out.println("5. Technology");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        String category = switch (choice) {
            case 1 -> "All";
            case 2 -> "Business";
            case 3 -> "Entertainment";
            case 4 -> "Sports";
            case 5 -> "Technology";
            default -> null;
        };

        if (category != null) {
            List<NewsArticle> articles = newsService.getHeadlines(category, date);
            displayArticles(articles);
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private void showCategoryMenuForDateRange(Date start, Date end) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        System.out.println("\nWelcome to the News Application, " + currentUser.getUsername() +
                "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
        System.out.println("Please choose the options below for Headlines");
        System.out.println("1. All");
        System.out.println("2. Business");
        System.out.println("3. Entertainment");
        System.out.println("4. Sports");
        System.out.println("5. Technology");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        String category = switch (choice) {
            case 1 -> "All";
            case 2 -> "Business";
            case 3 -> "Entertainment";
            case 4 -> "Sports";
            case 5 -> "Technology";
            default -> null;
        };

        if (category != null) {
            List<NewsArticle> articles = newsService.getHeadlinesByDateRange(category, start, end);
            displayArticles(articles);
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private void displayArticles(List<NewsArticle> articles) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        System.out.println("\nWelcome to the News Application, " + currentUser.getUsername() +
                "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
        System.out.println("\nH E A D L I N E S\n");

        if (articles.isEmpty()) {
            System.out.println("No articles found.");
            return;
        }

        for (NewsArticle article : articles) {
            System.out.println("\n" + String.valueOf('-').repeat(80));
            System.out.println("Article Id: " + article.getId());
            System.out.println(article.getTitle());
            System.out.println();

            String description = article.getDescription();
            if (description != null && description.length() > 200) {
                description = description.substring(0, 197) + "...";
            }
            System.out.println(description);
            System.out.println();
            System.out.println("source: " + article.getSource());
            System.out.println("URL: " + article.getUrl());
            System.out.println("Category: " + article.getCategory());
            System.out.println(String.valueOf('-').repeat(80));
        }

        showArticleActions();
    }

    private void showArticleActions() {
        while (true) {
            System.out.println("\n1. Back");
            System.out.println("2. Logout");
            System.out.println("3. Save Article");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> { return; }
                case 2 -> {
                    currentUser = null;
                    return;
                }
                case 3 -> {
                    System.out.print("Enter Article ID: ");
                    String articleId = scanner.nextLine();
                    try {
                        newsService.saveArticleForUser(currentUser.getId(), articleId);
                        System.out.println("Article saved successfully!");
                    } catch (Exception e) {
                        System.out.println("Error saving article: " + e.getMessage());
                    }
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void showSavedArticles() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        System.out.println("\nWelcome to the News Application, " + currentUser.getUsername() +
                "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
        System.out.println("\nS A V E D\n");

        List<NewsArticle> savedArticles = newsService.getSavedArticles(currentUser.getId());

        if (savedArticles.isEmpty()) {
            System.out.println("No saved articles found.");
        } else {
            for (NewsArticle article : savedArticles) {
                System.out.println("\n" + String.valueOf('-').repeat(80));
                System.out.println("Article Id: " + article.getId() + " " + article.getTitle());
                System.out.println();

                String description = article.getDescription();
                if (description != null && description.length() > 200) {
                    description = description.substring(0, 197) + "...";
                }
                System.out.println(description);
                System.out.println();
                System.out.println("source: " + article.getSource());
                System.out.println("URL: " + article.getUrl());
                System.out.println("Category: " + article.getCategory());
                System.out.println(String.valueOf('-').repeat(80));
            }
        }

        showSavedArticleActions();
    }

    private void showSavedArticleActions() {
        while (true) {
            System.out.println("\n1. Back");
            System.out.println("2. Logout");
            System.out.println("3. Delete Article");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> { return; }
                case 2 -> {
                    currentUser = null;
                    return;
                }
                case 3 -> {
                    System.out.print("Enter Article ID: ");
                    String articleId = scanner.nextLine();
                    try {
                        newsService.unsaveArticleForUser(currentUser.getId(), articleId);
                        System.out.println("Article removed from saved list!");
                    } catch (Exception e) {
                        System.out.println("Error removing article: " + e.getMessage());
                    }
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void searchArticles() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        System.out.println("\nWelcome to the News Application, " + currentUser.getUsername() +
                "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
        System.out.println("\nS E A R C H\n");

        System.out.print("Enter search query: ");
        String query = scanner.nextLine();

        List<NewsArticle> searchResults = newsService.searchArticles(query);

        System.out.println("\nResults for \"" + query + "\"");

        if (searchResults.isEmpty()) {
            System.out.println("No articles found matching your search.");
        } else {
            displayArticles(searchResults);
        }
    }

    private void showNotificationMenu() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        while (true) {
            System.out.println("\nWelcome to News Application, " + currentUser.getUsername() +
                    "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
            System.out.println("\nN O T I F I C A T I O N S\n");

            System.out.println("1. View Notifications");
            System.out.println("2. Configure Notifications");
            System.out.println("3. Back");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> viewNotifications();
                case 2 -> configureNotifications();
                case 3 -> { return; }
                case 4 -> {
                    currentUser = null;
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewNotifications() {
        // Get personalized articles based on user preferences
        List<NewsArticle> personalizedArticles = newsService.getPersonalizedArticles(currentUser);

        System.out.println("\nYour personalized news feed:");
        displayArticles(personalizedArticles);
    }

    private void configureNotifications() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        Date now = new Date();

        while (true) {
            System.out.println("\nWelcome to the News Application, " + currentUser.getUsername() +
                    "! Date: " + dateFormat.format(now) + " Time: " + timeFormat.format(now));
            System.out.println("\nC O N F I G U R E - N O T I F I C A T I O N S\n");

            Map<String, Boolean> settings = currentUser.getNotificationSettings();

            System.out.println("1. Business - " + (settings.get("Business") ? "Enabled" : "Disabled"));
            System.out.println("2. Entertainment - " + (settings.get("Entertainment") ? "Enabled" : "Disabled"));
            System.out.println("3. Sports - " + (settings.get("Sports") ? "Enabled" : "Disabled"));
            System.out.println("4. Technology - " + (settings.get("Technology") ? "Enabled" : "Disabled"));
            System.out.println("5. Keywords - " + (settings.get("Keywords") ? "Enabled" : "Disabled"));
            System.out.println("6. Back");
            System.out.println("7. Logout");
            System.out.print("Enter your option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> toggleNotificationSetting("Business");
                case 2 -> toggleNotificationSetting("Entertainment");
                case 3 -> toggleNotificationSetting("Sports");
                case 4 -> toggleNotificationSetting("Technology");
                case 5 -> {
                    toggleNotificationSetting("Keywords");
                    if (settings.get("Keywords")) {
                        configureKeywords();
                    }
                }
                case 6 -> { return; }
                case 7 -> {
                    currentUser = null;
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void toggleNotificationSetting(String category) {
        boolean currentValue = currentUser.getNotificationSettings().get(category);
        currentUser.getNotificationSettings().put(category, !currentValue);

        // Update in database
        UserDao userDao = new UserDaoImpl();
        userDao.update(currentUser);

        System.out.println(category + " notifications " + (!currentValue ? "enabled" : "disabled") + "!");
    }

    private void configureKeywords() {
        while (true) {
            System.out.println("\nCurrent keywords: " + currentUser.getKeywords());
            System.out.println("1. Add keyword");
            System.out.println("2. Remove keyword");
            System.out.println("3. Clear all keywords");
            System.out.println("4. Back");
            System.out.print("Enter your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter keyword to add: ");
                    String keyword = scanner.nextLine();
                    currentUser.getKeywords().add(keyword);
                    System.out.println("Keyword added!");
                }
                case 2 -> {
                    System.out.print("Enter keyword to remove: ");
                    String keyword = scanner.nextLine();
                    if (currentUser.getKeywords().remove(keyword)) {
                        System.out.println("Keyword removed!");
                    } else {
                        System.out.println("Keyword not found!");
                    }
                }
                case 3 -> {
                    currentUser.getKeywords().clear();
                    System.out.println("All keywords cleared!");
                }
                case 4 -> {
                    // Update in database before returning
                    UserDao userDao = new UserDaoImpl();
                    userDao.update(currentUser);
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }

            // Update in database after each change
            UserDao userDao = new UserDaoImpl();
            userDao.update(currentUser);
        }
    }

    // Admin specific methods
    private void viewExternalServers() {
        System.out.println("\nList of external servers:");
        System.out.println("1. News API - Active - last accessed: " + new SimpleDateFormat("dd MMM yyyy").format(new Date()));
        System.out.println("2. The News API - Active - last accessed: " + new SimpleDateFormat("dd MMM yyyy").format(new Date()));
    }

    private void viewServerDetails() {
        System.out.println("\nList of external server details:");
        System.out.println("1. News API - <API KEY>");
        System.out.println("2. The News API - <API KEY>");
    }

    private void updateServerDetails() {
        System.out.println("\nUpdate/Edit the external server's details");
        System.out.print("Enter the external server ID: ");
        int serverId = getIntInput();

        System.out.print("Enter the updated API key: ");
        String apiKey = scanner.nextLine();

        System.out.println("API key updated successfully!");
    }

    private void addNewsCategory() {
        System.out.print("\nEnter new category name: ");
        String category = scanner.nextLine();

        System.out.println("Category '" + category + "' added successfully!");
    }

    // Utility methods
    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        NewsAggregatorClient client = new NewsAggregatorClient();
        client.start();
    }
}