package com.dobrosav.matches.db.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Table(name = "user_images")
@Entity
public class UserImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(name = "content", columnDefinition = "LONGBLOB")
    private byte[] content;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "profile_image")
    private Boolean profileImage;

    public UserImage() {
    }

    public UserImage(User user, byte[] content, String contentType, String fileName, Boolean profileImage) {
        this.user = user;
        this.content = content;
        this.contentType = contentType;
        this.fileName = fileName;
        this.profileImage = profileImage;
    }

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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Boolean profileImage) {
        this.profileImage = profileImage;
    }
}
