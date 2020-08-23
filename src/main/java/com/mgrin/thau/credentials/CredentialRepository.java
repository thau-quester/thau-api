package com.mgrin.thau.credentials;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialRepository extends JpaRepository<Credentials, Long>{
    public Optional<Credentials> findByVerificationCode(String verificationCode);
    public Optional<Credentials> findByEmail(String email);
}