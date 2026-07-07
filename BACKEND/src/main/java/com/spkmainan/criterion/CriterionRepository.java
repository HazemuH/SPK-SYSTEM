package com.spkmainan.criterion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriterionRepository extends JpaRepository<CriterionEntity, Long> {

    List<CriterionEntity> findAllByOrderByNoAsc();

    Optional<CriterionEntity> findByCode(String code);
}
