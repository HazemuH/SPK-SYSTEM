package com.spkmainan.calculation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculationRunRepository extends JpaRepository<CalculationRun, Long> {

    List<CalculationRun> findAllByOrderByRunAtDesc();

    Optional<CalculationRun> findFirstByPublishedTrueOrderByPublishedAtDesc();
}
