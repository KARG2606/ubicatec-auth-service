package com.ubicatec.auth_service.domain.port.out;

import com.ubicatec.auth_service.domain.model.User;

public interface TokenIssuerPort {

    String issueAccessToken(User user);

    String issueRefreshToken(User user);

    String getJwks();

}
