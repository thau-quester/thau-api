package com.mgrin.thau.providers;

import java.util.Collection;
import java.util.Optional;

import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.users.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    public Optional<Provider> findByUserAndProvider(User user, Strategy provider);

    @Query("SELECT p FROM Provider p WHERE p.user.id = ?1")
    public Collection<Provider> findProvidersForUserId(Long userId);
}