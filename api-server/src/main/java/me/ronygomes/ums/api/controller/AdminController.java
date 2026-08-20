package me.ronygomes.ums.api.controller;

import jakarta.validation.Validator;
import me.ronygomes.ums.api.config.annotation.AdminAccess;
import me.ronygomes.ums.api.dto.AdminCreateInputDto;
import me.ronygomes.ums.api.dto.KeycloakUserCreateInputDto;
import me.ronygomes.ums.api.dto.KeycloakUserDto;
import me.ronygomes.ums.api.dto.KeycloakUserUpdateInputDto;
import me.ronygomes.ums.api.dto.PagedResponse;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Role;
import me.ronygomes.ums.api.service.KeycloakUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/admins")
public class AdminController {

    private static final String DATA_VALIDATION_ERROR_DETAILS_TEMPLATE = "Not a valid Admin. See 'error' field for details";

    private final KeycloakUserService keycloakUserService;
    private final Validator validator;
    private final ExceptionHelper exceptionHelper;

    public AdminController(KeycloakUserService keycloakUserService,
                           Validator validator,
                           ExceptionHelper exceptionHelper) {

        this.keycloakUserService = keycloakUserService;
        this.validator = validator;
        this.exceptionHelper = exceptionHelper;
    }

    @AdminAccess
    @GetMapping
    public PagedResponse<KeycloakUserDto> admins(Pageable pageable) {
        return PagedResponse.of(keycloakUserService.findByRole(Role.ADMIN, pageable));
    }

    @AdminAccess
    @GetMapping("/{id}")
    public KeycloakUserDto admin(@PathVariable String id) {
        return keycloakUserService.findById(id);
    }

    @AdminAccess
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AdminCreateInputDto input) {
        validate(input, "adminCreateInputDto");

        KeycloakUserCreateInputDto createInput = new KeycloakUserCreateInputDto(
                input.getUsername(), input.getEmail(), input.getFirstName(),
                input.getLastName(), input.getPassword(), List.of(Role.ADMIN));

        String id = keycloakUserService.create(createInput);
        return ResponseEntity.created(URI.create("/v1/admins/" + id)).build();
    }

    @AdminAccess
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody KeycloakUserUpdateInputDto input) {
        validate(input, "keycloakUserUpdateInputDto");
        keycloakUserService.update(id, input);
        return ResponseEntity.noContent().build();
    }

    @AdminAccess
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        keycloakUserService.disable(id);
        return ResponseEntity.noContent().build();
    }

    private void validate(Object dto, String objectName) {
        SpringValidatorAdapter beanValidator = new SpringValidatorAdapter(validator);
        BindingResult errors = new BeanPropertyBindingResult(dto, objectName);
        beanValidator.validate(dto, errors);
        exceptionHelper.throwErrorIfValidationError(errors, DATA_VALIDATION_ERROR_DETAILS_TEMPLATE);
    }
}
