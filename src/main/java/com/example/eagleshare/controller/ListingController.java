package com.example.eagleshare.controller;

import com.example.eagleshare.model.Listing;
import com.example.eagleshare.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*") // Allows your HTML file to talk to the Backend
public class ListingController {

    @Autowired
    private ListingRepository listingRepository;

    // GET ALL: This is what the dashboard calls to show the "Available Food"
    @GetMapping
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    // POST NEW: This is what saves your food item to MySQL
    @PostMapping
    public Listing createListing(@RequestBody Listing listing) {
        // Implementation logic: Set defaults for a new post
        listing.setStatus("AVAILABLE");
        listing.setVersion(0);
        // This saves the item to the database columns Eduing created
        return listingRepository.save(listing);
    }

    // CLAIM LOGIC: This handles the privacy and the 10-minute window
    @PostMapping("/{id}/claim")
    public Listing claimListing(@PathVariable Long id, @RequestBody String claimerEmail) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Phase 3 Logic: Only allow claim if it hasn't been taken yet
        if ("AVAILABLE".equals(listing.getStatus())) {
            listing.setStatus("CLAIMED");
            listing.setClaimedBy(claimerEmail);
            listing.setClaimTime(LocalDateTime.now()); // Starts the pickup clock
            return listingRepository.save(listing);
        } else {
            throw new RuntimeException("This item was already claimed by someone else!");
        }
    }
}