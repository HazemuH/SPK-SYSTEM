package com.spkmainan.calculation;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Frozen AHP weight for one criterion within a profile result. */
@Entity
@Table(name = "calculation_weights")
public class CalculationWeight extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private CalculationResult result;

    @Column(name = "criterion_code", nullable = false, length = 50)
    private String criterionCode;

    @Column(nullable = false)
    private double weight;

    protected CalculationWeight() {
    }

    public CalculationWeight(String criterionCode, double weight) {
        this.criterionCode = criterionCode;
        this.weight = weight;
    }

    public void setResult(CalculationResult result) {
        this.result = result;
    }

    public String getCriterionCode() {
        return criterionCode;
    }

    public double getWeight() {
        return weight;
    }
}
