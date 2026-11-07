
package com.team.idp.dto;

public record CustomerRegisterRequest(
        String username,
        String email,
        String password,
        String confirmPassword
) {
}
