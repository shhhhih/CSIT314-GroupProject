package com.team.idp.controller;

import com.team.idp.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class CurrentUserController {

    private final UserAccountRepository accountRepository;

    public CurrentUserController(UserAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        // TODO: not implemented yet (red phase)
        return ResponseEntity.ok().build();
    }
}