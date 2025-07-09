package newsAggregationApplication;

import menu.HeadlinesMenu;
import menu.NotificationMenu;
import menu.SavedArticlesMenu;
import menu.SearchMenu;
import model.*;
import java.text.SimpleDateFormat;
import repository.Database;
import service.*;
import util.ConfigLoader;

import java.util.*;

public class NewsAggregationApplication {

    public static final ArticleService articleService = new ArticleService();

    public static final Database database = new Database();
    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthenticationService authenticationService = new AuthenticationService(database);
    public static final NotificationService notificationService = new NotificationService();
    private static final List<NewsServer> newsServers = new ArrayList<>();
    private static final List<String> categories = new ArrayList<>();

    public static void main(String[] args) {
        loadData();
        while (true) {
            System.out.println("Welcome to the News Aggregator application. Please choose the options below.");
            System.out.println("1. Login");
            System.out.println("2. Sign up");
            System.out.println("3. Exit");
            int choice = getIntInput("Enter your choice: ");
            switch (choice) {
                case 1 -> loginFlow();
                case 2 -> signupFlow();
                case 3 -> {
                    System.out.println("Exiting... Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void loginFlow() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        User user = authenticationService.login(email, password);
        if (user == null) {
            System.out.println("Invalid credentials.");
            return;
        }
        if (user.getRole() == UserRole.ADMIN) {
            showAdminMenu(user);
        } else {
            showUserMenu(user);
        }
    }

    private static void signupFlow() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        UserRole role = UserRole.USER;
        User user = authenticationService.signup(username, email, password, role);
        if (user != null) {
            database.saveUser(user);
            System.out.println("Signup successful.");
        } else {
            System.out.println("Signup failed. Try again.");
        }
    }

    private static void showAdminMenu(User admin) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. View the list of external servers and status");
            System.out.println("2. View the external server’s details");
            System.out.println("3. Update/Edit the external server’s details");
            System.out.println("4. Add News Category");
            System.out.println("5. Hide Articles by Category");
            System.out.println("6. Hide Articles by Keyword");
            System.out.println("7. Logout");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    ensureServersInitialized();

                    if (NewsAggregationApplication.newsServers.isEmpty()) {
                        System.out.println("No news servers available.");
                    } else {
                        System.out.println("\nList of external servers:");
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy");

                        int count = 1;
                        for (NewsServer server : NewsAggregationApplication.newsServers) {
                            String status = server.getStatus() == ServerStatus.ACTIVE ? "Active" : "Not Active";
                            String formattedDate = displayFormat.format(server.getLastAccessed());
                            System.out.printf("%d. %s - %s - last accessed: %s\n",
                                    count++, server.getName(), status, formattedDate);
                        }
                    }
                }
                case "2" -> {
                    ensureServersInitialized();
                    System.out.print("Enter Server ID to view details: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        NewsServer server = getServerById(id);
                        if (server != null) {
                            System.out.println("List of external server details:");
                            System.out.println("Server ID: " + server.getId());
                            System.out.println("Name: " + server.getName());
                            System.out.println("API Key: " + server.getApiKey());
                            System.out.println("Status: " + server.getStatus());
                        } else {
                            System.out.println("Server not found.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID.");
                    }
                }
                case "3" -> {
                    ensureServersInitialized();

                    System.out.println("\nUpdate/Edit the external server’s details");

                    System.out.print("Enter the external server ID: ");
                    int serverId;
                    try {
                        serverId = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid server ID format.");
                        break;
                    }

                    NewsServer serverToUpdate = null;
                    for (NewsServer s : NewsAggregationApplication.newsServers) {
                        if (s.getId() == serverId) {
                            serverToUpdate = s;
                            break;
                        }
                    }

                    if (serverToUpdate == null) {
                        System.out.println("Server not found.");
                        break;
                    }

                    System.out.print("Enter the updated API key: ");
                    String newApiKey = scanner.nextLine().trim();
                    serverToUpdate.setApiKey(newApiKey);

                    serverToUpdate.updateLastAccessed();
                    System.out.println("API key updated successfully.");
                }

                case "4" -> {
                    System.out.print("Enter new category: ");
                    String newCategory = scanner.nextLine().trim();
                    if (!categories.contains(newCategory)) {
                        categories.add(newCategory);
                        System.out.println("Category added: " + newCategory);
                    } else {
                        System.out.println("Category already exists.");
                    }
                }
                case "5" -> {
                    System.out.print("Enter category to hide: ");
                    String category = scanner.nextLine().trim();
                    articleService.hideCategory(category);
                    System.out.println("Articles in category '" + category + "' are now hidden.");
                }
                case "6" -> {
                    System.out.print("Enter keyword to filter and hide articles: ");
                    String keyword = scanner.nextLine().trim();
                    articleService.hideKeyword(keyword);
                    System.out.println("Articles containing keyword '" + keyword + "' are now hidden.");
                }
                case "7" -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void showUserMenu(User user) {
        while (true) {
            String currentDate = java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

            String currentTime = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));

            System.out.printf("Welcome to the News Application, %s! Date: %s Time: %s\n",
                    user.getUsername(), currentDate, currentTime);

            System.out.println("1. Headlines");
            System.out.println("2. Saved Articles");
            System.out.println("3. Search");
            System.out.println("4. Notifications");
            System.out.println("5. Logout");

            int choice = getIntInput("Enter your choice: ");
            switch (choice) {
                case 1 -> HeadlinesMenu.show(user, articleService, database, scanner);
                case 2 -> SavedArticlesMenu.show(user, database, scanner);
                case 3 -> SearchMenu.show(user, articleService, database, scanner);
                case 4 -> NotificationMenu.show(user, notificationService, articleService, scanner);
                case 5 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void ensureServersInitialized() {
        if (NewsAggregationApplication.newsServers.isEmpty()) {
            String newsApiKey = ConfigLoader.get("newsapi.key");
            String theNewsApiKey = ConfigLoader.get("thenewsapi.key");

            NewsServer newsApi = new NewsServer(1, "News API", newsApiKey, ServerStatus.ACTIVE);
            NewsServer theNewsApi = new NewsServer(2, "The News API", theNewsApiKey, ServerStatus.ACTIVE);

            NewsAggregationApplication.newsServers.add(newsApi);
            NewsAggregationApplication.newsServers.add(theNewsApi);
        }
    }

    private static NewsServer getServerById(int id) {
        for (NewsServer s : NewsAggregationApplication.newsServers) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
