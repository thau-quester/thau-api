package com.mgrin.thau.permissions;

import java.util.List;

import javax.transaction.Transactional;

import com.mgrin.thau.users.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PermissionService {

    private RoleRepository roleRepository;

    @Autowired
    public PermissionService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<String> getUserRoles(User user) {
        return null;
    }

}
