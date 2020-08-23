package com.mgrin.thau.providers;

import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import com.mgrin.thau.configurations.strategies.Strategy;

import com.mgrin.thau.users.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProviderService {

    private ProviderRepository providerRepository;

    @Autowired
    public ProviderService(ProviderRepository providers) {
        this.providerRepository = providers;
    }

    public Provider create(User user, Strategy strategy) {
        Provider provider = new Provider();
        provider.setProvider(strategy);
        provider.setUser(user);

        return providerRepository.save(provider);
    }

    public Provider create(User user, Strategy strategy, Map<String, Object> data) {
        Provider provider = new Provider();
        provider.setProvider(strategy);
        provider.setUser(user);
        provider.setData(data);

        return providerRepository.save(provider);
    }

    public Optional<Provider> getByUserAndStrategy(User user, Strategy strategy) {
        return providerRepository.findByUserAndProvider(user, strategy);
    }

    public Provider update(Provider provider) {
        return providerRepository.save(provider);
    }
}