package newsAggregationApplication;

import api.*;
import io.javalin.Javalin;
import model.NewsServer;
import model.ServerStatus;
import util.ConfigLoader;

import java.util.ArrayList;
import java.util.List;

public class NewsAggregationApiApplication {
    public static void main(String[] args) {
        int port = Integer.parseInt(ConfigLoader.get("server.port"));
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        }).start(port);

        List<NewsServer> newsServers = loadServers();
        List<String> categories = new ArrayList<>();

        new AuthenticationController().registerRoutes(app);
        new NewsController().registerRoutes(app);
        new ArticleController().registerRoutes(app);
        new NotificationController().registerRoutes(app);
        new AdminController(newsServers, categories).registerRoutes(app);

        System.out.println("News Aggregator API running at http://localhost:" + port + "/");
    }

    private static List<NewsServer> loadServers() {
        String newsApiKey = ConfigLoader.get("newsapi.key");
        String theNewsApiKey = ConfigLoader.get("thenewsapi.key");

        NewsServer server1 = new NewsServer(1, "NewsAPI", newsApiKey, ServerStatus.INACTIVE);
        NewsServer server2 = new NewsServer(2, "TheNewsAPI", theNewsApiKey, ServerStatus.ACTIVE);

        return new ArrayList<>(List.of(server1, server2));
    }
}
