package com.team.idp.dto;

import com.team.idp.entity.IDP;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** "IDP summary" object from API.md section 6. */
public record IdpSummaryResponse(Long idpId, String title, Long categoryId, String categoryName,
                                 String propertyType, String projectStatus, BigDecimal budget,
                                 String coverImage, int viewCount, int shortlistCount,
                                 LocalDateTime createdAt) {

    // categoryName and coverImage stay null until B2's Category and B3's image upload exist.
    public static IdpSummaryResponse from(IDP idp) {
        return new IdpSummaryResponse(idp.getIdpID(), idp.getTitle(), idp.getCategoryID(), null,
                idp.getPropertyType(), idp.getProjectStatus(), idp.getBudget(),
                null, idp.getViewCount(), idp.getShortlistCount(), idp.getCreatedAt());
    }
}
