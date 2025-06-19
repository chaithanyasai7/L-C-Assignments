package src.test.java.service;

import model.User;
import model.UserRole;
import repository.Database;

public class AuthenticationService {
    private final Database database;

    public AuthenticationService(Database database) {
        this.database = database;
    }

    public User login(String email, String password) {
        User user = database.getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User signup(String username, String email, String password, UserRole role) {
        if (database.getUserByEmail(email) != null) return null;

        int id = database.getAllUsers().size() + 1;
        return new User(id, username, email, password, role);
    }
}
