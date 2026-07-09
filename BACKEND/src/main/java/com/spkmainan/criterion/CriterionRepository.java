package com.spkmainan.criterion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CriterionRepository extends JpaRepository<CriterionEntity, Long> {

    List<CriterionEntity> findAllByOrderByNoAsc();

    Optional<CriterionEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select coalesce(max(c.no), 0) from CriterionEntity c")
    int maxNo();

    /** Remove this criterion's ratings from every toy (element-collection table). */
    @Modifying
    @Query(value = "DELETE FROM toy_scores WHERE criterion_code = :code", nativeQuery = true)
    void deleteToyScores(@Param("code") String code);

    /** Remove this criterion's weight from every profile (element-collection table). */
    @Modifying
    @Query(value = "DELETE FROM weight_profile_weights WHERE criterion_code = :code", nativeQuery = true)
    void deleteProfileWeights(@Param("code") String code);
}
