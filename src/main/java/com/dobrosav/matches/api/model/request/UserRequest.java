package com.dobrosav.matches.api.model.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Date;

public class UserRequest implements Serializable {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Surname is required")
    private String surname;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    
    @NotBlank(message = "Sex is required")
    private String sex;
    
    @NotNull(message = "Date of birth is required")
    private Date dateOfBirth;
    
    private String disabilities;

    public UserRequest() {
    }

    public UserRequest(String name, String surname, String email, String username, String password, String sex, Date dateOfBirth, String disabilities) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.username = username;
        this.password = password;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.disabilities = disabilities;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDisabilities() {
        return disabilities;
    }

    public void setDisabilities(String disabilities) {
        this.disabilities = disabilities;
    }

    @Override
    public String toString() {
        return "UserRequest{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", sex='" + sex + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", disabilities='" + disabilities + '\'' +
                '}';
    }
}
