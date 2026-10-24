
package com.team.idp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "idps")
public class IDP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idpID;

    @Column(nullable = false)
    private Long companyID;

    @Column(nullable = false)
    private Long categoryID;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false)
    private String propertyType;

    @Column(nullable = false)
    private String projectStatus;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int shortlistCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public IDP() {}

    public IDP(Long companyID, Long categoryID, String title,
               String description, BigDecimal budget,
               String propertyType, String projectStatus) {
        this.companyID = companyID;
        this.categoryID = categoryID;
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.propertyType = propertyType;
        this.projectStatus = projectStatus;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getIdpID() { return idpID; }

    public Long getCompanyID() { return companyID; }
    public void setCompanyID(Long companyID) {
        this.companyID = companyID;
    }

    public Long getCategoryID() { return categoryID; }
    public void setCategoryID(Long categoryID) {
        this.categoryID = categoryID;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getProjectStatus() { return projectStatus; }
    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getShortlistCount() { return shortlistCount; }
    public void setShortlistCount(int shortlistCount) {
        this.shortlistCount = shortlistCount;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

