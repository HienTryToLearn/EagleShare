package com.example.eagleshare.controller;

import com.example.eagleshare.model.User;
import com.example.eagleshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Allows your HTML file to talk to this controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // --- REGISTRATION: Creates a new account ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        String email = user.getEmail().toLowerCase();

        // Rule 1: Must be a GSU email
        if (!email.endsWith("@georgiasouthern.edu")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Only Georgia Southern emails are allowed.");
        }

        // Rule 2: Email must be unique
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: An account with this email already exists.");
        }

        // Save to MySQL
        user.setEmail(email);
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    // --- LOGIN: Checks existing accounts ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginDetails) {
        String email = loginDetails.getEmail().toLowerCase();

        // 1. Search MySQL for this email
        Optional<User> foundUser = userRepository.findByEmail(email);

        if (foundUser.isPresent()) {
            User dbUser = foundUser.get();

            // 2. Check if the typed password matches the database password
            if (dbUser.getPassword().equals(loginDetails.getPassword())) {
                return ResponseEntity.ok(dbUser); // Let them in!
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Error: Incorrect password.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: No account found with that email.");
        }
    }
}