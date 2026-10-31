package com.team.idp.dto;

import com.team.idp.entity.IDP;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** "IDP details" object from API.md section 6 (summary fields plus details). */
public record IdpDetailsResponse(Long idpId, String title, Long categoryId, String categoryName,
                                 String propertyType, String projectStatus, BigDecimal budget,
                                 String coverImage, int viewCount, int shortlistCount,
                                 LocalDateTime createdAt, String description, Long companyId,
                                 String companyName, List<String> images, LocalDateTime updatedAt) {

    // categoryName, companyName and images are filled in once Category, Company and image upload exist.
    public static IdpDetailsResponse from(IDP idp) {
        return new IdpDetailsResponse(idp.getIdpID(), idp.getTitle(), idp.getCategoryID(), null,
                idp.getPropertyType(), idp.getProjectStatus(), idp.getBudget(),
                null, idp.getViewCount(), idp.getShortlistCount(), idp.getCreatedAt(),
                idp.getDescription(), idp.getCompanyID(), null, List.of(), idp.getUpdatedAt());
    }
}
