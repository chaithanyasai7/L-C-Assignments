package api;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.NewsServer;

import java.util.*;

public class AdminController {
    private final Gson gson = new Gson();
    private final List<NewsServer> newsServers = new ArrayList<>();
    private final Set<String> categories = new HashSet<>();

    public AdminController(List<NewsServer> existingServers, List<String> existingCategories) {
        this.newsServers.addAll(existingServers);
        this.categories.addAll(existingCategories);
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/admin/servers", this::getAllServers);
        app.get("/api/admin/servers/{id}", this::getServerById);
        app.put("/api/admin/servers/{id}", this::updateApiKey);
        app.post("/api/admin/categories", this::addCategory);
    }


    private void getAllServers(Context context) {
        context.json(newsServers);
    }

    private void getServerById(Context context) {
        int id = Integer.parseInt(context.pathParam("id"));
        NewsServer server = newsServers.stream()
                .filter(serverData -> serverData.getId() == id)
                .findFirst()
                .orElse(null);

        if (server == null) {
            context.status(404).result("Server not found");
        } else {
            context.json(server);
        }
    }

    private void updateApiKey(Context context) {
        int id = Integer.parseInt(context.pathParam("id"));
        Map<String, String> body = gson.fromJson(context.body(), Map.class);
        String newKey = body.get("apiKey");

        NewsServer server = newsServers.stream()
                .filter(serverItem -> serverItem.getId() == id)
                .findFirst()
                .orElse(null);

        if (server == null) {
            context.status(404).result("Server not found");
        } else {
            server.setApiKey(newKey);
            context.status(200).result("API key updated");
        }
    }

    private void addCategory(Context context) {
        Map<String, String> body = gson.fromJson(context.body(), Map.class);
        String category = body.get("category");

        if (category == null || category.trim().isEmpty()) {
            context.status(400).result("Invalid category");
            return;
        }

        boolean added = categories.add(category.trim().toLowerCase());
        if (added) {
            context.status(201).result("Category added");
        } else {
            context.status(200).result("Category already exists");
        }
    }
}
