package com.mgrin.thau.users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.restfb.types.ProfilePictureSource;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.springframework.lang.NonNull;

@Entity
@Table(name = "ThauUsers")
@Audited
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(unique = true)
    @NonNull
    @Email(message = "Please provide valid email")
    private String email;

    @Column
    @Length(min = 3, message = "Username should be longer than 3 symbols")
    private String username;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Column
    private String gender;

    @Column(columnDefinition = "TEXT")
    @URL
    private String picture;

    @CreationTimestamp
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonIgnore
    public LocalDateTime updatedAt;

    public String toString() {
        return "User[id: " + id + ", email: " + email + ", username: " + username + ", firstName: " + firstName
                + ", lastName: " + lastName + ", dateOfBirth: " + dateOfBirth + ", gender: " + gender + ", picture: "
                + picture + "]";
    }

    public static User of(com.restfb.types.User facebookUser) {
        User user = new User();
        user.setEmail(facebookUser.getEmail());
        user.setUsername(facebookUser.getFirstName() + "." + facebookUser.getLastName());
        user.setFirstName(facebookUser.getFirstName());
        user.setLastName(facebookUser.getLastName());
        user.setGender(facebookUser.getGender());

        ProfilePictureSource picture = facebookUser.getPicture();
        if (picture != null) {
            user.setPicture(picture.getUrl());
        }

        Date fbBirthday = facebookUser.getBirthdayAsDate();
        if (fbBirthday != null) {
            user.setDateOfBirth(LocalDate.ofEpochDay(fbBirthday.getTime()));
        }

        return user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}