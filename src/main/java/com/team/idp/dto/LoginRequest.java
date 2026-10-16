package com.team.idp.dto;

public record LoginRequest(String usernameOrEmail, String password) {
}