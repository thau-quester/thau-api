package com.mgrin.thau.permissions;

import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.mgrin.thau.configurations.ThauConfigurations;
import com.mgrin.thau.users.User;
import com.mgrin.thau.users.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InitialPermissionsLoader {

    private final String THAU_PERMISSIONS_ADMIN = "THAU_PERMISSIONS_ADMIN";
    private final String THAU_PERMISSIONS_READ = "THAU_PERMISSIONS_READ";

    @Autowired
    private ThauConfigurations configurations;

    @Autowired
    private RoleRepository roles;

    @Autowired
    private UserService users;

    @Autowired
    private PermissionService permissions;

    @PostConstruct
    public void loadInitialPermissions() {
        if (configurations.getSuperAdminEmail() == null) {
            return;
        }

        Optional<Role> opPermissionsAdminRole = roles.findByName(THAU_PERMISSIONS_ADMIN);
        Role permissionsAdminRole;

        if (!opPermissionsAdminRole.isPresent()) {
            permissionsAdminRole = new Role();
            permissionsAdminRole.setName(THAU_PERMISSIONS_ADMIN);
            permissionsAdminRole.setDescription(
                    "Automatically created role. Gives a User the full control over Thau permissions settings");
            permissionsAdminRole = roles.save(permissionsAdminRole);
        } else {
            permissionsAdminRole = opPermissionsAdminRole.get();
        }

        Optional<Role> opPermissionsReadRole = roles.findByName(THAU_PERMISSIONS_READ);
        Role permissionsReadRole;
        if (!opPermissionsReadRole.isPresent()) {
            permissionsReadRole = new Role();
            permissionsReadRole.setName(THAU_PERMISSIONS_READ);
            permissionsReadRole
                    .setDescription("Automaticcally created role. Gives a User the read rights in the list of roles.");
            permissionsReadRole = roles.save(permissionsReadRole);
        } else {
            permissionsReadRole = opPermissionsReadRole.get();
        }

        Optional<User> opSuperAdminUser = users.getByEmail(configurations.getSuperAdminEmail());
        if (opSuperAdminUser.isPresent()) {
            User superAdminUser = opSuperAdminUser.get();
            if (!permissions.getUserRoles(superAdminUser).contains(permissionsAdminRole.getName())) {
                Set<User> currentRoleUsers = permissionsAdminRole.getUsers();
                currentRoleUsers.add(superAdminUser);
                permissionsAdminRole.setUsers(currentRoleUsers);
                roles.save(permissionsAdminRole);
            }
        }
    }
}
