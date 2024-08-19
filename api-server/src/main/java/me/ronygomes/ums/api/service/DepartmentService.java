package me.ronygomes.ums.api.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import me.ronygomes.ums.api.dto.DepartmentDto;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static me.ronygomes.ums.api.exception.ExceptionType.DATA_VALIDATION_FAILED;
import static me.ronygomes.ums.api.exception.ExceptionType.ENTITY_NOT_FOUND;

@Service
@Validated
public class DepartmentService {

    private static final String FIND_BY_CODE_ERROR_DETAILS_TEMPLATE = "Department with code '%s' not found";
    private static final String DATA_VALIDATION_ERROR_DETAILS_TEMPLATE = "Not a valid Department. See 'error' field for details";

    private final DepartmentRepository departmentRepository;
    private final ExceptionHelper exceptionHelper;

    public DepartmentService(DepartmentRepository departmentRepository, ExceptionHelper exceptionHelper) {
        this.departmentRepository = departmentRepository;
        this.exceptionHelper = exceptionHelper;
    }

    public List<DepartmentDto> findAll() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentDto::new)
                .toList();
    }

    public DepartmentDto findByCode(@NotNull String code) {
        return new DepartmentDto(findByCodeOrThrow(code));
    }

    @Transactional
    public void save(DepartmentDto departmentDto) {
        save(departmentDto.toDepartment());
    }

    @Transactional
    public void updateAll(String code, DepartmentDto updatedDepartment) {
        Department department = findByCodeOrThrow(code);
        department.setCode(updatedDepartment.getCode());
        department.setName(updatedDepartment.getName());
        save(department);
    }

    @Transactional
    public void updateOne(String code, DepartmentDto updatedDepartment) {
        Department department = findByCodeOrThrow(code);
        if (Objects.nonNull(updatedDepartment.getCode())) {
            department.setCode(updatedDepartment.getCode());
        }

        if (Objects.nonNull(updatedDepartment.getName())) {
            department.setName(updatedDepartment.getName());
        }
        save(department);
    }

    @Transactional
    public void delete(String code) {
        Department department = findByCodeOrThrow(code);
        departmentRepository.delete(department);
    }

    private void save(Department department) {
        try {
            departmentRepository.save(department);
            // TODO: Extract flush() from service. Write exception handler for these exception
            departmentRepository.flush();
        } catch (TransactionSystemException | DataIntegrityViolationException tse) {
            throw new UmsDataException(DATA_VALIDATION_FAILED,
                    DATA_VALIDATION_ERROR_DETAILS_TEMPLATE, exceptionHelper.extractConstraintViolation(tse));
        }
    }

    private Department findByCodeOrThrow(String code) {
        return departmentRepository.findByCode(code)
                .orElseThrow(() -> new UmsDataException(ENTITY_NOT_FOUND,
                        FIND_BY_CODE_ERROR_DETAILS_TEMPLATE.formatted(code)));
    }
}
