package me.ronygomes.ums.api.model;

import me.ronygomes.ums.api.testHelper.DataHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentTest {

    @Test
    void testMergeFullName() {
        Student s = Mockito.spy(Student.class);

        Student input = new Student();
        input.setFullName("John");

        s.merge(input);

        Mockito.verify(s, Mockito.times(1)).setFullName("John");
        Mockito.verify(s, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeEmail() {
        Student s = Mockito.spy(Student.class);

        Student input = new Student();
        input.setEmail("Email");

        s.merge(input);

        Mockito.verify(s, Mockito.times(1)).setEmail("Email");
        Mockito.verify(s, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeContactNumber() {
        Student s = Mockito.spy(Student.class);

        Student input = new Student();
        input.setContactNumber("CN");

        s.merge(input);

        Mockito.verify(s, Mockito.times(1)).setContactNumber("CN");
        Mockito.verify(s, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeAddress() {
        Student s = Mockito.spy(Student.class);

        Student input = new Student();
        input.setAddress("A");

        s.merge(input);

        Mockito.verify(s, Mockito.times(1)).setAddress("A");
        Mockito.verify(s, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeRegistrationDate() {
        Student s = Mockito.spy(Student.class);

        Student input = new Student();
        Date d = new Date();
        input.setRegistrationDate(d);

        s.merge(input);

        Mockito.verify(s, Mockito.times(1)).setRegistrationDate(d);
        Mockito.verify(s, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeRegistrationNumber() {
        Student s = Mockito.spy(Student.class);

        Student input = new Student();
        input.setRegistrationNumber("1");

        s.merge(input);

        Mockito.verify(s, Mockito.times(1)).setRegistrationNumber("1");
        Mockito.verify(s, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeDepartmentCode() {
        Student s = Mockito.spy(Student.class);

        Student input = new Student();
        input.setDepartmentCode("CODE");

        s.merge(input);

        Mockito.verify(s, Mockito.times(1)).setDepartmentCode("CODE");
        Mockito.verify(s, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeEducations() {
        Student s = Mockito.spy(Student.class);

        Student input = new Student();
        List<Education> e = new ArrayList<>();
        e.add(new Education());
        input.setEducations(e);

        s.merge(input);

        Mockito.verify(s, Mockito.times(1)).setEducations(e);
        Mockito.verify(s, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testFindEducationById() {
        Student s = DataHelper.validPersistableStudent1(new Department());
        Assertions.assertEquals(2, s.getEducations().size());

        Assertions.assertNull(s.getEducations().get(0).getId());
        s.getEducations().get(0).setId(1038L);

        Assertions.assertNull(s.getEducations().get(1).getId());
        s.getEducations().get(1).setId(1002L);

        Assertions.assertEquals(0, s.findEducationById(1038L));
        Assertions.assertEquals(1, s.findEducationById(1002L));
        Assertions.assertEquals(-1, s.findEducationById(1L)); // Non existent
    }
}
