package com.example.eagleshare.repository;

import com.example.eagleshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Used for Login: Finds the user by email so you can check their password
    Optional<User> findByEmail(String email);

    // 2. Used for Registration: Returns true if the email is already in the database
    boolean existsByEmail(String email);
}