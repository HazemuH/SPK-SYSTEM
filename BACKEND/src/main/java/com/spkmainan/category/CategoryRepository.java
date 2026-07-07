package com.spkmainan.category;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);
}
