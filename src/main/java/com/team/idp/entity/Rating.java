package com.team.idp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/** A customer's star rating (1 to 5) of an IDP. One rating per customer per IDP; rating again updates it. */
@Entity
@Table(name = "ratings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "idp_id"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ratingID;

    @Column(name = "customer_id", nullable = false)
    private Long customerID;

    @Column(name = "idp_id", nullable = false)
    private Long idpID;

    @Column(nullable = false)
    private int stars;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime ratedAt;

    public Rating() {}

    public Rating(Long customerID, Long idpID, int stars, String comment) {
        this.customerID = customerID;
        this.idpID = idpID;
        this.stars = stars;
        this.comment = comment;
    }

    @PrePersist
    @PreUpdate
    void onSave() {
        ratedAt = LocalDateTime.now();
    }

    public Long getRatingID() { return ratingID; }
    public Long getCustomerID() { return customerID; }
    public Long getIdpID() { return idpID; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getRatedAt() { return ratedAt; }
    public void setRatedAt(LocalDateTime ratedAt) { this.ratedAt = ratedAt; }
}
