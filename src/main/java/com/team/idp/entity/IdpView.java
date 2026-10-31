package com.team.idp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** One record each time a customer opens an IDP's details (US15). The daily report (US22) counts these by date. */
@Entity
@Table(name = "idp_views")
public class IdpView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long viewID;

    @Column(nullable = false)
    private Long idpID;

    @Column(nullable = false)
    private Long customerID;

    @Column(nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    public IdpView() {}

    public IdpView(Long idpID, Long customerID) {
        this.idpID = idpID;
        this.customerID = customerID;
    }

    @PrePersist
    void onCreate() {
        if (viewedAt == null) {
            viewedAt = LocalDateTime.now();
        }
    }

    public Long getViewID() { return viewID; }
    public Long getIdpID() { return idpID; }
    public void setIdpID(Long idpID) { this.idpID = idpID; }
    public Long getCustomerID() { return customerID; }
    public void setCustomerID(Long customerID) { this.customerID = customerID; }
    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
