package com.dobrosav.matches.db.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "user_dislikes")
public class UserDislike implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disliker_id", nullable = false)
    private User disliker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disliked_id", nullable = false)
    private User disliked;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public UserDislike() {
    }

    public UserDislike(User disliker, User disliked) {
        this.disliker = disliker;
        this.disliked = disliked;
        this.createdAt = new Date();
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getDisliker() {
        return disliker;
    }

    public void setDisliker(User disliker) {
        this.disliker = disliker;
    }

    public User getDisliked() {
        return disliked;
    }

    public void setDisliked(User disliked) {
        this.disliked = disliked;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
