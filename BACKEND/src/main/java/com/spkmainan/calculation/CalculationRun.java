package com.spkmainan.calculation;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** One calculation session: SAW synthesis of the active toys for every weight profile. */
@Entity
@Table(name = "calculation_runs")
public class CalculationRun extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "run_at", nullable = false)
    private Instant runAt;

    @Column(name = "alt_count", nullable = false)
    private int altCount;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalculationResult> results = new ArrayList<>();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalculationCriterion> criteria = new ArrayList<>();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalculationNorm> norms = new ArrayList<>();

    protected CalculationRun() {
    }

    public CalculationRun(String code, Instant runAt, int altCount) {
        this.code = code;
        this.runAt = runAt;
        this.altCount = altCount;
    }

    public void addResult(CalculationResult result) {
        result.setRun(this);
        results.add(result);
    }

    public void addCriterion(CalculationCriterion criterion) {
        criterion.setRun(this);
        criteria.add(criterion);
    }

    public void addNorm(CalculationNorm norm) {
        norm.setRun(this);
        norms.add(norm);
    }

    public List<CalculationCriterion> getCriteria() {
        return criteria;
    }

    public List<CalculationNorm> getNorms() {
        return norms;
    }

    public String getCode() {
        return code;
    }

    public Instant getRunAt() {
        return runAt;
    }

    public int getAltCount() {
        return altCount;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<CalculationResult> getResults() {
        return results;
    }
}
