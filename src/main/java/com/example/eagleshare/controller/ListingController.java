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
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
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
        listing.setVersion(0);

        if (image != null && !image.isEmpty()) {
            String base64Image = java.util.Base64.getEncoder()
                    .encodeToString(image.getBytes());
            listing.setImageBase64(base64Image);
        }

        return listingRepository.save(listing);
    }

    @PostMapping("/{id}/claim")
    public Listing claimListing(@PathVariable Long id, @RequestBody String claimerEmail) {
        // Clean up the email string (remove quotes if sent as raw text from JS)
        String cleanEmail = claimerEmail.replace("\"", "").trim().toLowerCase();

        // Validation: Only GSU emails can claim
        if (!cleanEmail.endsWith("@georgiasouthern.edu")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only GSU students can claim food!");
        }

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        if ("AVAILABLE".equals(listing.getStatus())) {
            listing.setStatus("CLAIMED");
            listing.setClaimedBy(cleanEmail);
            listing.setClaimTime(LocalDateTime.now());
            return listingRepository.save(listing);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This item was already claimed!");
        }
    }

    // ... inside your ListingController class, just add this:

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

        // Log the deletion (you can later upgrade this to save to a database table)
        System.out.println("Post " + id + " deleted by " + userEmail + ". Reason: " + reason);

        listingRepository.delete(listing);
        return ResponseEntity.ok().build();
    }


}