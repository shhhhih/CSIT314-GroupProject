package com.team.idp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

class LogoutControllerTest {

    private final LogoutController controller = new LogoutController();

    @Test
    void logoutEndsTheSessionAndReturnsOk() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 3L);
        request.setSession(session);

        ResponseEntity<?> response = controller.logout(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(Map.of("message", "Logged out"), response.getBody());
        assertTrue(session.isInvalid());
    }

    @Test
    void logoutWithoutSessionStillReturnsOk() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = controller.logout(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(Map.of("message", "Logged out"), response.getBody());
    }
}