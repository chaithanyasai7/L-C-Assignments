package model;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;

public class NewsArticle {
    private int id;
    private final String title;
    private final String content;
    private final String source;
    private String category;
    private final String url;
    private final Date date;
    private boolean hidden;

    public NewsArticle(int id, String title, String content, String source, String category, String url, Date date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.source = source;
        this.category = category;
        this.url = url;
        this.date = date;
        this.hidden = false;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSource() {
        return source;
    }

    public String getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }

    public Date getDate() {
        return date;
    }

    public LocalDate getLocalDate() {
        return date != null ? date.toLocalDate() : null;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void hide() {
        this.hidden = true;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsArticle that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
