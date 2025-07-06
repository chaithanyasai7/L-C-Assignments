package com.newsaggregator.services;

import com.newsaggregator.dao.UserDao;
import com.newsaggregator.dao.impl.UserDaoImpl;
import com.newsaggregator.exceptions.AuthenticationException;
import com.newsaggregator.models.User;
import com.newsaggregator.models.UserRole;
import com.newsaggregator.utils.EmailValidator;
import com.newsaggregator.utils.PasswordUtil;

import java.util.Optional;

public class AuthenticationService {
    private final UserDao userDao;

    public AuthenticationService() {
        this.userDao = new UserDaoImpl();
    }

    public User login(String username, String password) throws AuthenticationException {
        Optional<User> userOpt = userDao.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOpt.get();

        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        return user;
    }

    public User register(String username, String email, String password) throws AuthenticationException {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty");
        }

        if (password == null || password.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters long");
        }

        if (!EmailValidator.isValid(email)) {
            throw new AuthenticationException("Invalid email format");
        }

        // Check if user already exists
        if (userDao.existsByUsername(username)) {
            throw new AuthenticationException("Username already exists");
        }

        if (userDao.existsByEmail(email)) {
            throw new AuthenticationException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(PasswordUtil.hashPassword(password));
        user.setRole(UserRole.USER);

        userDao.save(user);

        return user;
    }
}