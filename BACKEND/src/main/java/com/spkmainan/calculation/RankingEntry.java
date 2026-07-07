package com.spkmainan.calculation;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One toy's rank + SAW score within a profile's result (snapshot). */
@Entity
@Table(name = "ranking_entries")
public class RankingEntry extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private CalculationResult result;

    @Column(name = "toy_id", nullable = false)
    private int toyId;

    @Column(name = "toy_name", nullable = false)
    private String toyName;

    @Column(name = "category_name")
    private String categoryName;

    // "rank" is a reserved SQL word → store as rank_no.
    @Column(name = "rank_no", nullable = false)
    private int rankNo;

    @Column(name = "saw_score", nullable = false)
    private double sawScore;

    protected RankingEntry() {
    }

    public RankingEntry(int toyId, String toyName, String categoryName, int rankNo, double sawScore) {
        this.toyId = toyId;
        this.toyName = toyName;
        this.categoryName = categoryName;
        this.rankNo = rankNo;
        this.sawScore = sawScore;
    }

    public void setResult(CalculationResult result) {
        this.result = result;
    }

    public int getToyId() {
        return toyId;
    }

    public String getToyName() {
        return toyName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getRankNo() {
        return rankNo;
    }

    public double getSawScore() {
        return sawScore;
    }
}
