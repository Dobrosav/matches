package com.dobrosav.matches.api.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {
    @Size(max = 100, message = "Bio must be at most 100 characters")
    private String bio;
    
    @Size(max = 255, message = "Interests must be at most 255 characters")
    private String interests;
    
    @NotBlank(message = "Location is required")
    private String location;

    // Getters and setters
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
