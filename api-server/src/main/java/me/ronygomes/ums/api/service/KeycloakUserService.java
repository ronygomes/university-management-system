package me.ronygomes.ums.api.service;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import me.ronygomes.ums.api.dto.KeycloakUserCreateInputDto;
import me.ronygomes.ums.api.dto.KeycloakUserDto;
import me.ronygomes.ums.api.dto.KeycloakUserUpdateInputDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.Role;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeycloakUserService {

    private final Keycloak keycloak;
    private final String targetRealm;
    private final String appClientId;

    public KeycloakUserService(Keycloak keycloak,
                               @Value("${ums.keycloak.target-realm}") String targetRealm,
                               @Value("${ums.keycloak.app-client-id}") String appClientId) {
        this.keycloak = keycloak;
        this.targetRealm = targetRealm;
        this.appClientId = appClientId;
    }

    public String create(KeycloakUserCreateInputDto input) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(List.of(passwordCredential(input.getPassword())));

        UsersResource users = keycloak.realm(targetRealm).users();
        try (Response response = users.create(user)) {
            if (response.getStatus() != 201) {
                throw new IllegalStateException(
                        "Keycloak user creation failed: HTTP " + response.getStatus());
            }
            String userId = CreatedResponseUtil.getCreatedId(response);
            assignClientRoles(userId, input.getRoles());
            return userId;
        }
    }

    public void update(String id, KeycloakUserUpdateInputDto input) {
        UserResource userResource = keycloak.realm(targetRealm).users().get(id);
        UserRepresentation user = userResource.toRepresentation();
        boolean profileChanged = false;
        if (input.getFirstName() != null) {
            user.setFirstName(input.getFirstName());
            profileChanged = true;
        }
        if (input.getLastName() != null) {
            user.setLastName(input.getLastName());
            profileChanged = true;
        }
        if (profileChanged) {
            userResource.update(user);
        }
        if (input.getPassword() != null) {
            userResource.resetPassword(passwordCredential(input.getPassword()));
        }
    }

    public void disable(String id) {
        UserResource userResource = keycloak.realm(targetRealm).users().get(id);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(false);
        userResource.update(user);
    }

    public List<KeycloakUserDto> findByRole(Role role) {
        String clientUuid = resolveClientUuid();
        ClientResource clientResource = keycloak.realm(targetRealm).clients().get(clientUuid);
        return clientResource.roles().get(role.name().toLowerCase()).getUserMembers().stream()
                .map(KeycloakUserDto::new)
                .toList();
    }

    public Page<KeycloakUserDto> findByRole(Role role, Pageable pageable) {
        List<KeycloakUserDto> members = findByRole(role);
        int start = (int) Math.min(pageable.getOffset(), members.size());
        int end = Math.min(start + pageable.getPageSize(), members.size());
        return new PageImpl<>(members.subList(start, end), pageable, members.size());
    }

    public KeycloakUserDto findById(String id) {
        try {
            UserRepresentation user = keycloak.realm(targetRealm).users().get(id).toRepresentation();
            return new KeycloakUserDto(user);
        } catch (NotFoundException e) {
            throw new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "User with id=" + id + " not found");
        }
    }

    private CredentialRepresentation passwordCredential(String password) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);
        return cred;
    }

    private String resolveClientUuid() {
        List<ClientRepresentation> clients = keycloak.realm(targetRealm).clients().findByClientId(appClientId);
        if (clients.isEmpty()) {
            throw new IllegalStateException("Keycloak client not found: " + appClientId);
        }
        return clients.getFirst().getId();
    }

    private void assignClientRoles(String userId, List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        String clientUuid = resolveClientUuid();
        ClientResource clientResource = keycloak.realm(targetRealm).clients().get(clientUuid);
        List<RoleRepresentation> rolesToAssign = roles.stream()
                .map(role -> clientResource.roles().get(role.name().toLowerCase()).toRepresentation())
                .toList();
        keycloak.realm(targetRealm).users().get(userId).roles().clientLevel(clientUuid).add(rolesToAssign);
    }
}
