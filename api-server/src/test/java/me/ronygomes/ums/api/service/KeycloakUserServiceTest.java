package me.ronygomes.ums.api.service;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import me.ronygomes.ums.api.dto.KeycloakUserCreateInputDto;
import me.ronygomes.ums.api.dto.KeycloakUserDto;
import me.ronygomes.ums.api.dto.KeycloakUserUpdateInputDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.URI;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class KeycloakUserServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private ClientsResource clientsResource;

    @Mock
    private ClientResource clientResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource adminRoleResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource clientLevelResource;

    private KeycloakUserService service;

    private static final String TARGET_REALM = "ums";
    private static final String APP_CLIENT_ID = "ums-client-webapp";
    private static final String APP_CLIENT_UUID = "abc-uuid-123";

    @BeforeEach
    void setup() {
        service = new KeycloakUserService(keycloak, TARGET_REALM, APP_CLIENT_ID);

        Mockito.when(keycloak.realm(TARGET_REALM)).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);
        Mockito.when(realmResource.clients()).thenReturn(clientsResource);
        Mockito.when(usersResource.get(Mockito.anyString())).thenReturn(userResource);

        ClientRepresentation clientRep = new ClientRepresentation();
        clientRep.setId(APP_CLIENT_UUID);
        clientRep.setClientId(APP_CLIENT_ID);
        Mockito.when(clientsResource.findByClientId(APP_CLIENT_ID)).thenReturn(List.of(clientRep));
        Mockito.when(clientsResource.get(APP_CLIENT_UUID)).thenReturn(clientResource);
        Mockito.when(clientResource.roles()).thenReturn(rolesResource);
        Mockito.when(rolesResource.get(Mockito.anyString())).thenReturn(adminRoleResource);

        RoleRepresentation adminRoleRep = new RoleRepresentation();
        adminRoleRep.setName("admin");
        Mockito.when(adminRoleResource.toRepresentation()).thenReturn(adminRoleRep);

        Mockito.when(userResource.roles()).thenReturn(roleMappingResource);
        Mockito.when(roleMappingResource.clientLevel(APP_CLIENT_UUID)).thenReturn(clientLevelResource);
    }

    private Response createdResponse(String userId) {
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getStatus()).thenReturn(201);
        Mockito.when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
        Mockito.when(response.getLocation()).thenReturn(URI.create("http://keycloak/admin/realms/ums/users/" + userId));
        return response;
    }

    @Test
    void testCreateSendsCorrectUserRepresentationAndReturnsId() {
        Response resp = createdResponse("user-42");
        ArgumentCaptor<UserRepresentation> ac = ArgumentCaptor.forClass(UserRepresentation.class);
        Mockito.when(usersResource.create(ac.capture())).thenReturn(resp);

        KeycloakUserCreateInputDto input = new KeycloakUserCreateInputDto(
                "jane.doe", "jane@ums.dev", "Jane", "Doe", "secret123", List.of(Role.STUDENT));

        String id = service.create(input);

        Assertions.assertEquals("user-42", id);
        UserRepresentation user = ac.getValue();
        Assertions.assertEquals("jane.doe", user.getUsername());
        Assertions.assertEquals("jane@ums.dev", user.getEmail());
        Assertions.assertEquals("Jane", user.getFirstName());
        Assertions.assertEquals("Doe", user.getLastName());
        Assertions.assertTrue(user.isEnabled());
        Assertions.assertTrue(user.isEmailVerified());
        Assertions.assertEquals(1, user.getCredentials().size());
        CredentialRepresentation cred = user.getCredentials().get(0);
        Assertions.assertEquals(CredentialRepresentation.PASSWORD, cred.getType());
        Assertions.assertEquals("secret123", cred.getValue());
        Assertions.assertFalse(cred.isTemporary());
    }

    @Test
    void testCreateAssignsRolesLowercasedToTheAppClient() {
        Response resp = createdResponse("user-42");
        Mockito.when(usersResource.create(Mockito.any())).thenReturn(resp);

        service.create(new KeycloakUserCreateInputDto(
                "u", "u@x.com", "U", "X", "secret123",
                List.of(Role.ADMIN, Role.TEACHER)));

        // role names are looked up as lowercase
        Mockito.verify(rolesResource).get("admin");
        Mockito.verify(rolesResource).get("teacher");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleRepresentation>> ac = ArgumentCaptor.forClass(List.class);
        Mockito.verify(clientLevelResource).add(ac.capture());
        Assertions.assertEquals(2, ac.getValue().size());
    }

    @Test
    void testCreateThrowsWhenKeycloakReturnsNon201() {
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getStatus()).thenReturn(409);
        Mockito.when(usersResource.create(Mockito.any())).thenReturn(response);

        KeycloakUserCreateInputDto input = new KeycloakUserCreateInputDto(
                "u", "u@x.com", "U", "X", "secret123", List.of(Role.STUDENT));

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> service.create(input));
        Assertions.assertTrue(ex.getMessage().contains("409"));
    }

    @Test
    void testUpdateAppliesOnlyProvidedFieldsAndPasswordIsReset() {
        UserRepresentation existing = new UserRepresentation();
        existing.setUsername("jane.doe");
        existing.setEmail("jane@ums.dev");
        existing.setFirstName("Old");
        existing.setLastName("Name");
        Mockito.when(userResource.toRepresentation()).thenReturn(existing);

        KeycloakUserUpdateInputDto input = new KeycloakUserUpdateInputDto("New", null, "newpass1");
        service.update("user-42", input);

        ArgumentCaptor<UserRepresentation> ac = ArgumentCaptor.forClass(UserRepresentation.class);
        Mockito.verify(userResource).update(ac.capture());
        UserRepresentation sent = ac.getValue();
        Assertions.assertEquals("New", sent.getFirstName());
        Assertions.assertEquals("Name", sent.getLastName()); // unchanged
        // username/email never touched
        Assertions.assertEquals("jane.doe", sent.getUsername());
        Assertions.assertEquals("jane@ums.dev", sent.getEmail());

        ArgumentCaptor<CredentialRepresentation> credAc = ArgumentCaptor.forClass(CredentialRepresentation.class);
        Mockito.verify(userResource).resetPassword(credAc.capture());
        Assertions.assertEquals("newpass1", credAc.getValue().getValue());
        Assertions.assertFalse(credAc.getValue().isTemporary());
    }

    @Test
    void testUpdateWithOnlyPasswordSkipsUserUpdate() {
        Mockito.when(userResource.toRepresentation()).thenReturn(new UserRepresentation());

        service.update("user-42", new KeycloakUserUpdateInputDto(null, null, "newpass1"));

        Mockito.verify(userResource, Mockito.never()).update(Mockito.any());
        Mockito.verify(userResource, Mockito.times(1)).resetPassword(Mockito.any());
    }

    @Test
    void testUpdateWithOnlyNameSkipsPasswordReset() {
        UserRepresentation existing = new UserRepresentation();
        Mockito.when(userResource.toRepresentation()).thenReturn(existing);

        service.update("user-42", new KeycloakUserUpdateInputDto("Anne", "Smith", null));

        Mockito.verify(userResource, Mockito.times(1)).update(Mockito.any());
        Mockito.verify(userResource, Mockito.never()).resetPassword(Mockito.any());
    }

    @Test
    void testDisableSetsEnabledFalseAndUpdates() {
        UserRepresentation existing = new UserRepresentation();
        existing.setEnabled(true);
        Mockito.when(userResource.toRepresentation()).thenReturn(existing);

        service.disable("user-42");

        ArgumentCaptor<UserRepresentation> ac = ArgumentCaptor.forClass(UserRepresentation.class);
        Mockito.verify(userResource).update(ac.capture());
        Assertions.assertFalse(ac.getValue().isEnabled());
    }

    @Test
    void testFindByRoleReturnsUsersWithThatClientRoleMapped() {
        UserRepresentation u1 = new UserRepresentation();
        u1.setId("id-1");
        u1.setUsername("admin1");
        u1.setEmail("a1@ums.dev");
        u1.setFirstName("A");
        u1.setLastName("One");
        u1.setEnabled(true);

        UserRepresentation u2 = new UserRepresentation();
        u2.setId("id-2");
        u2.setUsername("admin2");
        u2.setEnabled(false);

        Mockito.when(adminRoleResource.getUserMembers()).thenReturn(List.of(u1, u2));

        List<KeycloakUserDto> admins = service.findByRole(Role.ADMIN);

        // role name is looked up lowercased against the app client
        Mockito.verify(rolesResource).get("admin");
        Assertions.assertEquals(2, admins.size());
        Assertions.assertEquals("id-1", admins.get(0).getId());
        Assertions.assertEquals("admin1", admins.get(0).getUsername());
        Assertions.assertEquals("a1@ums.dev", admins.get(0).getEmail());
        Assertions.assertEquals("A", admins.get(0).getFirstName());
        Assertions.assertTrue(admins.get(0).isEnabled());
        Assertions.assertFalse(admins.get(1).isEnabled());
    }

    @Test
    void testFindByIdReturnsMappedUser() {
        UserRepresentation u = new UserRepresentation();
        u.setId("user-42");
        u.setUsername("jane.doe");
        u.setEmail("jane@ums.dev");
        u.setFirstName("Jane");
        u.setLastName("Doe");
        u.setEnabled(true);
        Mockito.when(userResource.toRepresentation()).thenReturn(u);

        KeycloakUserDto dto = service.findById("user-42");

        Assertions.assertEquals("user-42", dto.getId());
        Assertions.assertEquals("jane.doe", dto.getUsername());
        Assertions.assertEquals("jane@ums.dev", dto.getEmail());
        Assertions.assertEquals("Jane", dto.getFirstName());
        Assertions.assertEquals("Doe", dto.getLastName());
        Assertions.assertTrue(dto.isEnabled());
    }

    @Test
    void testFindByIdThrowsEntityNotFoundWhenUserMissing() {
        Mockito.when(userResource.toRepresentation()).thenThrow(new NotFoundException());

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> service.findById("missing"));
        Assertions.assertEquals(ExceptionType.ENTITY_NOT_FOUND, ex.getExceptionType());
    }
}
