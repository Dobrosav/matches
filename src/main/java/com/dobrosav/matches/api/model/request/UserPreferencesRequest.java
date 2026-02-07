package com.dobrosav.matches.api.model.request;

import java.io.Serializable;

public class UserPreferencesRequest implements Serializable {

    private String targetGender;
    private Integer minAge;
    private Integer maxAge;

    public UserPreferencesRequest() {
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
