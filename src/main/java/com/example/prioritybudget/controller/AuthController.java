package com.example.prioritybudget.controller;

import com.example.prioritybudget.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    public record RegisterRequest(
            String username,
            String password
    ) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        try {
            users.register(
                    request.username(),
                    request.password()
            );

            return ResponseEntity.status(201).body(
                    Map.of("message", "User registered successfully")
            );

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @GetMapping("/login")
    public Map<String, String> login(
            Authentication authentication) {

        return Map.of(
                "message", "Login successful",
                "username", authentication.getName()
        );
    }
}