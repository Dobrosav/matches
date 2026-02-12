package com.dobrosav.matches.api.model.response;

import com.dobrosav.matches.db.entities.User;

import java.io.Serializable;
import java.util.Date;

public class UserResponse implements Serializable {
    private Integer id;
    private String name;
    private String surname;
    private String email;
    private Boolean premium;
    private Boolean admin;
    private String sex;
    private String username;
    private Date dateOfBirth;
    private String disability;
    private String location;
    private String bio;
    private String interests;

    public UserResponse() {
    }

    public UserResponse(Integer id, String name, String surname, String email, Boolean premium, Boolean admin, String sex, String username, Date dateOfBirth, String disability, String location, String bio, String interests) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.premium = premium;
        this.admin = admin;
        this.sex = sex;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.disability = disability;
        this.location = location;
        this.bio = bio;
        this.interests = interests;
    }

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
        this.premium = user.getPremium();
        this.admin = user.getAdmin();
        this.sex = user.getSex();
        this.username = user.getUsername();
        this.dateOfBirth = user.getDateOfBirth();
        this.disability = user.getDisability();
        this.location = user.getLocation();
        this.bio = user.getBio();
        this.interests = user.getInterests();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDisability() {
        return disability;
    }

    public void setDisability(String disability) {
        this.disability = disability;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

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
}
