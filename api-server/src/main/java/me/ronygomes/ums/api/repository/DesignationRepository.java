package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    List<Designation> findAllByOrderByIdAsc();
}
