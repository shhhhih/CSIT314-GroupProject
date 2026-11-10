package com.team.idp.service;

import com.team.idp.dto.DesignerIdpRequest;
import com.team.idp.entity.Category;
import com.team.idp.entity.Company;
import com.team.idp.entity.IDP;
import com.team.idp.repository.CategoryRepository;
import com.team.idp.repository.CompanyRepository;
import com.team.idp.repository.FavouriteRepository;
import com.team.idp.repository.IDPRepository;
import com.team.idp.repository.IdpViewRepository;
import com.team.idp.repository.IDPImageRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesignerIDPServiceTest {
    @Mock IDPRepository idps;
    @Mock CompanyRepository companies;
    @Mock CategoryRepository categories;
    @Mock FavouriteRepository favourites;
    @Mock IdpViewRepository views;
    @Mock IDPImageRepository images;
    @InjectMocks DesignerIDPService service;

    private Company company() {
        Company c = new Company();
        ReflectionTestUtils.setField(c,"companyID",5L);
        c.setUserID(9L);
        c.setCompanyName("Demo Interior");
        return c;
    }
    private IDP project() {
        IDP p = new IDP(5L,1L,"Kitchen","Bright kitchen",new BigDecimal("25000"),"HDB","Published");
        ReflectionTestUtils.setField(p,"idpID",13L);
        return p;
    }

    @Test
    void designerCannotReadAnotherCompanyProject() {
        IDP p=project();p.setCompanyID(6L);
        when(idps.findById(13L)).thenReturn(Optional.of(p));
        when(companies.findByUserID(9L)).thenReturn(Optional.of(company()));
        assertThrows(SecurityException.class,()->service.view(9L,13L));
    }

    @Test
    void createRequiresCompany() {
        DesignerIdpRequest r = new DesignerIdpRequest("Kitchen","Bright kitchen",
            new BigDecimal("25000"),"HDB","Published",1L);
        assertThrows(IllegalStateException.class,()->service.create(9L,r));
        verify(idps,never()).save(any());
    }

    @Test
    void acceptsPublishedAndOngoingButRejectsCompleted() {
        DesignerIdpRequest completed = new DesignerIdpRequest("Kitchen","Bright kitchen",
            new BigDecimal("25000"),"HDB","Completed",1L);
        assertThrows(IllegalArgumentException.class,()->service.create(9L,completed));
        DesignerIdpRequest ongoing = new DesignerIdpRequest("Kitchen","Bright kitchen",
            new BigDecimal("25000"),"HDB","Ongoing",1L);
        assertThrows(IllegalStateException.class,()->service.create(9L,ongoing));
        verify(idps,never()).save(any());
    }

    @Test
    void listsOnlyDesignersCompanyProjects() {
        when(companies.findByUserID(9L)).thenReturn(Optional.of(company()));
        when(idps.findByCompanyID(5L)).thenReturn(List.of(project()));
        when(categories.findById(1L)).thenReturn(Optional.of(new Category("Japandi","Modern")));
        assertEquals(1, service.list(9L,"date","desc").size());
        verify(idps).findByCompanyID(5L);
    }
}
