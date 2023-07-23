package com.dobrosav.matches.model.pojo;

import java.io.Serializable;

public class LoginRequest implements Serializable {
    private String mail;
    private String password;

    public LoginRequest() {
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "mail='" + mail + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
