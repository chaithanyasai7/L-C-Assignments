package api;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.NewsArticle;
import model.User;
import repository.Database;
import service.NewsFetcherService;
import service.NotificationService;

import java.util.*;

public class NotificationController {
    private final Gson gson = new Gson();
    private final Database database = new Database();
    private final NotificationService notificationService = new NotificationService();
    private final NewsFetcherService newsFetcher = new NewsFetcherService();

    public void registerRoutes(Javalin app) {
        app.post("/api/notifications/configure", this::configure);
        app.get("/api/notifications", this::getNotifications);
    }

    private void configure(Context context) {
        Map<String, Object> body = gson.fromJson(context.body(), Map.class);

        String email = (String) body.get("email");
        List<String> keywords = (List<String>) body.get("keywords");

        User user = database.getUserByEmail(email);
        if (user == null) {
            context.status(404).result("User not found");
            return;
        }

        user.setKeywords(keywords);
        context.status(200).result("Notification config saved");
    }

    private void getNotifications(Context context) {
        String email = context.queryParam("email");
        if (email == null) {
            context.status(400).result("Missing email");
            return;
        }

        User user = database.getUserByEmail(email);
        if (user == null) {
            context.status(404).result("User not found");
            return;
        }

        List<NewsArticle> allNews = newsFetcher.fetchTopHeadlines();
        List<NewsArticle> matchedArticles = notificationService.getMatchingArticles(user, allNews);
        context.json(matchedArticles);
    }
}
