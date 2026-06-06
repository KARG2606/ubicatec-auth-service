package com.ubicatec.auth_service.domain.model;

import java.time.Instant;
import java.util.UUID;

public record OtpChallenge(
        UUID id,
        String email,
        String codeHash,
        Instant expiresAt,
        int attempts,
        Instant consumedAt
) {
}