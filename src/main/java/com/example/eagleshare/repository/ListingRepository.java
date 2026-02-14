package com.example.eagleshare.repository;

import com.example.eagleshare.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    // This empty interface now has methods like .save(), .findAll(), and .delete()!
}