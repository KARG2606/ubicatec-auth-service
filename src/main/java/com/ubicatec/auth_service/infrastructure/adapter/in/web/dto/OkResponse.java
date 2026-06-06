package com.ubicatec.auth_service.infrastructure.adapter.in.web.dto;

public record OkResponse(
        String message,
        String email
) {}