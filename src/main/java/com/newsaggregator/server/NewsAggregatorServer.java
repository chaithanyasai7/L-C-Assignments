package com.newsaggregator.server;

import com.newsaggregator.dao.DatabaseConnection;
import com.newsaggregator.dao.NewsArticleDao;
import com.newsaggregator.dao.UserDao;
import com.newsaggregator.dao.impl.NewsArticleDaoImpl;
import com.newsaggregator.dao.impl.UserDaoImpl;
import com.newsaggregator.exceptions.AuthenticationException;
import com.newsaggregator.models.NewsArticle;
import com.newsaggregator.models.User;
import com.newsaggregator.services.AuthenticationService;
import com.newsaggregator.services.NewsAggregationService;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.h2.tools.Server;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.sql.SQLException;

public class NewsAggregatorServer {
    private static final Logger LOGGER = Logger.getLogger(NewsAggregatorServer.class.getName());
    private final NewsAggregationService newsService;
    private final AuthenticationService authService;
    private final UserDao userDao;
    private final NewsArticleDao articleDao;
    private HttpServer server;
    private Server h2Server;

    public NewsAggregatorServer() {
        this.newsService = new NewsAggregationService();
        this.authService = new AuthenticationService();
        this.userDao = new UserDaoImpl();
        this.articleDao = new NewsArticleDaoImpl();
        setupLogger();
    }

    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("news-aggregator-server.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.INFO);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to setup logger", e);
        }
    }

    public void start(int port) throws IOException {
        // Start H2 Console
        try {
            h2Server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 Console started at http://localhost:8082");
            System.out.println("Use JDBC URL: jdbc:h2:./newsaggregator");
            System.out.println("Username: sa, Password: (leave empty)");
            System.out.println("-".repeat(50));
            LOGGER.info("H2 Console started on port 8082");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to start H2 Console", e);
            e.printStackTrace();
        }

        // Start HTTP Server
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Configure endpoints
        server.createContext("/api/headlines", new HeadlinesHandler());
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/articles", new ArticlesHandler());
        server.createContext("/api/articles/save", new SaveArticleHandler());
        server.createContext("/api/articles/report", new ReportArticleHandler());
        server.createContext("/api/articles/like", new LikeArticleHandler());
        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/notifications", new NotificationsHandler());
        server.createContext("/api/admin/servers", new ExternalServersHandler());
        server.createContext("/api/admin/categories", new CategoriesHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        LOGGER.info("News Aggregator Server started on port " + port);
        System.out.println("News Aggregator Server started on port " + port);
        System.out.println("API Base URL: http://localhost:" + port + "/api");
        System.out.println("-".repeat(50));

        // Initial fetch of news
        System.out.println("Fetching initial news articles...");
        newsService.fetchNewsFromAllSources();
    }

    public void stop() {
        // Stop H2 Console
        if (h2Server != null) {
            h2Server.stop();
            LOGGER.info("H2 Console stopped");
        }

        // Stop HTTP Server
        if (server != null) {
            server.stop(0);
            LOGGER.info("HTTP Server stopped");
        }

        // Shutdown services
        newsService.shutdown();
        DatabaseConnection.closeConnection();
        LOGGER.info("Server shutdown complete");
    }

    // Base handler class
    abstract class BaseHandler implements HttpHandler {
        protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            LOGGER.info(String.format("Response: %d - %s", statusCode, exchange.getRequestURI()));
        }

        protected void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            JSONObject error = new JSONObject();
            error.put("status", "error");
            error.put("message", message);
            sendResponse(exchange, statusCode, error.toString());
        }

        protected String readRequestBody(HttpExchange exchange) throws IOException {
            try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        }

        protected Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null && !query.isEmpty()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return params;
        }
    }

    // Handler implementations
    class HeadlinesHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String category = params.getOrDefault("category", "All");
                String dateStr = params.getOrDefault("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                    List<NewsArticle> articles = newsService.getHeadlines(category, date);

                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("count", articles.size());

                    JSONArray articlesArray = new JSONArray();
                    for (NewsArticle article : articles) {
                        articlesArray.put(articleToJson(article));
                    }
                    response.put("articles", articlesArray);

                    sendResponse(exchange, 200, response.toString());
                } catch (Exception e) {
                    sendErrorResponse(exchange, 400, "Invalid parameters");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class SearchHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String query = params.getOrDefault("q", "");

                if (query.isEmpty()) {
                    sendErrorResponse(exchange, 400, "Query parameter 'q' is required");
                    return;
                }

                List<NewsArticle> searchResults = newsService.searchArticles(query);

                JSONObject response = new JSONObject();
                response.put("status", "success");
                response.put("query", query);
                response.put("count", searchResults.size());

                JSONArray resultsArray = new JSONArray();
                for (NewsArticle article : searchResults) {
                    resultsArray.put(articleToJson(article));
                }
                response.put("results", resultsArray);

                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class ArticlesHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET" -> handleGetArticles(exchange);
                case "POST" -> handleCreateArticle(exchange);
                case "PUT" -> handleUpdateArticle(exchange);
                case "DELETE" -> handleDeleteArticle(exchange);
                default -> sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }

        private void handleGetArticles(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String articleId = params.get("id");

            if (articleId != null) {
                // Get single article
                Optional<NewsArticle> article = articleDao.findById(articleId);
                if (article.isPresent()) {
                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("article", articleToJson(article.get()));
                    sendResponse(exchange, 200, response.toString());
                } else {
                    sendErrorResponse(exchange, 404, "Article not found");
                }
            } else {
                // Get all articles
                List<NewsArticle> articles = articleDao.findVisibleArticles();
                JSONObject response = new JSONObject();
                response.put("status", "success");
                response.put("count", articles.size());

                JSONArray articlesArray = new JSONArray();
                for (NewsArticle article : articles) {
                    articlesArray.put(articleToJson(article));
                }
                response.put("articles", articlesArray);

                sendResponse(exchange, 200, response.toString());
            }
        }

        private void handleCreateArticle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            JSONObject json = new JSONObject(body);

            NewsArticle article = new NewsArticle();
            article.setId(UUID.randomUUID().toString());
            article.setTitle(json.getString("title"));
            article.setDescription(json.getString("description"));
            article.setContent(json.optString("content", ""));
            article.setUrl(json.optString("url", ""));
            article.setSource(json.optString("source", ""));
            article.setCategory(json.optString("category", "General"));
            article.setPublishedAt(new Date());

            articleDao.save(article);

            JSONObject response = new JSONObject();
            response.put("status", "created");
            response.put("article", articleToJson(article));

            sendResponse(exchange, 201, response.toString());
        }

        private void handleUpdateArticle(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            JSONObject json = new JSONObject(body);

            String articleId = json.getString("id");
            Optional<NewsArticle> existingArticle = articleDao.findById(articleId);

            if (existingArticle.isPresent()) {
                NewsArticle article = existingArticle.get();

                if (json.has("title")) article.setTitle(json.getString("title"));
                if (json.has("description")) article.setDescription(json.getString("description"));
                if (json.has("content")) article.setContent(json.getString("content"));
                if (json.has("category")) article.setCategory(json.getString("category"));
                if (json.has("hidden")) article.setHidden(json.getBoolean("hidden"));

                articleDao.update(article);

                JSONObject response = new JSONObject();
                response.put("status", "updated");
                response.put("article", articleToJson(article));

                sendResponse(exchange, 200, response.toString());
            } else {
                sendErrorResponse(exchange, 404, "Article not found");
            }
        }

        private void handleDeleteArticle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String articleId = params.get("id");

            if (articleId != null) {
                articleDao.delete(articleId);
                sendResponse(exchange, 200, "{\"status\": \"deleted\"}");
            } else {
                sendErrorResponse(exchange, 400, "Article ID is required");
            }
        }
    }

    class SaveArticleHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String userId = json.getString("userId");
                String articleId = json.getString("articleId");

                newsService.saveArticleForUser(userId, articleId);

                sendResponse(exchange, 200, "{\"status\": \"saved\"}");
            } else if ("DELETE".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String userId = json.getString("userId");
                String articleId = json.getString("articleId");

                newsService.unsaveArticleForUser(userId, articleId);

                sendResponse(exchange, 200, "{\"status\": \"unsaved\"}");
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class ReportArticleHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String userId = json.getString("userId");
                String articleId = json.getString("articleId");

                newsService.reportArticle(articleId, userId);

                sendResponse(exchange, 200, "{\"status\": \"reported\"}");
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class LikeArticleHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String articleId = json.getString("articleId");
                String action = json.getString("action"); // "like" or "dislike"

                if ("like".equals(action)) {
                    newsService.likeArticle(articleId);
                } else if ("dislike".equals(action)) {
                    newsService.dislikeArticle(articleId);
                } else {
                    sendErrorResponse(exchange, 400, "Invalid action");
                    return;
                }

                sendResponse(exchange, 200, "{\"status\": \"success\"}");
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class UsersHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String userId = params.get("id");

                if (userId != null) {
                    Optional<User> user = userDao.findById(userId);
                    if (user.isPresent()) {
                        JSONObject response = new JSONObject();
                        response.put("status", "success");
                        response.put("user", userToJson(user.get()));
                        sendResponse(exchange, 200, response.toString());
                    } else {
                        sendErrorResponse(exchange, 404, "User not found");
                    }
                } else {
                    List<User> users = userDao.findAll();
                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("count", users.size());

                    JSONArray usersArray = new JSONArray();
                    for (User user : users) {
                        usersArray.put(userToJson(user));
                    }
                    response.put("users", usersArray);

                    sendResponse(exchange, 200, response.toString());
                }
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class LoginHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String username = json.getString("username");
                String password = json.getString("password");

                try {
                    User user = authService.login(username, password);

                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("user", userToJson(user));
                    response.put("token", generateToken(user)); // Simple token generation

                    sendResponse(exchange, 200, response.toString());

                    LOGGER.info("User logged in: " + username);
                } catch (AuthenticationException e) {
                    sendErrorResponse(exchange, 401, e.getMessage());
                }
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class RegisterHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String username = json.getString("username");
                String email = json.getString("email");
                String password = json.getString("password");

                try {
                    User user = authService.register(username, email, password);

                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("user", userToJson(user));

                    sendResponse(exchange, 201, response.toString());

                    LOGGER.info("New user registered: " + username);
                } catch (AuthenticationException e) {
                    sendErrorResponse(exchange, 400, e.getMessage());
                }
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class NotificationsHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String userId = params.get("userId");

                if (userId == null) {
                    sendErrorResponse(exchange, 400, "User ID is required");
                    return;
                }

                Optional<User> userOpt = userDao.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    List<NewsArticle> personalizedArticles = newsService.getPersonalizedArticles(user);

                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("count", personalizedArticles.size());

                    JSONArray articlesArray = new JSONArray();
                    for (NewsArticle article : personalizedArticles) {
                        articlesArray.put(articleToJson(article));
                    }
                    response.put("articles", articlesArray);

                    sendResponse(exchange, 200, response.toString());
                } else {
                    sendErrorResponse(exchange, 404, "User not found");
                }
            } else if ("PUT".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String userId = json.getString("userId");
                JSONObject settings = json.getJSONObject("settings");

                Optional<User> userOpt = userDao.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Update notification settings
                    for (String key : settings.keySet()) {
                        user.getNotificationSettings().put(key, settings.getBoolean(key));
                    }

                    // Update keywords if provided
                    if (json.has("keywords")) {
                        JSONArray keywordsArray = json.getJSONArray("keywords");
                        user.getKeywords().clear();
                        for (int i = 0; i < keywordsArray.length(); i++) {
                            user.getKeywords().add(keywordsArray.getString(i));
                        }
                    }

                    userDao.update(user);

                    sendResponse(exchange, 200, "{\"status\": \"updated\"}");
                } else {
                    sendErrorResponse(exchange, 404, "User not found");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class ExternalServersHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                JSONObject response = new JSONObject();
                response.put("status", "success");

                JSONArray servers = new JSONArray();

                JSONObject newsApi = new JSONObject();
                newsApi.put("id", "server-001");
                newsApi.put("name", "News API");
                newsApi.put("status", "Active");
                newsApi.put("lastAccessed", new SimpleDateFormat("dd MMM yyyy").format(new Date()));
                servers.put(newsApi);

                JSONObject theNewsApi = new JSONObject();
                theNewsApi.put("id", "server-002");
                theNewsApi.put("name", "The News API");
                theNewsApi.put("status", "Active");
                theNewsApi.put("lastAccessed", new SimpleDateFormat("dd MMM yyyy").format(new Date()));
                servers.put(theNewsApi);

                response.put("servers", servers);
                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    class CategoriesHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                JSONObject json = new JSONObject(body);

                String category = json.getString("category");

                // In a real implementation, you would store this in the database
                JSONObject response = new JSONObject();
                response.put("status", "success");
                response.put("message", "Category '" + category + "' added successfully");

                sendResponse(exchange, 201, response.toString());

                LOGGER.info("New category added: " + category);
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    // Helper methods
    private JSONObject articleToJson(NewsArticle article) {
        JSONObject json = new JSONObject();
        json.put("id", article.getId());
        json.put("title", article.getTitle());
        json.put("description", article.getDescription());
        json.put("content", article.getContent());
        json.put("url", article.getUrl());
        json.put("source", article.getSource());
        json.put("category", article.getCategory());
        json.put("publishedAt", article.getPublishedAt().getTime());
        json.put("likes", article.getLikes());
        json.put("dislikes", article.getDislikes());
        json.put("hidden", article.isHidden());
        return json;
    }

    private JSONObject userToJson(User user) {
        JSONObject json = new JSONObject();
        json.put("id", user.getId());
        json.put("username", user.getUsername());
        json.put("email", user.getEmail());
        json.put("role", user.getRole().toString());
        json.put("createdAt", user.getCreatedAt().getTime());

        JSONObject settings = new JSONObject();
        for (Map.Entry<String, Boolean> entry : user.getNotificationSettings().entrySet()) {
            settings.put(entry.getKey(), entry.getValue());
        }
        json.put("notificationSettings", settings);

        JSONArray keywords = new JSONArray();
        for (String keyword : user.getKeywords()) {
            keywords.put(keyword);
        }
        json.put("keywords", keywords);

        return json;
    }

    private String generateToken(User user) {
        // Simple token generation - in production, use JWT or similar
        return Base64.getEncoder().encodeToString(
                (user.getId() + ":" + System.currentTimeMillis()).getBytes()
        );
    }

    public static void main(String[] args) {
        NewsAggregatorServer server = new NewsAggregatorServer();

        try {
            int port = 8080;
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            server.start(port);

            // Print startup summary
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Server started successfully!");
            System.out.println("=".repeat(50));
            System.out.println("Services:");
            System.out.println("  - API Server: http://localhost:" + port);
            System.out.println("  - H2 Console: http://localhost:8082");
            System.out.println("  - Log file: news-aggregator-server.log");
            System.out.println("=".repeat(50));
            System.out.println("Press Ctrl+C to stop the server\n");

            // Keep server running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                server.stop();
                System.out.println("Server stopped successfully!");
            }));

            // Wait indefinitely
            Thread.currentThread().join();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Server error", e);
            e.printStackTrace();
        }
    }
}