package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("FROM Student s LEFT JOIN FETCH s.educations WHERE s.registrationNumber = :rn")
    Optional<Student> findByRegistrationNumber(@Param("rn") String registrationNumber);

    @Query("FROM Student s LEFT JOIN FETCH s.educations WHERE s.id = :id")
    Optional<Student> findWithEducationById(Long id);

    Optional<Student> findByEmail(String email);
}
