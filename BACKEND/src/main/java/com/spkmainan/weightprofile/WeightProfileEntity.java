package com.spkmainan.weightprofile;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persistent weight profile (scenario): the validated AHP pairwise result —
 * a weight vector (criterion code → weight, Σ=1) plus consistency metrics.
 */
@Entity
@Table(name = "weight_profiles")
public class WeightProfileEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(length = 30)
    private String icon;

    @Column(length = 512)
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean defaultProfile = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private double cr;

    @Column(name = "lambda_max", nullable = false)
    private double lambdaMax;

    @Column(nullable = false)
    private double ci;

    @ElementCollection
    @CollectionTable(name = "weight_profile_weights",
        joinColumns = @JoinColumn(name = "weight_profile_id"))
    @MapKeyColumn(name = "criterion_code", length = 50)
    @Column(name = "weight", nullable = false)
    private Map<String, Double> weights = new LinkedHashMap<>();

    protected WeightProfileEntity() {
    }

    public WeightProfileEntity(String code, String name, String shortName, String icon,
                               String description, boolean defaultProfile, boolean active) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.icon = icon;
        this.description = description;
        this.defaultProfile = defaultProfile;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(boolean defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getCr() {
        return cr;
    }

    public void setCr(double cr) {
        this.cr = cr;
    }

    public double getLambdaMax() {
        return lambdaMax;
    }

    public void setLambdaMax(double lambdaMax) {
        this.lambdaMax = lambdaMax;
    }

    public double getCi() {
        return ci;
    }

    public void setCi(double ci) {
        this.ci = ci;
    }

    public Map<String, Double> getWeights() {
        return weights;
    }

    public void setWeights(Map<String, Double> weights) {
        this.weights = weights;
    }
}
