package com.ubicatec.auth_service.domain.port.out;

import com.ubicatec.auth_service.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);

    User save(User user);

}
