package me.ronygomes.ums.api.testHelper;

import me.ronygomes.ums.api.model.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.List;

public class RoleHelper {

    private static final String ROLE_PREFIX = "ROLE_";

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {
        return jwtWithRole(ROLE_PREFIX + Role.ADMIN.name());
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor studentJwt() {
        return jwtWithRole(ROLE_PREFIX + Role.STUDENT.name());
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor teacherJwt() {
        return jwtWithRole(ROLE_PREFIX + Role.TEACHER.name());
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(List.of(new SimpleGrantedAuthority(role)));
    }
}
