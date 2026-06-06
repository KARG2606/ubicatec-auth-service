package com.ubicatec.auth_service.domain.port.out;

import com.ubicatec.auth_service.domain.model.OtpChallenge;

import java.util.Optional;

public interface OtpRepositoryPort {

    OtpChallenge save(OtpChallenge challenge);

    Optional<OtpChallenge> findActiveByEmail(String email);

}