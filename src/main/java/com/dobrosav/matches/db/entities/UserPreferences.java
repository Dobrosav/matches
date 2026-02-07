package com.dobrosav.matches.db.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "user_preferences")
public class UserPreferences implements Serializable {
    @Id
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "target_gender")
    private String targetGender; // e.g., "M", "F", "Any"

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    public UserPreferences() {
    }

    public UserPreferences(User user) {
        this.user = user;
        this.minAge = 18;
        this.maxAge = 99;
        this.targetGender = "Any";
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTargetGender() {
        return targetGender;
    }

    public void setTargetGender(String targetGender) {
        this.targetGender = targetGender;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }
}
