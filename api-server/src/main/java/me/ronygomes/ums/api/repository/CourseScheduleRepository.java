package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {
}
