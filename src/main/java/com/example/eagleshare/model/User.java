package com.example.eagleshare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // Names the table in MySQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The 'unique = true' tells MySQL to NEVER allow duplicate emails
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // --- Constructors ---
    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getters and Setters (The Grabbers and Givers) ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}