package com.ubicatec.auth_service.application.service;

import com.ubicatec.auth_service.domain.port.in.VerifyCodeUseCase;
import org.springframework.stereotype.Service;

@Service
public class VerifyCodeService implements VerifyCodeUseCase {

    @Override
    public TokenPair verifyCode(String email, String code) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }
}