package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}
