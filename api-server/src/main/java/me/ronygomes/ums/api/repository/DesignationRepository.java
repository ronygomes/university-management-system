package me.ronygomes.ums.api.repository;

import me.ronygomes.ums.api.model.Designation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    String CACHE_KEY = "designationListCache";

    @Override
    @Cacheable(CACHE_KEY)
    // Spring will store the complete list under one key here, so evicting
    // with allEntries = true, which is expected to have one key
    List<Designation> findAll();

    @Override
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    <S extends Designation> S save(S entity);

    @Override
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    void delete(Designation entity);

    Optional<Designation> findByTitle(String title);

    List<Designation> findAllByOrderByTitleAsc();
}
