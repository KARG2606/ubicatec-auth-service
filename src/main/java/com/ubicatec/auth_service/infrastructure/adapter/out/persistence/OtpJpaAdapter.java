package com.ubicatec.auth_service.infrastructure.adapter.out.persistence;

import com.ubicatec.auth_service.domain.model.OtpChallenge;
import com.ubicatec.auth_service.domain.port.out.OtpRepositoryPort;
import com.ubicatec.auth_service.infrastructure.adapter.out.persistence.entity.OtpChallengeEntity;
import com.ubicatec.auth_service.infrastructure.adapter.out.persistence.repository.SpringDataOtpRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class OtpJpaAdapter implements OtpRepositoryPort {

    private final SpringDataOtpRepository repository;

    public OtpJpaAdapter(SpringDataOtpRepository repository) {
        this.repository = repository;
    }

    @Override
    public OtpChallenge save(OtpChallenge challenge) {

        OtpChallengeEntity entity = toEntity(challenge);

        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<OtpChallenge> findActiveByEmail(String email) {

        return repository
                .findTopByEmailAndConsumedAtIsNullOrderByExpiresAtDesc(email)
                .filter(e -> e.getExpiresAt().isAfter(Instant.now()))
                .map(this::toDomain);
    }

    private OtpChallenge toDomain(OtpChallengeEntity entity) {

        return new OtpChallenge(
                entity.getId(),
                entity.getEmail(),
                entity.getCodeHash(),
                entity.getExpiresAt(),
                entity.getAttempts(),
                entity.getConsumedAt()
        );
    }

    private OtpChallengeEntity toEntity(OtpChallenge challenge) {

        OtpChallengeEntity entity = new OtpChallengeEntity();

        entity.setId(challenge.id());
        entity.setEmail(challenge.email());
        entity.setCodeHash(challenge.codeHash());
        entity.setExpiresAt(challenge.expiresAt());
        entity.setAttempts(challenge.attempts());
        entity.setConsumedAt(challenge.consumedAt());
        entity.setCreatedAt(Instant.now());

        return entity;
    }
}