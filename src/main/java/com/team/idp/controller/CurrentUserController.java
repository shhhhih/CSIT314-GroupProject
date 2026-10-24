package com.team.idp.controller;

import com.team.idp.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.team.idp.dto.CurrentUserResponse;
import com.team.idp.entity.UserAccount;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class CurrentUserController {

    private final UserAccountRepository accountRepository;

    public CurrentUserController(UserAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long userId)) {
            return unauthorised();
        }

        Optional<UserAccount> account = accountRepository.findById(userId);
        if (account.isEmpty()) {
            session.invalidate();
            return unauthorised();
        }

        String profileName = (String) session.getAttribute("profileName");
        return ResponseEntity.ok(new CurrentUserResponse(userId, account.get().getUsername(), profileName));
    }

    private ResponseEntity<?> unauthorised() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Please log in first"));
    }
}