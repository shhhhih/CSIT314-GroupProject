package com.team.idp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.team.idp.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByCategoryNameContainingIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCase(String categoryName);
}