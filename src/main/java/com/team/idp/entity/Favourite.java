package com.team.idp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/** A customer saving an IDP to their favourites (US17, US18). One row per customer per IDP. */
@Entity
@Table(name = "favourites",
       uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "idp_id"}))
public class Favourite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favouriteID;

    @Column(name = "customer_id", nullable = false)
    private Long customerID;

    @Column(name = "idp_id", nullable = false)
    private Long idpID;

    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;   // the daily report counts shortlists by this date

    public Favourite() {}

    public Favourite(Long customerID, Long idpID) {
        this.customerID = customerID;
        this.idpID = idpID;
    }

    @PrePersist
    void onCreate() {
        if (savedAt == null) {
            savedAt = LocalDateTime.now();
        }
    }

    public Long getFavouriteID() { return favouriteID; }
    public Long getCustomerID() { return customerID; }
    public void setCustomerID(Long customerID) { this.customerID = customerID; }
    public Long getIdpID() { return idpID; }
    public void setIdpID(Long idpID) { this.idpID = idpID; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
