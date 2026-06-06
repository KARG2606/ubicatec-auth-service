package com.ubicatec.auth_service.infrastructure.adapter.out.persistence.repository;

import com.ubicatec.auth_service.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

}