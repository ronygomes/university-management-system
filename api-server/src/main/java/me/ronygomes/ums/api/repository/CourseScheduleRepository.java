package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {

    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.course.id = :courseId ORDER BY cs.startDate")
    List<CourseSchedule> findByCourseId(Long courseId);
}
