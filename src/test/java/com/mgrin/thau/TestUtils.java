package com.mgrin.thau;

import com.mgrin.thau.credentials.Credentials;
import com.mgrin.thau.users.User;

public class TestUtils {
    public static User createUser(String email) {
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setUsername("Test");
        user.setFirstName("First namee");
        user.setLastName("Last namee");
        user.setGender("they/them");
        user.setPicture("https://google.com");

        return user;
    }

    public static Credentials createCredentials(User user) {
        Credentials credentials = new Credentials();
        credentials.setUser(user);
        credentials.setEmail(user.getEmail());
        credentials.setId(1);
        credentials.setVerified(false);

        return credentials;
    }

    public static Credentials createCredentials(User user, boolean verified) {
        Credentials credentials = TestUtils.createCredentials(user);
        credentials.setVerified(verified);

        return credentials;
    }
}