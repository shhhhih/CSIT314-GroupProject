package com.team.idp.repository;

import com.team.idp.entity.IDP;

import java.math.BigDecimal;
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

    /** Search IDPs with optional keyword, category, property type and budget filters. */
    @Query("SELECT i FROM IDP i "
         + "WHERE (:keyword IS NULL OR "
         + "       LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
         + "       OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))        OR EXISTS (SELECT c FROM Category c WHERE c.categoryID = i.categoryID        AND LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
         + "AND (:categoryId IS NULL OR i.categoryID = :categoryId) "
         + "AND (:propertyType IS NULL OR LOWER(i.propertyType) = LOWER(:propertyType)) "
         + "AND (:minBudget IS NULL OR i.budget >= :minBudget) "
         + "AND (:maxBudget IS NULL OR i.budget <= :maxBudget) "
         + "ORDER BY i.createdAt DESC")
    List<IDP> searchWithFilters(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("propertyType") String propertyType,
        @Param("minBudget") java.math.BigDecimal minBudget,
        @Param("maxBudget") java.math.BigDecimal maxBudget);
        
/**
 * Customer search: only show Ongoing and Completed projects.
 */
@Query("""
    SELECT i FROM IDP i
    WHERE LOWER(i.projectStatus) IN ('published','ongoing', 'completed')
    AND (:keyword IS NULL
         OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
         OR EXISTS (SELECT c FROM Category c WHERE c.categoryID = i.categoryID
                    AND LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))))
    AND (:categoryId IS NULL OR i.categoryID = :categoryId)
    AND (:propertyType IS NULL OR LOWER(i.propertyType) = LOWER(:propertyType))
    AND (:minBudget IS NULL OR i.budget >= :minBudget)
    AND (:maxBudget IS NULL OR i.budget <= :maxBudget)
    ORDER BY i.createdAt DESC
    """)
List<IDP> searchPublishedWithFilters(
    @Param("keyword") String keyword,
    @Param("categoryId") Long categoryId,
    @Param("propertyType") String propertyType,
    @Param("minBudget") BigDecimal minBudget,
    @Param("maxBudget") BigDecimal maxBudget);
}