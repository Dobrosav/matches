package com.dobrosav.matches.db.entities;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;


@Table(name = "users")
@Entity
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "surname")
    private String surname;
    @Column(name = "mail")
    private String mail;
    @Column(name = "password")
    @JsonIgnore
    private String password;
    @Column(name = "premium")
    private Boolean premium;

    @Column(name = "admin")
    private Boolean admin;
    @Column(name = "sex")
    private String sex;
    @Column(name = "username")
    private String username;
    @Column(name = "dateoffbrtih")
    private Date dateOfBirth;
    @Column(name = "disabilty")
    private String disability;

    @Column(name = "location")
    private String location;

    @Column(name = "bio", length = 1024)
    private String bio;

    @Column(name = "interests")
    private String interests;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserPreferences preferences;

    public User() {
    }

    public User(String name, String surname, String mail, String password, Boolean premium, Boolean admin, String sex, String username, Date dateOfBirth, String disability) {
        this.name = name;
        this.surname = surname;
        this.mail = mail;
        this.password = password;
        this.premium = premium;
        this.admin = admin;
        this.sex = sex;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.disability = disability;
        this.location = "";
        this.bio = "";
        this.interests = "";
    }

    public static User createDefaultUser(String name, String surname, String mail, String username, String password, String sex, Date dateOfBirth, String disability) {
        return new User(name, surname, mail, password, false, false, sex, username, dateOfBirth, disability);
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

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", mail='" + mail + '\'' +
                ", password='" + password + '\'' +
                ", premium=" + premium +
                ", admin=" + admin +
                ", sex=" + sex +
                ", username='" + username + '\'' +
                '}';
    }
}