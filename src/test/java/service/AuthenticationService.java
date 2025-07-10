package service;

import model.User;
import model.UserRole;
import repository.Database;

import java.util.regex.Pattern;

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
        if (!isValidEmail(email)) return null;
        if (database.getUserByEmail(email) != null) return null;

        int id = database.getAllUsers().size() + 1;
        return new User(id, username, email, password, role);
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(regex, email);
    }
}
