package com.example.eagleshare.controller;

import com.example.eagleshare.model.Listing;
import com.example.eagleshare.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*")
public class ListingController {

    @Autowired
    private ListingRepository listingRepository;

    @GetMapping
    public List<Listing> getAvailableListings() {
        return listingRepository.findByStatus("AVAILABLE"); //show listing that is "Available" only
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public Listing createListing(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String location,
            @RequestParam String posterEmail,
            @RequestParam(required = false) MultipartFile image
    ) throws Exception {

        if (posterEmail == null || !posterEmail.toLowerCase().endsWith("@georgiasouthern.edu")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only GSU emails can post listings!");
        }

        Listing listing = new Listing();
        listing.setTitle(title);
        listing.setDescription(description);
        listing.setCategory(category);
        listing.setLocation(location);
        listing.setPosterEmail(posterEmail);
        listing.setStatus("AVAILABLE");

        if (image != null && !image.isEmpty()) {
            String base64Image = java.util.Base64.getEncoder()
                    .encodeToString(image.getBytes());
            listing.setImageBase64(base64Image);
        }

        return listingRepository.save(listing);
    }

    @PostMapping("/{id}/claim")
    public Listing claimListing(@PathVariable Long id,
                                @RequestBody Map<String, String> request) {

        String cleanEmail = request.get("email").toLowerCase().trim();

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Listing not found"));


        if (!"AVAILABLE".equals(listing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This listing is currently locked by another student.");
        }

        // LOCK IT
        listing.setStatus("PENDING");
        listing.setClaimedBy(cleanEmail);
        listing.setClaimTime(LocalDateTime.now());

        return listingRepository.save(listing);
    }



    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteListing(@PathVariable Long id,
                                           @RequestBody Map<String, String> request) {
        String userEmail = request.get("userEmail");
        String reason = request.get("reason");

        // Fetch the listing, throw 404 if it doesn't exist
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        // Security: Ensure the person deleting it is the one who posted it
        if (!listing.getPosterEmail().equalsIgnoreCase(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }

        // Log the deletion (can later upgrade this to save to a database table)
        System.out.println("Post " + id + " deleted by " + userEmail + ". Reason: " + reason);

        listingRepository.delete(listing);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unclaim")
    public Listing unclaimListing(@PathVariable Long id, @RequestBody Map<String, String> request) {

        String userEmail = request.get("email").toLowerCase().trim();

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        // THE SECURITY GATE
        if (!userEmail.equalsIgnoreCase(listing.getClaimedBy()) &&
                !userEmail.equalsIgnoreCase(listing.getPosterEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action.");
        }

        // THE ACTION
        if ("PENDING".equals(listing.getStatus())) {
            listing.setStatus("AVAILABLE");
            listing.setClaimedBy(null);
            listing.setClaimTime(null);
            return listingRepository.save(listing);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot unclaim this listing.");
    }

    @PostMapping("/{id}/confirm")
    public Listing confirmClaim(@PathVariable Long id,
                                @RequestBody Map<String, String> request) {

        String userEmail = request.get("email").toLowerCase().trim();

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Listing not found"));

        // Only the poster can confirm
        if (!listing.getPosterEmail().equalsIgnoreCase(userEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the poster can confirm pickup.");
        }

        if ("PENDING".equals(listing.getStatus())) {

            listing.setStatus("CLAIMED");
            return listingRepository.save(listing);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Listing is not pending.");
    }

}
