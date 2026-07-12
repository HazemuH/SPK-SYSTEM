package com.spkmainan.calculation;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A criterion as it stood at publish time (frozen: type drives normalization; name/abbr label it). */
@Entity
@Table(name = "calculation_criteria")
public class CalculationCriterion extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private CalculationRun run;

    @Column(name = "criterion_code", nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String abbr;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "no", nullable = false)
    private int no;

    protected CalculationCriterion() {
    }

    public CalculationCriterion(String code, String name, String abbr, String type, int no) {
        this.code = code;
        this.name = name;
        this.abbr = abbr;
        this.type = type;
        this.no = no;
    }

    public void setRun(CalculationRun run) {
        this.run = run;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAbbr() {
        return abbr;
    }

    public String getType() {
        return type;
    }

    public int getNo() {
        return no;
    }
}
