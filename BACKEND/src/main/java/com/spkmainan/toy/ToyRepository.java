package com.spkmainan.toy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToyRepository extends JpaRepository<ToyEntity, Long> {

    Page<ToyEntity> findByNameContainingIgnoreCaseAndCategoryCode(
        String name, String categoryCode, Pageable pageable);

    Page<ToyEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByCategoryCode(String categoryCode);
}
