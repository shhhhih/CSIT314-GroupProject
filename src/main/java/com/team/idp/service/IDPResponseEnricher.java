package com.team.idp.service;

import com.team.idp.dto.IdpDetailsResponse;
import com.team.idp.dto.IdpSummaryResponse;
import com.team.idp.entity.Category;
import com.team.idp.entity.Company;
import com.team.idp.entity.IDPImage;
import com.team.idp.repository.CategoryRepository;
import com.team.idp.repository.CompanyRepository;
import com.team.idp.repository.IDPImageRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IDPResponseEnricher {
    private final CategoryRepository categories;
    private final CompanyRepository companies;
    private final IDPImageRepository images;
    public IDPResponseEnricher(CategoryRepository categories, CompanyRepository companies,
                               IDPImageRepository images) {
        this.categories = categories;
        this.companies = companies;
        this.images = images;
    }
    public IdpSummaryResponse summary(IdpSummaryResponse r) {
        String name = categories.findById(r.categoryId()).map(Category::getCategoryName).orElse("Uncategorised");
        String image = IDPPhotoOrder.coverFirst(images.findByIdpIDOrderByImageIDAsc(r.idpId())).stream()
                .map(IDPImage::getImageUrl).findFirst().orElse(null);
        return new IdpSummaryResponse(r.idpId(),r.title(),r.categoryId(),name,r.propertyType(),
                r.projectStatus(),r.budget(),image,r.viewCount(),r.shortlistCount(),r.createdAt());
    }
    public IdpDetailsResponse details(IdpDetailsResponse r) {
        String category = categories.findById(r.categoryId()).map(Category::getCategoryName).orElse("Uncategorised");
        String company = companies.findById(r.companyId()).map(Company::getCompanyName).orElse("Unknown studio");
        List<String> urls = IDPPhotoOrder.coverFirst(images.findByIdpIDOrderByImageIDAsc(r.idpId())).stream()
                .map(IDPImage::getImageUrl).toList();
        return new IdpDetailsResponse(r.idpId(),r.title(),r.categoryId(),category,r.propertyType(),
                r.projectStatus(),r.budget(),urls.isEmpty()?null:urls.get(0),
                r.viewCount(),r.shortlistCount(),r.createdAt(),r.description(),r.companyId(),company,urls,r.updatedAt());
    }
}
