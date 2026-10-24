package com.team.idp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.team.idp.dto.CurrentUserResponse;
import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.repository.UserAccountRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

@ExtendWith(MockitoExtension.class)
class CurrentUserControllerTest {

    @Mock
    private UserAccountRepository accountRepository;

    @InjectMocks
    private CurrentUserController controller;

    private MockHttpServletRequest requestWithSession(Long userId, String profileName) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        if (userId != null) {
            session.setAttribute("userId", userId);
        }
        if (profileName != null) {
            session.setAttribute("profileName", profileName);
        }
        request.setSession(session);
        return request;
    }

    @Test
    void returnsCurrentUserWhenLoggedIn() {
        UserAccount account = new UserAccount("customer1", "customer1@test.com", "hashed", 3L, AccountStatus.ACTIVE);
        when(accountRepository.findById(3L)).thenReturn(Optional.of(account));

        ResponseEntity<?> response = controller.me(requestWithSession(3L, "Customer"));

        assertEquals(200, response.getStatusCode().value());
        CurrentUserResponse body = (CurrentUserResponse) response.getBody();
        assertEquals(3L, body.userId());
        assertEquals("customer1", body.username());
        assertEquals("Customer", body.profileName());
    }

    @Test
    void returns401WhenThereIsNoSession() {
        ResponseEntity<?> response = controller.me(new MockHttpServletRequest());

        assertEquals(401, response.getStatusCode().value());
        assertEquals(Map.of("message", "Please log in first"), response.getBody());
    }

    @Test
    void returns401WhenSessionHasNoUserId() {
        ResponseEntity<?> response = controller.me(requestWithSession(null, null));

        assertEquals(401, response.getStatusCode().value());
        assertEquals(Map.of("message", "Please log in first"), response.getBody());
    }

    @Test
    void returns401AndEndsSessionWhenAccountNoLongerExists() {
        when(accountRepository.findById(9L)).thenReturn(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 9L);
        session.setAttribute("profileName", "Customer");
        request.setSession(session);

        ResponseEntity<?> response = controller.me(request);

        assertEquals(401, response.getStatusCode().value());
        assertTrue(session.isInvalid());
    }
}