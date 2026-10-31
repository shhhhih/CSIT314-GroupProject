package com.team.idp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryID;

    @Column(nullable = false, unique = true, length = 100)
    private String categoryName;

    @Column(length = 500) 
    private String description;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Category() {}

    public Category(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
    }
    @PrePersist
    public void setCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    public Long getCategoryID() {
        return categoryID;
    }
    public void setCategoryID(Long categoryID) {
        this.categoryID = categoryID;
    }
    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}