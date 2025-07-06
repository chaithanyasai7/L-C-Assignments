package com.newsaggregator.services;

import com.newsaggregator.models.NewsArticle;
import com.newsaggregator.models.User;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
    private final Session session;
    private static final String FROM_EMAIL = "noreply@newsaggregator.com";
    private static final String ADMIN_EMAIL = "admin@newsaggregator.com";

    public EmailService() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "25");

        this.session = Session.getInstance(props);
    }

    public CompletableFuture<Void> sendNotificationEmail(User user, List<NewsArticle> articles) {
        return CompletableFuture.runAsync(() -> {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
                message.setSubject("Your Daily News Digest");

                StringBuilder content = new StringBuilder();
                content.append("<html><body>");
                content.append("<h2>Hello ").append(user.getUsername()).append(",</h2>");
                content.append("<p>Here are your personalized news articles for today:</p>");

                for (NewsArticle article : articles) {
                    content.append("<div style='margin-bottom: 20px;'>");
                    content.append("<h3>").append(article.getTitle()).append("</h3>");
                    content.append("<p>").append(article.getDescription()).append("</p>");
                    content.append("<p><a href='").append(article.getUrl()).append("'>Read more</a></p>");
                    content.append("<hr/>");
                    content.append("</div>");
                }

                content.append("</body></html>");

                message.setContent(content.toString(), "text/html");

                // For demo purposes, just log the email
                System.out.println("Email sent to: " + user.getEmail());
                System.out.println("Subject: Your Daily News Digest");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendAdminNotification(String subject, String body) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(ADMIN_EMAIL));
            message.setSubject(subject);
            message.setText(body);

            // For demo purposes, just log the email
            System.out.println("Admin notification sent: " + subject);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
