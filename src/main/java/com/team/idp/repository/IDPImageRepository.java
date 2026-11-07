package com.team.idp.repository;

import com.team.idp.entity.IDPImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDPImageRepository extends JpaRepository<IDPImage, Long> {
    List<IDPImage> findByIdpIDOrderByImageIDAsc(Long idpID);
    long countByIdpID(Long idpID);
    void deleteByIdpID(Long idpID);
}
