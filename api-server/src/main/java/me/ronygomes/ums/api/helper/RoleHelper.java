package me.ronygomes.ums.api.helper;

import me.ronygomes.ums.api.model.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Objects;

public class RoleHelper {

    private static final String ROLE_PREFIX = "ROLE_";

    public static SimpleGrantedAuthority validateAndConvert(String roleName) {
        Objects.requireNonNull(roleName);
        Role role = Role.valueOf(roleName.toUpperCase());
        return new SimpleGrantedAuthority(ROLE_PREFIX + role.name());
    }
}
