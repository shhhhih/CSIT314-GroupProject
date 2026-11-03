package com.team.idp.service;

import com.team.idp.dto.DesignerIdpRequest;
import com.team.idp.dto.IdpDetailsResponse;
import com.team.idp.dto.IdpSummaryResponse;
import com.team.idp.entity.Category;
import com.team.idp.entity.Company;
import com.team.idp.entity.IDP;
import com.team.idp.entity.IDPImage;
import com.team.idp.repository.CategoryRepository;
import com.team.idp.repository.CompanyRepository;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.FavouriteRepository;
import com.team.idp.repository.IdpViewRepository;
import com.team.idp.repository.IDPImageRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesignerIDPService {
    private final IDPRepository idps;
    private final CompanyRepository companies;
    private final CategoryRepository categories;
    private final FavouriteRepository favourites;
    private final IdpViewRepository views;
    private final IDPImageRepository images;

    public DesignerIDPService(IDPRepository idps, CompanyRepository companies,
                              CategoryRepository categories, FavouriteRepository favourites,
                              IdpViewRepository views, IDPImageRepository images) {
        this.idps = idps;
        this.companies = companies;
        this.categories = categories;
        this.favourites = favourites;
        this.views = views;
        this.images = images;
    }

    private Company company(Long userId) {
        return companies.findByUserID(userId)
                .orElseThrow(() -> new IllegalStateException("Please save your company information first"));
    }

    private IDP own(Long userId, Long id) {
        IDP idp = idps.findById(id).orElseThrow(() -> new NoSuchElementException("IDP not found"));
        if (!idp.getCompanyID().equals(company(userId).getCompanyID())) {
            throw new SecurityException("This IDP does not belong to you");
        }
        return idp;
    }

    private Category category(Long categoryId) {
        if (categoryId == null) throw new IllegalArgumentException("Select a category");
        return categories.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
    }

    private void validate(DesignerIdpRequest r) {
        if (r == null) throw new IllegalArgumentException("Project details are required");
        if (r.title() == null || r.title().isBlank() || r.title().length() > 150)
            throw new IllegalArgumentException("Title is required (maximum 150 characters)");
        if (r.description() == null || r.description().isBlank() || r.description().length() > 5000)
            throw new IllegalArgumentException("Description is required (maximum 5000 characters)");
        if (r.budget() == null || r.budget().compareTo(BigDecimal.ZERO) <= 0 ||
                r.budget().compareTo(new BigDecimal("9999999999.99")) > 0)
            throw new IllegalArgumentException("Budget must be greater than 0 and within range");
        if (r.propertyType() == null || !List.of("HDB", "Condo", "Landed", "Commercial").contains(r.propertyType()))
            throw new IllegalArgumentException("Select a valid property type");
        if (r.projectStatus() == null || !List.of("Draft", "Published").contains(r.projectStatus()))
            throw new IllegalArgumentException("Select a valid publication status");
    }

    private IdpSummaryResponse summary(IDP idp) {
        String categoryName = categories.findById(idp.getCategoryID())
                .map(Category::getCategoryName).orElse("Uncategorised");
        String cover = IDPPhotoOrder.coverFirst(images.findByIdpIDOrderByImageIDAsc(idp.getIdpID())).stream()
                .map(IDPImage::getImageUrl).findFirst().orElse(null);
        return new IdpSummaryResponse(idp.getIdpID(), idp.getTitle(), idp.getCategoryID(),
                categoryName, idp.getPropertyType(), idp.getProjectStatus(), idp.getBudget(),
                cover, idp.getViewCount(), idp.getShortlistCount(), idp.getCreatedAt());
    }

    private IdpDetailsResponse details(IDP idp) {
        String categoryName = categories.findById(idp.getCategoryID())
                .map(Category::getCategoryName).orElse("Uncategorised");
        String companyName = companies.findById(idp.getCompanyID())
                .map(Company::getCompanyName).orElse("Unknown studio");
        List<String> urls = IDPPhotoOrder.coverFirst(images.findByIdpIDOrderByImageIDAsc(idp.getIdpID())).stream()
                .map(IDPImage::getImageUrl).toList();
        return new IdpDetailsResponse(idp.getIdpID(), idp.getTitle(), idp.getCategoryID(),
                categoryName, idp.getPropertyType(), idp.getProjectStatus(), idp.getBudget(),
                urls.isEmpty() ? null : urls.get(0), idp.getViewCount(), idp.getShortlistCount(), idp.getCreatedAt(),
                idp.getDescription(), idp.getCompanyID(), companyName,
                urls, idp.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public List<IdpSummaryResponse> list(Long userId, String sort, String order) {
        if (!List.of("date", "title").contains(sort)) throw new IllegalArgumentException("Invalid sort option");
        if (!List.of("asc", "desc").contains(order)) throw new IllegalArgumentException("Invalid order option");
        Comparator<IDP> comparator = "title".equals(sort)
                ? Comparator.comparing(IDP::getTitle, String.CASE_INSENSITIVE_ORDER)
                : Comparator.comparing(IDP::getCreatedAt);
        if ("desc".equals(order)) comparator = comparator.reversed();
        return idps.findByCompanyID(company(userId).getCompanyID()).stream()
                .sorted(comparator).map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public IdpDetailsResponse view(Long userId, Long id) { return details(own(userId,id)); }

    @Transactional
    public IdpDetailsResponse create(Long userId, DesignerIdpRequest request) {
        validate(request);
        Company company = company(userId);
        Category category = category(request.categoryId());
        IDP idp = new IDP(company.getCompanyID(), category.getCategoryID(), request.title().trim(),
                request.description().trim(), request.budget(), request.propertyType(), request.projectStatus());
        return details(idps.save(idp));
    }

    @Transactional
    public IdpDetailsResponse update(Long userId, Long id, DesignerIdpRequest request) {
        validate(request);
        IDP idp = own(userId,id);
        Category category = category(request.categoryId());
        idp.setTitle(request.title().trim());
        idp.setDescription(request.description().trim());
        idp.setBudget(request.budget());
        idp.setPropertyType(request.propertyType());
        idp.setProjectStatus(request.projectStatus());
        idp.setCategoryID(category.getCategoryID());
        return details(idps.save(idp));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        IDP idp = own(userId,id);
        // Dependent rows must be cleaned up before removing the project.
        favourites.deleteAll(favourites.findByIdpID(id));
        views.deleteAll(views.findByIdpID(id));
        images.deleteByIdpID(id);
        idps.delete(idp);
    }
}
