package api;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.User;
import model.UserRole;
import repository.Database;
import service.AuthenticationService;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationController {
    private final Gson gson = new Gson();
    private final AuthenticationService authService = new AuthenticationService(new Database());

    public void registerRoutes(Javalin app) {
        app.post("/api/auth/signup", this::handleSignup);
        app.post("/api/auth/login", this::handleLogin);
    }

    private void handleSignup(Context context) {
        Map<String, String> request = gson.fromJson(context.body(), Map.class);
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String userRole = request.getOrDefault("role", "USER").toUpperCase();

        UserRole role;
        try {
            role = UserRole.valueOf(userRole);
        } catch (IllegalArgumentException e) {
            role = UserRole.USER;
        }

        User user = authService.signup(username, email, password, role);
        if (user == null) {
            context.status(400).result("Signup failed: Invalid input or duplicate email");
        } else {
            new Database().saveUser(user);
            context.status(201).json(user);
        }
    }

    private void handleLogin(Context context) {
        Map<String, String> request = gson.fromJson(context.body(), Map.class);
        String email = request.get("email");
        String password = request.get("password");

        User user = authService.login(email, password);
        if (user == null) {
            context.status(401).result("Invalid credentials");
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("token", "mock-token");
            context.status(200).json(response);
        }
    }
}
