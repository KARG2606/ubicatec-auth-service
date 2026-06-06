package com.ubicatec.auth_service.domain.port.in;

import com.ubicatec.auth_service.domain.model.User;

public interface VerifyCodeUseCase {

    record TokenPair(
            String accessToken,
            String refreshToken,
            User user
    ) {}

    TokenPair verifyCode(String email, String code);

}