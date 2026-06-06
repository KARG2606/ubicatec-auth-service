package com.ubicatec.auth_service.infrastructure.adapter.out.persistence.repository;

import com.ubicatec.auth_service.infrastructure.adapter.out.persistence.entity.OtpChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataOtpRepository extends JpaRepository<OtpChallengeEntity, UUID> {

    Optional<OtpChallengeEntity> findTopByEmailOrderByExpiresAtDesc(String email);

}