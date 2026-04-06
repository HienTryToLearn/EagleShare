package com.example.eagleshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // Don't forget this!
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // And this!

@SpringBootApplication
public class EagleshareApplication {

	public static void main(String[] args) {
		SpringApplication.run(EagleshareApplication.class, args);
	}
}
