package com.team.idp.service;

import com.team.idp.entity.Category;
import com.team.idp.repository.CategoryRepository;
import com.team.idp.repository.IDPRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceDeletionTest {
    @Mock CategoryRepository categories;
    @Mock IDPRepository idps;
    @InjectMocks CategoryService service;

    @Test
    void cannotDeleteCategoryUsedByAnyIdp() {
        Category category = new Category("Japandi", "Japandi styles");
        when(categories.findById(4L)).thenReturn(Optional.of(category));
        when(idps.existsByCategoryID(4L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.deleteCategory(4L));
        verify(categories, never()).delete(any());
    }

    @Test
    void canDeleteUnusedCategory() {
        Category category = new Category("Japandi", "Japandi styles");
        when(categories.findById(4L)).thenReturn(Optional.of(category));
        service.deleteCategory(4L);
        verify(categories).delete(category);
    }
}
