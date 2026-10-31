package com.team.idp.repository;

import com.team.idp.entity.IDP;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Customer-side IDP queries (US13). Kept separate from B3's IDPRepository so we never edit the same file.
 * TODO when B2's Category entity is merged: also match the category name.
 */
public interface IDPSearchRepository extends JpaRepository<IDP, Long> {

    /** Blank keyword: all IDPs, newest first. */
    List<IDP> findAllByOrderByCreatedAtDesc();

    /** Case-insensitive partial match on title or description, newest first. */
    @Query("SELECT i FROM IDP i "
         + "WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
         + "   OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) "
         + "ORDER BY i.createdAt DESC")
    List<IDP> searchByKeyword(@Param("keyword") String keyword);
}
