package com.spkmainan.calculation;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Frozen normalized value r_ij for one toy under one criterion (0..1). */
@Entity
@Table(name = "calculation_norms")
public class CalculationNorm extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private CalculationRun run;

    @Column(name = "toy_id", nullable = false)
    private int toyId;

    @Column(name = "criterion_code", nullable = false, length = 50)
    private String criterionCode;

    @Column(name = "norm_value", nullable = false)
    private double normValue;

    protected CalculationNorm() {
    }

    public CalculationNorm(int toyId, String criterionCode, double normValue) {
        this.toyId = toyId;
        this.criterionCode = criterionCode;
        this.normValue = normValue;
    }

    public void setRun(CalculationRun run) {
        this.run = run;
    }

    public int getToyId() {
        return toyId;
    }

    public String getCriterionCode() {
        return criterionCode;
    }

    public double getNormValue() {
        return normValue;
    }
}
