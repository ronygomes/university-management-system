package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.Department;
import me.ronygomes.ums.api.model.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    Optional<Designation> findByTitle(String title);

    List<Designation> findAllByOrderByTitleAsc();
}
