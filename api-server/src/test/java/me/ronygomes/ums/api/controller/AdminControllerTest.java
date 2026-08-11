package me.ronygomes.ums.api.controller;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.dto.KeycloakUserCreateInputDto;
import me.ronygomes.ums.api.dto.KeycloakUserDto;
import me.ronygomes.ums.api.dto.KeycloakUserUpdateInputDto;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Role;
import me.ronygomes.ums.api.service.KeycloakUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static me.ronygomes.ums.api.testHelper.RoleHelper.adminJwt;
import static me.ronygomes.ums.api.testHelper.RoleHelper.studentJwt;
import static me.ronygomes.ums.api.testHelper.RoleHelper.teacherJwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("controller-test")
public class AdminControllerTest {

    @MockitoBean
    private KeycloakUserService keycloakUserService;

    @MockitoBean
    private ExceptionHelper exceptionHelper;

    @MockitoBean
    private Validator validator;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private static KeycloakUserDto admin(String id, String username, String email, boolean enabled) {
        KeycloakUserDto dto = new KeycloakUserDto();
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setFirstName("First");
        dto.setLastName("Last");
        dto.setEnabled(enabled);
        return dto;
    }

    @Test
    void testListAdmins() throws Exception {
        Mockito.when(keycloakUserService.findByRole(Role.ADMIN)).thenReturn(List.of(
                admin("id-1", "admin1", "a1@ums.dev", true),
                admin("id-2", "admin2", "a2@ums.dev", false)));

        mockMvc.perform(get("/v1/admins").with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].username").value("admin1"))
                .andExpect(jsonPath("$[0].email").value("a1@ums.dev"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[1].id").value("id-2"))
                .andExpect(jsonPath("$[1].enabled").value(false));

        mockMvc.perform(get("/v1/admins").with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
        mockMvc.perform(get("/v1/admins").with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testGetAdminById() throws Exception {
        Mockito.when(keycloakUserService.findById("id-1")).thenReturn(admin("id-1", "admin1", "a1@ums.dev", true));

        mockMvc.perform(get("/v1/admins/id-1").with(adminJwt()))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.username").value("admin1"));

        mockMvc.perform(get("/v1/admins/id-1").with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testGetAdminByIdNotFound() throws Exception {
        Mockito.when(keycloakUserService.findById("missing"))
                .thenThrow(new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "User with id=missing not found"));

        mockMvc.perform(get("/v1/admins/missing").with(adminJwt()))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/entity-not-found"))
                .andExpect(jsonPath("$.detail").value("User with id=missing not found"));
    }

    @Test
    void testCreateAdminForcesAdminRoleAndReturns201() throws Exception {
        ArgumentCaptor<KeycloakUserCreateInputDto> ac = ArgumentCaptor.forClass(KeycloakUserCreateInputDto.class);
        Mockito.when(keycloakUserService.create(ac.capture())).thenReturn("new-id");

        String body = """
                {
                  "username": "newadmin",
                  "email": "newadmin@ums.dev",
                  "firstName": "New",
                  "lastName": "Admin",
                  "password": "secret123"
                }
                """;

        mockMvc.perform(post("/v1/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(adminJwt()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(header().string("Location", "/v1/admins/new-id"));

        KeycloakUserCreateInputDto sent = ac.getValue();
        Assertions.assertEquals("newadmin", sent.getUsername());
        Assertions.assertEquals("newadmin@ums.dev", sent.getEmail());
        Assertions.assertEquals("New", sent.getFirstName());
        Assertions.assertEquals("Admin", sent.getLastName());
        Assertions.assertEquals("secret123", sent.getPassword());
        Assertions.assertEquals(List.of(Role.ADMIN), sent.getRoles());

        mockMvc.perform(post("/v1/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testCreateAdminValidationFailure() throws Exception {
        Mockito.doThrow(new UmsDataException(ExceptionType.DATA_VALIDATION_FAILED, "abc"))
                .when(exceptionHelper).throwErrorIfValidationError(Mockito.any(), Mockito.anyString());

        mockMvc.perform(post("/v1/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(adminJwt()))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.type").value("https://documentation.com/errors/data-validation-failed"));

        Mockito.verify(keycloakUserService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void testUpdateAdmin() throws Exception {
        String body = """
                { "firstName": "Renamed", "password": "newsecret1" }
                """;

        mockMvc.perform(put("/v1/admins/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(adminJwt()))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        ArgumentCaptor<KeycloakUserUpdateInputDto> ac = ArgumentCaptor.forClass(KeycloakUserUpdateInputDto.class);
        Mockito.verify(keycloakUserService).update(Mockito.eq("id-1"), ac.capture());
        Assertions.assertEquals("Renamed", ac.getValue().getFirstName());

        mockMvc.perform(put("/v1/admins/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(studentJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void testDeleteAdminDisables() throws Exception {
        mockMvc.perform(delete("/v1/admins/id-1").with(adminJwt()))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        Mockito.verify(keycloakUserService, Mockito.times(1)).disable("id-1");

        mockMvc.perform(delete("/v1/admins/id-1").with(teacherJwt()))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }
}
