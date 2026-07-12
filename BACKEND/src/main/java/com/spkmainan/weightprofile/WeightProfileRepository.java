package com.spkmainan.weightprofile;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WeightProfileRepository extends JpaRepository<WeightProfileEntity, Long> {

    Optional<WeightProfileEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select max(p.updatedAt) from WeightProfileEntity p")
    Instant maxUpdatedAt();
}
