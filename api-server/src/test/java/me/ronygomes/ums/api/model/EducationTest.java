package me.ronygomes.ums.api.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EducationTest {

    @Test
    void testMergeExamType() {
        Education e = Mockito.spy(Education.class);

        Education input = new Education();
        input.setExamType(ExamType.SSC);

        e.merge(input);

        Mockito.verify(e, Mockito.times(1)).setExamType(ExamType.SSC);
        Mockito.verify(e, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeGrade() {
        Education e = Mockito.spy(Education.class);

        Education input = new Education();
        input.setGrade(Grade.A);

        e.merge(input);

        Mockito.verify(e, Mockito.times(1)).setGrade(Grade.A);
        Mockito.verify(e, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeCgpa() {
        Education e = Mockito.spy(Education.class);

        Education input = new Education();
        input.setCgpa(3.7f);

        e.merge(input);

        Mockito.verify(e, Mockito.times(1)).setCgpa(3.7f);
        Mockito.verify(e, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeCertificatePath() {
        Education e = Mockito.spy(Education.class);

        Education input = new Education();
        input.setCertificatePath("Path");

        e.merge(input);

        Mockito.verify(e, Mockito.times(1)).setCertificatePath("Path");
        Mockito.verify(e, Mockito.never()).setId(Mockito.any());
    }

    @Test
    void testMergeCertificateFileName() {
        Education e = Mockito.spy(Education.class);

        Education input = new Education();
        input.setCertificateFileName("Name");

        e.merge(input);

        Mockito.verify(e, Mockito.times(1)).setCertificateFileName("Name");
        Mockito.verify(e, Mockito.never()).setId(Mockito.any());
    }
}
