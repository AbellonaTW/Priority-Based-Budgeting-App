package com.example.prioritybudget.service;

import com.example.prioritybudget.model.AppUser;
import com.example.prioritybudget.model.Budget;
import com.example.prioritybudget.repository.UserRepository;
import com.example.prioritybudget.repository.BudgetRepository;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository users;
    private final BudgetRepository budgets;
    private final PasswordEncoder encoder;

    public UserService(
            UserRepository users,
            BudgetRepository budgets,
            PasswordEncoder encoder) {
        this.users = users;
        this.budgets = budgets;
        this.encoder = encoder;
    }

    @Transactional
    public void register(String username, String password) {

        if (username == null ||
                !username.matches("[A-Za-z0-9_]{3,30}") ||
                password == null ||
                password.length() < 8) {
            throw new IllegalArgumentException(
                    "Username must be 3-30 letters, digits or underscores; "
                            + "password must contain at least 8 characters."
            );
        }

        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists."
            );
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));

        users.save(user);

        Budget budget = new Budget();
        budget.setUser(user);
        budgets.save(budget);
    }

    public AppUser getUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        AppUser user = getUser(username);

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}