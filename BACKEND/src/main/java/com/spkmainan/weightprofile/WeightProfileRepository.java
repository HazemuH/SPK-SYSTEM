package com.spkmainan.weightprofile;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightProfileRepository extends JpaRepository<WeightProfileEntity, Long> {

    Optional<WeightProfileEntity> findByCode(String code);

    boolean existsByCode(String code);
}
