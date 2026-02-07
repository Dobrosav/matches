package com.dobrosav.matches.api.model.response;

import com.dobrosav.matches.db.entities.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginWrapper implements Serializable {
    private UserResponse user;
    private SuccessResult result;

    public LoginWrapper() {
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }


    public SuccessResult getResult() {
        return result;
    }

    public void setResult(SuccessResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "LoginWrapper{" +
                "user=" + user +
                ", result=" + result +
                '}';
    }
}
