package com.team.idp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.team.idp.dto.ProfileStatusRequest;
import com.team.idp.dto.UserProfileRequest;
import com.team.idp.dto.UserProfileResponse;
import com.team.idp.service.ConflictException;
import com.team.idp.service.ProfileManagementService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

@ExtendWith(MockitoExtension.class)
class ProfileManagementControllerTest {

    @Mock
    private ProfileManagementService service;

    @InjectMocks
    private ProfileManagementController controller;

    private MockHttpServletRequest requestAs(String profileName) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("profileName", profileName);
        request.setSession(session);
        return request;
    }

    private UserProfileResponse sample() {
        return new UserProfileResponse(1L, "Customer", "Searches", "ACTIVE",
                LocalDateTime.of(2026, 10, 1, 9, 0));
    }

    @Test
    void listWithoutLoginReturns401() {
        ResponseEntity<?> response = controller.list(null, new MockHttpServletRequest());

        assertEquals(401, response.getStatusCode().value());
        assertEquals(Map.of("message", "Please log in first"), response.getBody());
        verifyNoInteractions(service);
    }

    @Test
    void listByNonAdminReturns403() {
        ResponseEntity<?> response = controller.list(null, requestAs("Customer"));

        assertEquals(403, response.getStatusCode().value());
        assertEquals(Map.of("message", "You do not have permission to do this"), response.getBody());
        verifyNoInteractions(service);
    }

    @Test
    void listByAdminReturnsProfiles() {
        when(service.search("cust")).thenReturn(List.of(sample()));

        ResponseEntity<?> response = controller.list("cust", requestAs("User Admin"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(sample()), response.getBody());
    }

    @Test
    void viewByAdminReturnsTheProfile() {
        when(service.view(1L)).thenReturn(sample());

        ResponseEntity<?> response = controller.view(1L, requestAs("User Admin"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(sample(), response.getBody());
    }

    @Test
    void createByAdminReturns201() {
        UserProfileRequest body = new UserProfileRequest("Interior Decorator", "Decorates");
        when(service.create(body)).thenReturn(sample());

        ResponseEntity<?> response = controller.create(body, requestAs("User Admin"));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(sample(), response.getBody());
    }

    @Test
    void createByNonAdminReturns403() {
        ResponseEntity<?> response = controller.create(
                new UserProfileRequest("X", "y"), requestAs("Interior Designer"));

        assertEquals(403, response.getStatusCode().value());
        verifyNoInteractions(service);
    }

    @Test
    void updateByAdminReturnsTheProfile() {
        UserProfileRequest body = new UserProfileRequest("Home Owner", "new");
        when(service.update(1L, body)).thenReturn(sample());

        ResponseEntity<?> response = controller.update(1L, body, requestAs("User Admin"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(sample(), response.getBody());
    }

    @Test
    void changeStatusByAdminReturnsTheProfile() {
        ProfileStatusRequest body = new ProfileStatusRequest("SUSPENDED");
        when(service.changeStatus(1L, body)).thenReturn(sample());

        ResponseEntity<?> response = controller.changeStatus(1L, body, requestAs("User Admin"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(sample(), response.getBody());
    }

    @Test
    void errorHandlersUseTheAgreedStatusCodesAndMessages() {
        ResponseEntity<?> invalid = controller.invalidInput(new IllegalArgumentException("Profile name is required"));
        ResponseEntity<?> missing = controller.notFound(new NoSuchElementException("Profile not found"));
        ResponseEntity<?> conflict = controller.conflict(new ConflictException("Profile name already exists"));

        assertEquals(400, invalid.getStatusCode().value());
        assertEquals(Map.of("message", "Profile name is required"), invalid.getBody());
        assertEquals(404, missing.getStatusCode().value());
        assertEquals(Map.of("message", "Profile not found"), missing.getBody());
        assertEquals(409, conflict.getStatusCode().value());
        assertEquals(Map.of("message", "Profile name already exists"), conflict.getBody());
    }
}