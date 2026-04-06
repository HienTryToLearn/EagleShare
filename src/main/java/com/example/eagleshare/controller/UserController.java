package com.example.eagleshare.controller;

import com.example.eagleshare.model.User;
import com.example.eagleshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Allows your HTML file to talk to this controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // 1. Add the "Grinder"

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        String email = user.getEmail().toLowerCase().trim();

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

        // 2. Hash the password BEFORE saving
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        // -------------------------------

        user.setEmail(email);
        userRepository.save(user);

        // 3. Professional Tip: Don't send the hashed password back to the frontend
        user.setPassword(null);

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

            // 🛡️ THE SECURITY UPGRADE: Use .matches() instead of .equals()
            if (passwordEncoder.matches(loginDetails.getPassword(), dbUser.getPassword())) {

                // Safety tip: Wipe the password before sending the user object back to the browser
                dbUser.setPassword(null);

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
