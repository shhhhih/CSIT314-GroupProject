package com.team.idp.controller;

import com.team.idp.dto.LoginRequest;
import com.team.idp.dto.LoginResponse;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        if (request == null || isBlank(request.usernameOrEmail()) || isBlank(request.password())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please enter your username and password"));
        }

        UserAccount account = loginService.authenticate(request.usernameOrEmail(), request.password());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        UserProfile profile = loginService.getUserProfile(account.getProfileID()).orElse(null);
        if (!loginService.isActive(account, profile)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Your account is not active. Please contact the administrator."));
        }

        startSession(httpRequest, account, profile);

        return ResponseEntity.ok(new LoginResponse(
                account.getUserID(),
                account.getUsername(),
                profile.getProfileName(),
                loginService.getRedirectUrl(profile.getProfileName())));
    }

    private void startSession(HttpServletRequest httpRequest, UserAccount account, UserProfile profile) {
        HttpSession old = httpRequest.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("userId", account.getUserID());
        session.setAttribute("profileName", profile.getProfileName());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}