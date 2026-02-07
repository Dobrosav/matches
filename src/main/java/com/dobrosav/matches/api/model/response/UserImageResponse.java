package com.dobrosav.matches.api.model.response;

import java.io.Serializable;

public class UserImageResponse implements Serializable {
    private Integer id;
    private String fileName;
    private String contentType;
    private Boolean profileImage;

    public UserImageResponse() {
    }

    public UserImageResponse(Integer id, String fileName, String contentType, Boolean profileImage) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.profileImage = profileImage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Boolean getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Boolean profileImage) {
        this.profileImage = profileImage;
    }
}
