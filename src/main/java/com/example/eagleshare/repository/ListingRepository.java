
package com.example.eagleshare.repository;

import com.example.eagleshare.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    // Finds all food items posted by a specific student
    List<Listing> findByPosterEmail(String email);

    // Finds all food items currently available
    List<Listing> findByStatus(String status);
}
