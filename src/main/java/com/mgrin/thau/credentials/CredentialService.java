package com.mgrin.thau.credentials;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.users.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

@Service
@Transactional
public class CredentialService {

    private static final Random RANDOM = new SecureRandom();

    private CredentialRepository credentialRepository;

    @Autowired
    public CredentialService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    public Credentials create(User user, String password) {
        Credentials credentials = new Credentials();
        credentials.setUser(user);
        credentials.setEmail(user.getEmail());
        credentials.setStrategy(Strategy.PASSWORD);
        credentials.setSalt(generateSalt());
        credentials.setPassword(getHash(password, credentials.getSalt()));
        return credentialRepository.save(credentials);
    }

    public Optional<Credentials> getByVerificationCode(String verificationCode) {
        return credentialRepository.findByVerificationCode(verificationCode);
    }

    public void markAsVerified(Credentials credentials) {
        credentials.setVerified(true);
        credentialRepository.save(credentials);
    }

    public Optional<Credentials> getByEmailAndPassword(String email, String password) {
        Optional<Credentials> opCredentials = credentialRepository.findByEmail(email);
        if (opCredentials.isPresent()) {
            Credentials credentials = opCredentials.get();

            String hashedPassword = getHash(password, credentials.getSalt());
            if (!credentials.getPassword().equals(hashedPassword)) {
                return Optional.empty();
            }
        }

        return opCredentials;
    }

    public Optional<Credentials> getByEmail(String email) {
        return credentialRepository.findByEmail(email);
    }

    public String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return new String(Base64Utils.encode(salt));
    }

    public String getHash(String input, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.reset();
            digest.update(salt.getBytes());
            byte[] hashedBytes = digest.digest(input.getBytes());
            return new String(Base64Utils.encode(hashedBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}