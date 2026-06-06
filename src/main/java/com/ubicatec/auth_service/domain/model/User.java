package com.ubicatec.auth_service.domain.model;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String email,
        Role role,
        Instant createdAt,
        Instant lastLoginAt
) {
}