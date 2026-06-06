package com.ubicatec.auth_service.domain.port.in;

public interface SendCodeUseCase {

    void sendCode(String email);

}
