package com.spkmainan.calculation;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** The ranking + consistency of one weight profile within a calculation run. */
@Entity
@Table(name = "calculation_results")
public class CalculationResult extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private CalculationRun run;

    @Column(name = "profile_code", nullable = false, length = 50)
    private String profileCode;

    @Column(name = "profile_name", nullable = false)
    private String profileName;

    @Column(nullable = false)
    private double cr;

    @Column(name = "lambda_max", nullable = false)
    private double lambdaMax;

    @Column(nullable = false)
    private double ci;

    @Column(nullable = false)
    private boolean consistent;

    @Column(name = "best_toy_id")
    private Integer bestToyId;

    @Column(name = "best_toy_name")
    private String bestToyName;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "icon", length = 50)
    private String icon;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RankingEntry> rankings = new ArrayList<>();

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalculationWeight> weights = new ArrayList<>();

    protected CalculationResult() {
    }

    public CalculationResult(String profileCode, String profileName, double cr, double lambdaMax,
                             double ci, boolean consistent) {
        this.profileCode = profileCode;
        this.profileName = profileName;
        this.cr = cr;
        this.lambdaMax = lambdaMax;
        this.ci = ci;
        this.consistent = consistent;
    }

    public void addRanking(RankingEntry entry) {
        entry.setResult(this);
        rankings.add(entry);
    }

    public void setRun(CalculationRun run) {
        this.run = run;
    }

    public String getProfileCode() {
        return profileCode;
    }

    public String getProfileName() {
        return profileName;
    }

    public double getCr() {
        return cr;
    }

    public double getLambdaMax() {
        return lambdaMax;
    }

    public double getCi() {
        return ci;
    }

    public boolean isConsistent() {
        return consistent;
    }

    public Integer getBestToyId() {
        return bestToyId;
    }

    public void setBestToyId(Integer bestToyId) {
        this.bestToyId = bestToyId;
    }

    public String getBestToyName() {
        return bestToyName;
    }

    public void setBestToyName(String bestToyName) {
        this.bestToyName = bestToyName;
    }

    public List<RankingEntry> getRankings() {
        return rankings;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void addWeight(CalculationWeight weight) {
        weight.setResult(this);
        weights.add(weight);
    }

    public List<CalculationWeight> getWeights() {
        return weights;
    }
}
