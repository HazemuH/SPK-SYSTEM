package com.spkmainan.criterion;

import com.spkmainan.common.domain.BaseEntity;
import com.spkmainan.domain.CriterionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Persistent AHP criterion (10 fixed: 9 benefit + 1 cost). */
@Entity
@Table(name = "criteria")
public class CriterionEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "no", nullable = false)
    private int no;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CriterionType type;

    @Column(length = 512)
    private String description;

    @Column(length = 50)
    private String abbr;

    @Column(nullable = false)
    private boolean active = true;

    protected CriterionEntity() {
    }

    public CriterionEntity(String code, int no, String name, CriterionType type,
                           String description, String abbr, boolean active) {
        this.code = code;
        this.no = no;
        this.name = name;
        this.type = type;
        this.description = description;
        this.abbr = abbr;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CriterionType getType() {
        return type;
    }

    public void setType(CriterionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
