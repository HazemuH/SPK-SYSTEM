package com.spkmainan.criterion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CriterionLevelRepository extends JpaRepository<CriterionLevelEntity, Long> {

    List<CriterionLevelEntity> findAllByOrderByCriterionCodeAscLevelAsc();

    List<CriterionLevelEntity> findByCriterionCodeOrderByLevelAsc(String criterionCode);

    /** Drop a removed criterion's subcriteria along with it. */
    @Modifying
    @Query("delete from CriterionLevelEntity l where l.criterionCode = :code")
    void deleteByCriterionCode(@Param("code") String code);
}
