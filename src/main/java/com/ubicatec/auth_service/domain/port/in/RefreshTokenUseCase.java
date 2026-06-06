package com.ubicatec.auth_service.domain.port.in;

import com.ubicatec.auth_service.domain.model.User;

public interface RefreshTokenUseCase {

    record TokenPair(
            String accessToken,
            String refreshToken,
            User user
    ) {}

    TokenPair refresh(String refreshToken);

}
