package com.ubicatec.auth_service.infrastructure.adapter.out.persistence;

import com.ubicatec.auth_service.domain.model.User;
import com.ubicatec.auth_service.domain.port.out.UserRepositoryPort;
import com.ubicatec.auth_service.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.ubicatec.auth_service.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserJpaAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public UserJpaAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        return toDomain(repository.save(entity));
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getRole(),
                entity.getCreatedAt(),
                entity.getLastLoginAt()
        );
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();

        entity.setId(user.id());
        entity.setEmail(user.email());
        entity.setRole(user.role());
        entity.setCreatedAt(user.createdAt());
        entity.setLastLoginAt(user.lastLoginAt());

        return entity;
    }
}