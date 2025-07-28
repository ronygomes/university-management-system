package me.ronygomes.ums.api.controller;

import me.ronygomes.ums.api.assembler.DesignationModelAssembler;
import me.ronygomes.ums.api.config.annotation.AdminAccess;
import me.ronygomes.ums.api.dto.DesignationModel;
import me.ronygomes.ums.api.exception.ExceptionType;
import me.ronygomes.ums.api.exception.UmsDataException;
import me.ronygomes.ums.api.model.Designation;
import me.ronygomes.ums.api.repository.DesignationRepository;
import me.ronygomes.ums.api.testHelper.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DesignationControllerTest {

    @Mock
    private DesignationRepository designationRepository;

    @Mock
    private DesignationModelAssembler designationModelAssembler;

    private DesignationController controller;

    @BeforeEach
    void setup() {
        controller = new DesignationController(designationRepository, designationModelAssembler);
    }

    @Test
    void testDesignations() {
        List<Designation> dummyDesignations = new ArrayList<>();
        Mockito.when(designationRepository.findAll()).thenReturn(dummyDesignations);

        CollectionModel<DesignationModel> col = CollectionModel.of(new ArrayList<>());
        Mockito.when(designationModelAssembler.toCollectionModel(dummyDesignations)).thenReturn(col);

        Assertions.assertSame(col, controller.designations());
        Mockito.verify(designationRepository, Mockito.times(1)).findAll();
        Mockito.verify(designationModelAssembler, Mockito.times(1)).toCollectionModel(dummyDesignations);
    }

    @Test
    void testDesignationSuccess() {
        Designation d = new Designation();
        Mockito.when(designationRepository.findById(1L)).thenReturn(Optional.of(d));

        DesignationModel expected = new DesignationModel();
        Mockito.when(designationModelAssembler.toModel(d)).thenReturn(expected);

        DesignationModel model = controller.designation(1L);
        Assertions.assertSame(expected, model);

        Mockito.verify(designationRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(designationModelAssembler, Mockito.times(1)).toModel(d);
    }

    @Test
    void testDesignationFailure() {
        Mockito.when(designationRepository.findById(500L)).thenReturn(Optional.empty());

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> controller.designation(500L));
        Assertions.assertEquals(ExceptionType.ENTITY_NOT_FOUND, ex.getExceptionType());
        Assertions.assertEquals("Designation with id '500' not found", ex.getErrorDetails());
        Assertions.assertTrue(ex.getErrors().isEmpty());

        Mockito.verify(designationRepository, Mockito.times(1)).findById(500L);
        Mockito.verifyNoInteractions(designationModelAssembler);
    }

    @Test
    void testCreateSuccess() {

        ArgumentCaptor<Designation> ac = ArgumentCaptor.forClass(Designation.class);
        Mockito.doAnswer((i) -> {
            Designation d = (Designation) i.getArguments()[0];
            d.setId(500L);
            return null;
        }).when(designationRepository).save(ac.capture());

        DesignationModel model = new DesignationModel();
        model.setTitle("Create");
        ResponseEntity<?> res = controller.create(model);

        Assertions.assertEquals(model.getTitle(), ac.getValue().getTitle());
        Assertions.assertEquals(HttpStatus.CREATED, res.getStatusCode());
        Assertions.assertNotNull(res.getHeaders().getLocation());
        Assertions.assertEquals("/v1/designations/500", res.getHeaders().getLocation().toASCIIString());
        Assertions.assertNull(res.getBody());
    }

    @Test
    void testCreateValidationFailure() {
        UmsDataException ex = new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "Ignore");
        Mockito.doThrow(ex).when(designationRepository).save(Mockito.any());

        UmsDataException got = Assertions.assertThrows(UmsDataException.class, () -> controller.create(new DesignationModel()));
        Assertions.assertSame(ex, got);

        Mockito.verifyNoInteractions(designationModelAssembler);
    }

    @Test
    void testUpdateSuccess() {
        Designation dbData = new Designation();
        dbData.setId(1L);
        dbData.setTitle("Initial");

        Mockito.when(designationRepository.findById(dbData.getId())).thenReturn(Optional.of(dbData));

        ArgumentCaptor<Designation> ac = ArgumentCaptor.forClass(Designation.class);
        Mockito.when(designationRepository.save(ac.capture())).thenReturn(new Designation());

        DesignationModel input = new DesignationModel();
        input.setTitle("Updated");
        ResponseEntity<?> res = controller.update(1L, input);

        Designation capture = ac.getValue();
        Assertions.assertEquals(input.getTitle(), capture.getTitle());
        Assertions.assertEquals(dbData.getId(), capture.getId());

        Assertions.assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
        Assertions.assertNotNull(res.getHeaders().getLocation());
        Assertions.assertEquals("/v1/designations/1", res.getHeaders().getLocation().toASCIIString());
    }

    @Test
    void testUpdateFailure() {
        Mockito.when(designationRepository.findById(500L)).thenReturn(Optional.empty());

        UmsDataException ex = Assertions.assertThrows(UmsDataException.class, () -> controller.update(500L, new DesignationModel()));
        Assertions.assertEquals(ExceptionType.ENTITY_NOT_FOUND, ex.getExceptionType());
        Assertions.assertEquals("Designation with id '500' not found", ex.getErrorDetails());
        Assertions.assertTrue(ex.getErrors().isEmpty());

        Mockito.verify(designationRepository, Mockito.times(1)).findById(500L);
        Mockito.verifyNoInteractions(designationModelAssembler);
    }

    @Test
    void testUpdateValidationFailureOnSave() {
        UmsDataException ex = new UmsDataException(ExceptionType.ENTITY_NOT_FOUND, "Ignore");
        Mockito.doThrow(ex).when(designationRepository).save(Mockito.any());
        Mockito.when(designationRepository.findById(500L)).thenReturn(Optional.of(new Designation()));

        UmsDataException got = Assertions.assertThrows(UmsDataException.class, () -> controller.update(500L, new DesignationModel()));
        Assertions.assertSame(ex, got);

        Mockito.verify(designationRepository, Mockito.times(1)).findById(500L);
        Mockito.verifyNoInteractions(designationModelAssembler);
    }

    @Test
    void testDeleteSuccess() {
        Designation dbData = new Designation();
        dbData.setId(500L);
        dbData.setTitle("For Delete");

        Mockito.when(designationRepository.findById(500L)).thenReturn(Optional.of(dbData));
        ResponseEntity<?> res = controller.delete(500L);

        Assertions.assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
        Assertions.assertNull(res.getBody());
        Mockito.verify(designationRepository, Mockito.times(1)).delete(dbData);
    }

    @Test
    void testAccessCheck() {
        Method[] publicMethods = TestHelper.getPublicMethods(DesignationController.class);

        Assertions.assertEquals(5, publicMethods.length);
        for (Method method : publicMethods) {
            Assertions.assertTrue(method.isAnnotationPresent(AdminAccess.class));
        }
    }
}
