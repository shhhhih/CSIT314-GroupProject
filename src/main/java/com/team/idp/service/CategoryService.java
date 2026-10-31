package com.team.idp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.idp.entity.Category;
import com.team.idp.repository.CategoryRepository;
import com.team.idp.repository.IDPRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final IDPRepository idpRepository;

    public CategoryService(CategoryRepository categoryRepository, IDPRepository idpRepository) {
        this.categoryRepository = categoryRepository;
        this.idpRepository = idpRepository;
    }

    public Category createCategory(Category category) {
        if (category.getCategoryName() == null || category.getCategoryName().isEmpty()) {
            throw new IllegalArgumentException("Please fill in all required fields");
        }
        String name = category.getCategoryName().trim();

        if(categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category already exists");
        }
        category.setCategoryName(name);
        category.setCreatedAt(null);

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id,Category updatedCategory) {

        Category existingCategory = categoryRepository.findById(id).orElse(null);
        if (existingCategory == null) {
            throw new IllegalArgumentException("Category not found");
        }

        if (updatedCategory.getCategoryName() == null || updatedCategory.getCategoryName().isEmpty()) {
            throw new IllegalArgumentException("Please fill in all required fields");
        }

        String newName = updatedCategory.getCategoryName().trim();
        boolean duplicateExists = categoryRepository.existsByCategoryNameIgnoreCase(newName);
        boolean sameAsCurrentName = existingCategory.getCategoryName().equalsIgnoreCase(newName);

        if (duplicateExists && !sameAsCurrentName) {
            throw new IllegalArgumentException("Category already exists");
        }
        existingCategory.setCategoryName(newName);
        existingCategory.setDescription(updatedCategory.getDescription());

        return categoryRepository.save(existingCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }
        if (idpRepository.existsById(id)) {
            throw new IllegalArgumentException("Category cannot be deleted because it is being used by an IDP");
        }

        categoryRepository.delete(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findByCategoryNameContainingIgnoreCase(keyword.trim());
    }

    public Category getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }
        return category;
    }
}