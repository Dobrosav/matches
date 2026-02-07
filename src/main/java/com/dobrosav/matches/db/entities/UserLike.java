package com.dobrosav.matches.db.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "user_likes")
@Entity
public class UserLike implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liker_id", nullable = false)
    private User liker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liked_id", nullable = false)
    private User liked;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public UserLike() {
    }

    public UserLike(User liker, User liked) {
        this.liker = liker;
        this.liked = liked;
        this.createdAt = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getLiker() {
        return liker;
    }

    public void setLiker(User liker) {
        this.liker = liker;
    }

    public User getLiked() {
        return liked;
    }

    public void setLiked(User liked) {
        this.liked = liked;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
