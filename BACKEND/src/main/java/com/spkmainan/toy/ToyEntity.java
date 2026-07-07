package com.spkmainan.toy;

import com.spkmainan.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Persistent toy (alternative). {@code scores} holds the 1–5 rating per benefit
 * criterion (code → rating); the cost criterion "harga" uses {@code price}.
 */
@Entity
@Table(name = "toys")
public class ToyEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(nullable = false)
    private long price;

    @Column(name = "age_min", nullable = false)
    private int ageMin;

    @Column(name = "age_max", nullable = false)
    private int ageMax;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 512)
    private String description;

    @ElementCollection
    @CollectionTable(name = "toy_tags", joinColumns = @JoinColumn(name = "toy_id"))
    @Column(name = "tag", nullable = false, length = 100)
    private Set<String> tags = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "toy_scores", joinColumns = @JoinColumn(name = "toy_id"))
    @MapKeyColumn(name = "criterion_code", length = 50)
    @Column(name = "rating", nullable = false)
    private Map<String, Integer> scores = new LinkedHashMap<>();

    protected ToyEntity() {
    }

    public ToyEntity(String name, String categoryCode, long price, int ageMin, int ageMax,
                     int stock, boolean active, String description) {
        this.name = name;
        this.categoryCode = categoryCode;
        this.price = price;
        this.ageMin = ageMin;
        this.ageMax = ageMax;
        this.stock = stock;
        this.active = active;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(int ageMin) {
        this.ageMin = ageMin;
    }

    public int getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(int ageMax) {
        this.ageMax = ageMax;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }
}
