package com.team.idp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "idp_images")
public class IDPImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageID;

    @Column(nullable = false)
    private Long idpID;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    // Existing image records default to false after the schema update.
    @Column(name = "is_cover", nullable = false)
    private boolean cover = false;

    public IDPImage() { }
    public IDPImage(Long idpID, String imageUrl) {
        this.idpID = idpID;
        this.imageUrl = imageUrl;
    }
    public Long getImageID() { return imageID; }
    public Long getIdpID() { return idpID; }
    public String getImageUrl() { return imageUrl; }
    public boolean isCover() { return cover; }
    public void setCover(boolean cover) { this.cover = cover; }
}
