package service;

import model.NewsArticle;
import model.User;
import repository.Database;
import util.ConfigLoader;
import util.EmailSender;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ArticleService {
    private final List<NewsArticle> articleStore = new ArrayList<>();
    private final Map<Integer, NewsArticle> articleMap = new HashMap<>();
    private final Map<Integer, Integer> articleReports = new HashMap<>();
    private final Set<String> hiddenCategories = new HashSet<>();
    private final Set<String> hiddenKeywords = new HashSet<>();
    private final Database database = new Database();

    private final Map<String, Integer> urlToIdMap = new HashMap<>();

    private int nextId = 1;


    public void addArticle(NewsArticle article) {
        articleStore.add(article);
        articleMap.put(article.getId(), article);
    }

    public static void normalizeAndFixArticle(NewsArticle article) {
        String category = article.getCategory();
        if (category == null || category.equalsIgnoreCase("N/A") || category.trim().isEmpty()) {
            category = "general";
        }
        article.setCategory(category.toLowerCase());
    }

    public void loadTodayArticles() {
        articleStore.clear();
        articleMap.clear();

        Set<String> seenUrls = new HashSet<>();
        String newsApiKey = ConfigLoader.get("newsapi.key");
        String theNewsApiKey = ConfigLoader.get("thenewsapi.key");

        NewsFetcherService fetcher = new NewsFetcherService();
        Date today = new Date(System.currentTimeMillis());

        List<NewsArticle> allFetched = new ArrayList<>();
        allFetched.addAll(fetcher.fetchFromNewsAPI(newsApiKey));
        allFetched.addAll(fetcher.fetchFromTheNewsAPI(theNewsApiKey));

        for (NewsArticle article : allFetched) {
            Date articleDate = article.getDate();
            String url = article.getUrl();

            if (isSameDay(articleDate, today) && seenUrls.add(url)) {
                normalizeAndFixArticle(article);
                if (articleMap.containsKey(url)) {
                    NewsArticle existing = articleMap.get(url);
                    article.setId(existing.getId());
                    if (existing.isHidden()) {
                        article.hide();
                    }
                } else {
                    if (!urlToIdMap.containsKey(url)) {
                        urlToIdMap.put(url, nextId++);
                    }
                    article.setId(urlToIdMap.get(url));
                }

                addArticle(article);
                database.saveArticle(article);
            }
        }

        System.out.println("Today's articles loaded.");
    }



    public void clearArticles() {
        articleStore.clear();
        articleMap.clear();
    }

    public boolean isVisible(NewsArticle article) {
        if (article.isHidden()) return false;
        if (hiddenCategories.contains(article.getCategory().toLowerCase())) return false;
        for (String keyword : hiddenKeywords) {
            if (article.getTitle().toLowerCase().contains(keyword)) return false;
        }
        return true;
    }

    public void reportArticle(User reporter, int articleId) {
        int count = articleReports.getOrDefault(articleId, 0) + 1;
        articleReports.put(articleId, count);

        NewsArticle article = articleMap.get(articleId);
        if (article != null) {
            System.out.println("Article ID " + articleId + " has been reported " + count + " time(s).");
            if (count >= 3) {
                System.out.println("Article ID " + articleId + " is now hidden.");
            }

            notifyAdmins(article, reporter);
        }
    }


    private void notifyAdmins(NewsArticle article, User reporter) {
        Collection<User> users = database.getAllUsers();
        for (User u : users) {
            if (u.getRole().name().equalsIgnoreCase("ADMIN")) {
                String subject = "Article Reported: ID " + article.getId();
                String message = String.format(
                        "User '%s' reported article:\nTitle: %s\nCategory: %s\nURL: %s\n\nCurrent Report Count: %d",
                        reporter.getUsername(), article.getTitle(), article.getCategory(), article.getUrl(),
                        articleReports.get(article.getId())
                );
                EmailSender.sendEmail(u.getEmail(), subject, message);
            }
        }
    }

    public List<NewsArticle> getAllArticles() {
        return new ArrayList<>(articleStore);
    }

    public void hideCategory(String category) {
        hiddenCategories.add(category.toLowerCase());
    }

    public void hideKeyword(String keyword) {
        hiddenKeywords.add(keyword.toLowerCase());
    }

    public List<NewsArticle> getArticlesByDate(Date date) {
        List<NewsArticle> results = new ArrayList<>();
        for (NewsArticle article : articleStore) {
            if (isSameDay(article.getDate(), date) && isVisible(article)) {
                results.add(article);
            }
        }
        return results;
    }

    public List<NewsArticle> getArticlesByDateRange(LocalDate start, LocalDate end) {
        List<NewsArticle> results = new ArrayList<>();
        for (NewsArticle article : database.getAllArticles()) {
            Date sqlDate = article.getDate();
            LocalDate articleDate = new java.util.Date(sqlDate.getTime()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (!articleDate.isBefore(start) && !articleDate.isAfter(end) && isVisible(article)) {
                results.add(article);
            }
        }
        return results;
    }

    public List<NewsArticle> search(String query) {
        List<NewsArticle> results = new ArrayList<>();
        for (NewsArticle article : articleStore) {
            if (isVisible(article) && (
                    article.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                            article.getContent().toLowerCase().contains(query.toLowerCase())
            )) {
                results.add(article);
            }
        }
        return results;
    }

    private boolean isSameDay(Date d1, Date d2) {
        return d1.toLocalDate().equals(d2.toLocalDate());
    }
}
