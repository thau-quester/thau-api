package com.mgrin.thau.users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.mgrin.thau.sessions.externalServices.GitHubService;
import com.mgrin.thau.sessions.externalServices.LinkedInService;
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

    @Column(length = 2048)
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
            user.setDateOfBirth(LocalDate.ofInstant(fbBirthday.toInstant(), ZoneId.systemDefault()));
        }

        return user;
    }

    public static User of(GoogleIdToken.Payload googleUser) {
        User user = new User();
        user.setEmail(googleUser.getEmail());
        user.setUsername((String) googleUser.get("name"));
        user.setFirstName((String) googleUser.get("given_name"));
        user.setLastName((String) googleUser.get("family_name"));
        user.setGender((String) googleUser.get("gender"));
        user.setPicture((String) googleUser.get("picture"));
        return user;
    }

    public static User of(GitHubService.GitHubUser githubUser) {
        User user = new User();
        user.setEmail(githubUser.getEmail());
        user.setUsername(githubUser.getName());
        user.setPicture(githubUser.getAvatarUrl());
        return user;
    }

    public static User of(LinkedInService.LinkedInUser linkedinUser) {
        User user = new User();
        user.setEmail(linkedinUser.getEmail());
        user.setUsername(linkedinUser.getLocalizedFirstName() + "." + linkedinUser.getLocalizedLastName());
        user.setPicture(linkedinUser.getProfilePicture());
        return user;
    }

    public static User of(twitter4j.User twitterUser) {
        User user = new User();
        String email = twitterUser.getEmail();

        if (email == null) {
            email = twitterUser.getScreenName() + "@twitter.thau";
        }
        user.setEmail(email);
        user.setUsername(twitterUser.getScreenName());
        user.setPicture(twitterUser.getOriginalProfileImageURLHttps());
        return user;
    }

    public Optional<User> applyProvidersUpdate(User updatedUser) {
        boolean needSaving = false;
        if (this.getUsername() == null && updatedUser.getUsername() != null) {
            this.setUsername(updatedUser.getUsername());
            needSaving = true;
        }

        if (this.getFirstName() == null && updatedUser.getFirstName() != null) {
            this.setFirstName(updatedUser.getFirstName());
            needSaving = true;
        }

        if (this.getLastName() == null && updatedUser.getLastName() != null) {
            this.setLastName(updatedUser.getLastName());
            needSaving = true;
        }

        if (this.getDateOfBirth() == null && updatedUser.getDateOfBirth() != null) {
            this.setDateOfBirth(updatedUser.getDateOfBirth());
            needSaving = true;
        }

        if (this.getGender() == null && updatedUser.getGender() != null) {
            this.setGender(updatedUser.getGender());
            needSaving = true;
        }

        if (this.getPicture() == null && updatedUser.getPicture() != null) {
            this.setPicture(updatedUser.getPicture());
            needSaving = true;
        }

        if (needSaving) {
            return Optional.of(this);
        }

        return Optional.empty();
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