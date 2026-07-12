package com.spkmainan.toy;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ToyRepository extends JpaRepository<ToyEntity, Long> {

    @Query("select max(t.updatedAt) from ToyEntity t")
    Instant maxUpdatedAt();

    Page<ToyEntity> findByNameContainingIgnoreCaseAndCategoryCode(
        String name, String categoryCode, Pageable pageable);

    Page<ToyEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByCategoryCode(String categoryCode);
}
