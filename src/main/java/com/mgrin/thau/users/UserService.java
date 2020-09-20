package com.mgrin.thau.users;

import java.util.Map;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.credentials.CredentialService;
import com.mgrin.thau.providers.Provider;
import com.mgrin.thau.providers.ProviderService;
import com.mgrin.thau.utils.HashMapConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private UserRepository users;

    private CredentialService credentials;

    private ProviderService providers;

    @Autowired
    public UserService(UserRepository users, CredentialService credentials, ProviderService providers) {
        this.users = users;
        this.credentials = credentials;
        this.providers = providers;
    }

    public User create(User user, String password) {
        User savedUser = users.save(user);
        if (password != null) {
            credentials.create(savedUser, password);
            providers.create(savedUser, Strategy.PASSWORD);
        }
        return savedUser;
    }

    public User update(User user) {
        return users.save(user);
    }

    public User create(User user, com.restfb.types.User data) {
        User savedUser = users.save(user);
        Map<String, Object> map = HashMapConverter.convertObjectToMap(data);
        providers.create(savedUser, Strategy.FACEBOOK, map);
        return savedUser;
    }

    public User create(User user, GoogleIdToken.Payload payload) {
        User savedUser = users.save(user);
        Map<String, Object> map = HashMapConverter.convertObjectToMap(payload);
        providers.create(savedUser, Strategy.GOOGLE, map);
        return savedUser;
    }

    public void updateProvidersData(User user, com.restfb.types.User data) {
        Optional<Provider> opProvider = providers.getByUserAndStrategy(user, Strategy.FACEBOOK);
        Map<String, Object> map = HashMapConverter.convertObjectToMap(data);
        Provider provider;
        if (!opProvider.isPresent()) {
            provider = providers.create(user, Strategy.FACEBOOK, map);
        } else {
            provider = opProvider.get();
            provider.setData(map);
            providers.update(provider);
        }
    }

    public void updateProvidersData(User user, GoogleIdToken.Payload payload) {
        Optional<Provider> opProvider = providers.getByUserAndStrategy(user, Strategy.FACEBOOK);
        Map<String, Object> map = HashMapConverter.convertObjectToMap(payload);
        Provider provider;
        if (!opProvider.isPresent()) {
            provider = providers.create(user, Strategy.GOOGLE, map);
        } else {
            provider = opProvider.get();
            provider.setData(map);
            providers.update(provider);
        }
    }

    public Optional<User> getById(long id) {
        return users.findById(id);
    }

    public Optional<User> getByEmail(String email) {
        return users.findByEmail(email);
    }

}