package api;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.NewsArticle;
import model.User;
import repository.Database;
import service.AuthenticationService;

import java.util.List;
import java.util.Map;

public class ArticleController {
    private final Gson gson = new Gson();
    private final Database database = new Database();
    private final AuthenticationService authService = new AuthenticationService(database);

    public void registerRoutes(Javalin app) {
        app.post("/api/articles/save", this::saveArticle);
        app.get("/api/articles/saved", this::getSavedArticles);
        app.delete("/api/articles/{id}", this::deleteSavedArticle);
    }

    private void saveArticle(Context context) {
        Map<String, String> body = gson.fromJson(context.body(), Map.class);
        String email = body.get("email");
        String articleIdParam = body.get("articleId");

        if (email == null || articleIdParam == null) {
            context.status(400).result("Missing email or articleId");
            return;
        }

        int articleId;
        try {
            articleId = Integer.parseInt(articleIdParam);
        } catch (NumberFormatException e) {
            context.status(400).result("Invalid articleId format");
            return;
        }

        User user = database.getUserByEmail(email);
        if (user == null) {
            context.status(404).result("User not found");
            return;
        }

        NewsArticle article = database.getAllArticles().stream()
                .filter(articleData -> articleData.getId() == articleId)
                .findFirst()
                .orElse(null);

        if (article == null) {
            context.status(404).result("Article not found");
            return;
        }

        database.saveArticleForUser(user, article);
        context.status(200).result("Article saved for user");
    }

    private void getSavedArticles(Context context) {
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

        List<NewsArticle> saved = database.getSavedArticles(user);
        context.json(saved);
    }

    private void deleteSavedArticle(Context context) {
        String email = context.queryParam("email");
        String idParam = context.pathParam("id");

        if (email == null || idParam == null) {
            context.status(400).result("Missing email or articleId");
            return;
        }

        int articleId;
        try {
            articleId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            context.status(400).result("Invalid article ID format");
            return;
        }

        User user = database.getUserByEmail(email);
        if (user == null) {
            context.status(404).result("User not found");
            return;
        }

        database.deleteSavedArticle(user, articleId);
        context.status(200).result("Article removed from saved list");
    }
}
