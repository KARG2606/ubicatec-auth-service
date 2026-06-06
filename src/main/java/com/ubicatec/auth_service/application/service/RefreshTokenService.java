package com.ubicatec.auth_service.application.service;

import com.ubicatec.auth_service.domain.port.in.RefreshTokenUseCase;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    @Override
    public TokenPair refresh(String refreshToken) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}