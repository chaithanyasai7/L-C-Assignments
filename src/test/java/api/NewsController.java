package api;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.NewsArticle;
import service.NewsFetcherService;

import java.util.List;
import java.util.stream.Collectors;

public class NewsController {
    private final NewsFetcherService newsFetcher = new NewsFetcherService();
    //private final Gson gson = new Gson();

    public void registerRoutes(Javalin app) {
        app.get("/api/news/headlines", this::getHeadlines);
        app.get("/api/news/search", this::searchNews);
    }

    private void getHeadlines(Context context) {
        List<NewsArticle> headlines = newsFetcher.fetchTopHeadlines();
        context.json(headlines);
    }

    private void searchNews(Context context) {
        String query = context.queryParam("query") != null ? context.queryParam("query") : "";
        List<NewsArticle> allArticles = newsFetcher.fetchTopHeadlines();

        List<NewsArticle> filtered = allArticles.stream()
                .filter(article -> article.getTitle().toLowerCase().contains(query.toLowerCase())
                        || article.getContent().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        context.json(filtered);
    }

}
