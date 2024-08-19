package me.ronygomes.ums.api.service;

import me.ronygomes.ums.api.dto.DepartmentDto;
import me.ronygomes.ums.api.exception.ErrorMessage;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.helper.DataHelper;
import me.ronygomes.ums.api.helper.ExceptionHelper;
import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.repository.DepartmentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionSystemException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ExceptionHelper exceptionHelper;

    private DepartmentService service;

    @BeforeEach
    void setup() {
        service = new DepartmentService(departmentRepository, exceptionHelper);
    }

    @Test
    void testFindAll() {
        List<Department> departments = createCockDepartmentList();
        Mockito.when(departmentRepository.findAll()).thenReturn(departments);
        List<DepartmentDto> departmentDto = service.findAll();

        Assertions.assertEquals(departments.size(), departmentDto.size());
        for (int i = 0; i < departments.size(); i++) {
            assertDepartmentDataEqual(departments.get(i), departmentDto.get(i));
        }
    }

    @Test
    void testFindByCodeReturnsValue() {
        Department d1 = DataHelper.validPersistableDepartment1();
        d1.setId(1L);

        Mockito.when(departmentRepository.findByCode("CODE-1")).thenReturn(Optional.of(d1));
        DepartmentDto departmentDto = service.findByCode("CODE-1");

        assertDepartmentDataEqual(d1, departmentDto);
    }

    @Test
    void testFindByCodeThrowsException() {

        Mockito.when(departmentRepository.findByCode("CODE-X")).thenReturn(Optional.empty());
        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> service.findByCode("CODE-X"));

        Assertions.assertEquals(ExceptionType.ENTITY_NOT_FOUND, ex.getExceptionType());
        Assertions.assertEquals("Department with code 'CODE-X' not found", ex.getErrorDetails());
        Assertions.assertEquals(0, ex.getErrors().size());
    }

    @Test
    void testSaveValid() {

        DepartmentDto dto = new DepartmentDto();
        dto.setCode("GREET");
        dto.setName("Hello World");

        ArgumentCaptor<Department> deptParam = ArgumentCaptor.forClass(Department.class);
        Mockito.when(departmentRepository.save(deptParam.capture())).thenReturn(null);

        service.save(dto);

        assertDepartmentDataEqual(deptParam.getValue(), dto);
        Mockito.verify(departmentRepository, Mockito.times(1)).flush();
    }

    @Test
    void testSaveInValid() {

        DepartmentDto dto = new DepartmentDto();
        dto.setCode("GREET");
        dto.setName("Hello World");

        TransactionSystemException tex = new TransactionSystemException("Dummy");
        Mockito.doThrow(tex).when(departmentRepository).flush();

        List<ErrorMessage> errors = new ArrayList<>();
        Mockito.when(exceptionHelper.extractConstraintViolation(tex)).thenReturn(errors);

        UmsDataException umsEx = Assertions.assertThrows(UmsDataException.class, () -> service.save(dto));

        Mockito.verify(departmentRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(departmentRepository, Mockito.times(1)).flush();

        Assertions.assertEquals(ExceptionType.DATA_VALIDATION_FAILED, umsEx.getExceptionType());
        Assertions.assertEquals("Not a valid Department. See 'error' field for details", umsEx.getErrorDetails());
        Assertions.assertSame(errors, umsEx.getErrors());
    }

    @Test
    void testUpdateValid() {

        Department d1 = DataHelper.validPersistableDepartment1();
        d1.setId(1L);

        DepartmentDto dto = new DepartmentDto();
        dto.setCode("UPD");
        dto.setName("Updated Name");

        ArgumentCaptor<Department> deptParam = ArgumentCaptor.forClass(Department.class);
        Mockito.when(departmentRepository.save(deptParam.capture())).thenReturn(null);
        Mockito.when(departmentRepository.findByCode(d1.getCode())).thenReturn(Optional.of(d1));

        service.updateAll(d1.getCode(), dto);

        assertDepartmentDataEqual(deptParam.getValue(), dto);
        Mockito.verify(departmentRepository, Mockito.times(1)).flush();
        Mockito.verify(departmentRepository, Mockito.times(1)).save(d1);
    }

    @Test
    void testUpdateOverride() {

        Department d1 = DataHelper.validPersistableDepartment1();
        d1.setId(1L);

        DepartmentDto dto = new DepartmentDto();
        dto.setCode("UPD");

        ArgumentCaptor<Department> deptParam = ArgumentCaptor.forClass(Department.class);
        Mockito.when(departmentRepository.save(deptParam.capture())).thenReturn(null);
        Mockito.when(departmentRepository.findByCode(d1.getCode())).thenReturn(Optional.of(d1));

        service.updateAll(d1.getCode(), dto);

        Assertions.assertNull(d1.getName());
        Assertions.assertEquals(d1.getCode(), dto.getCode());

        Mockito.verify(departmentRepository, Mockito.times(1)).flush();
        Mockito.verify(departmentRepository, Mockito.times(1)).save(d1);
    }

    @Test
    void testUpdateOne_code() {
        Department d1 = DataHelper.validPersistableDepartment1();
        d1.setId(1L);

        DepartmentDto dto = new DepartmentDto();
        dto.setCode("UPD");

        ArgumentCaptor<Department> deptParam = ArgumentCaptor.forClass(Department.class);
        Mockito.when(departmentRepository.save(deptParam.capture())).thenReturn(null);
        Mockito.when(departmentRepository.findByCode(d1.getCode())).thenReturn(Optional.of(d1));

        service.updateOne(d1.getCode(), dto);

        Assertions.assertNotNull(d1.getName());
        Assertions.assertEquals(d1.getCode(), dto.getCode());

        Mockito.verify(departmentRepository, Mockito.times(1)).flush();
        Mockito.verify(departmentRepository, Mockito.times(1)).save(d1);
    }

    @Test
    void testUpdateOne_name() {
        Department d1 = DataHelper.validPersistableDepartment1();
        d1.setId(1L);

        DepartmentDto dto = new DepartmentDto();
        dto.setName("Some Name");

        ArgumentCaptor<Department> deptParam = ArgumentCaptor.forClass(Department.class);
        Mockito.when(departmentRepository.save(deptParam.capture())).thenReturn(null);
        Mockito.when(departmentRepository.findByCode(d1.getCode())).thenReturn(Optional.of(d1));

        service.updateOne(d1.getCode(), dto);

        Assertions.assertNotNull(d1.getCode());
        Assertions.assertEquals(d1.getName(), dto.getName());

        Mockito.verify(departmentRepository, Mockito.times(1)).flush();
        Mockito.verify(departmentRepository, Mockito.times(1)).save(d1);
    }

    @Test
    void testDelete() {
        Department d1 = DataHelper.validPersistableDepartment1();
        d1.setId(1L);
        d1.setCode("CODE-X");

        Mockito.when(departmentRepository.findByCode(d1.getCode())).thenReturn(Optional.of(d1));
        service.delete("CODE-X");

        Mockito.verify(departmentRepository, Mockito.times(1)).delete(d1);
    }

    private void assertDepartmentDataEqual(Department department, DepartmentDto dto) {
        Assertions.assertEquals(department.getCode(), dto.getCode());
        Assertions.assertEquals(department.getName(), dto.getName());
    }

    private List<Department> createCockDepartmentList() {
        Department d1 = DataHelper.validPersistableDepartment1();
        d1.setId(1L);

        Department d2 = DataHelper.validPersistableDepartment2();
        d2.setId(2L);

        return Arrays.asList(d1, d2);
    }
}
