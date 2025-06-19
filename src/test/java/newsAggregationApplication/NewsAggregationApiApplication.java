package src.test.java.newsAggregationApplication;

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

        System.out.println("News Aggregator API running at http://localhost:7070/");
    }

    private static List<NewsServer> loadServers() {
        String newsApiKey = ConfigLoader.get("newsapi.key");
        String theNewsApiKey = ConfigLoader.get("thenewsapi.key");

        NewsServer server1 = new NewsServer(1, "NewsAPI", newsApiKey, ServerStatus.ACTIVE);
        NewsServer server2 = new NewsServer(2, "TheNewsAPI", theNewsApiKey, ServerStatus.INACTIVE);

        return new ArrayList<>(List.of(server1, server2));
    }
}
