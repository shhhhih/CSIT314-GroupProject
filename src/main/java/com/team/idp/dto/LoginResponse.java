package com.team.idp.dto;

public record LoginResponse(Long userId, String username, String profileName, String redirectUrl) {
}