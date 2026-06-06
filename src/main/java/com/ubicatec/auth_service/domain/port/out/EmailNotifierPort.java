package com.ubicatec.auth_service.domain.port.out;

public interface EmailNotifierPort {

    void sendOtp(
            String to,
            String code,
            int expiresInMinutes
    );

}
