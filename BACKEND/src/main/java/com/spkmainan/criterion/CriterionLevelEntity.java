package com.spkmainan.criterion;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Persistent subcriterion (S1–S5) of a criterion, with its AHP local priority. */
@Entity
@Table(name = "criterion_levels",
    uniqueConstraints = @UniqueConstraint(columnNames = {"criterion_code", "level"}))
public class CriterionLevelEntity extends BaseEntity {

    @Column(name = "criterion_code", nullable = false, length = 50)
    private String criterionCode;

    /** 1 = S1 (best band) … 5 = S5 (worst band). */
    @Column(name = "level", nullable = false)
    private int level;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private double priority;

    protected CriterionLevelEntity() {
    }

    public CriterionLevelEntity(String criterionCode, int level, String label, double priority) {
        this.criterionCode = criterionCode;
        this.level = level;
        this.label = label;
        this.priority = priority;
    }

    public String getCriterionCode() {
        return criterionCode;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }
}
