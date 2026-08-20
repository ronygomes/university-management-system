package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.Course;
import me.ronygomes.ums.api.model.Semester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
            SELECT c FROM Course c
            WHERE (:departmentCode IS NULL OR c.department.code = :departmentCode)
              AND (:semester IS NULL OR c.semester = :semester)
            """)
    Page<Course> findFiltered(@Param("departmentCode") String departmentCode,
                              @Param("semester") Semester semester,
                              Pageable pageable);
}
