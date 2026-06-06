package com.ubicatec.auth_service.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendCodeRequest(
        @Email
        @NotBlank
        String email
) {}