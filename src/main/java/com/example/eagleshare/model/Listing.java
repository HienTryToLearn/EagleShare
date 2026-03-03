package com.example.eagleshare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category; // e.g., Perishable, Canned [cite: 34]

    private String location; // Campus Safe Zones [cite: 32]

    private String status = "AVAILABLE"; // AVAILABLE, PENDING, CLAIMED [cite: 30]

    private String posterEmail; // Private email of the student sharing food

    private String claimedBy; // GSU email of the student who claimed it [cite: 44]

    private LocalDateTime claimTime; // Timestamp for the 10-minute pickup rule [cite: 33]

    @Version
    private Integer version = 0; // Henry's logic to prevent race conditions [cite: 8, 28]

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPosterEmail() { return posterEmail; }
    public void setPosterEmail(String posterEmail) { this.posterEmail = posterEmail; }

    public String getClaimedBy() { return claimedBy; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }

    public LocalDateTime getClaimTime() { return claimTime; }
    public void setClaimTime(LocalDateTime claimTime) { this.claimTime = claimTime; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Column(columnDefinition = "LONGTEXT")
    private String imageBase64;

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}