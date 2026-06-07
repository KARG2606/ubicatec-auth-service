package com.ubicatec.auth_service.infrastructure.adapter.out.security;

import com.ubicatec.auth_service.domain.model.User;
import com.ubicatec.auth_service.domain.port.out.TokenIssuerPort;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class RsaJwtIssuerAdapter implements TokenIssuerPort {

    private final KeyPair keyPair;
    private final String keyId = UUID.randomUUID().toString();

    public RsaJwtIssuerAdapter() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        this.keyPair = gen.generateKeyPair();
    }

    @Override
    public String issueAccessToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.id().toString())
                .claim("email", user.email())
                .claim("role", user.role().name())
                .issuer("ubicatec-auth")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(keyPair.getPrivate())
                .compact();
    }

    @Override
    public String issueRefreshToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.id().toString())
                .claim("type", "refresh")
                .issuer("ubicatec-auth")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(30L * 24 * 3600)))
                .signWith(keyPair.getPrivate())
                .compact();
    }

    @Override
    public String getJwks() {

        RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();

        String n = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(pub.getModulus().toByteArray());

        String e = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(pub.getPublicExponent().toByteArray());

        return String.format(
                "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\",\"kid\":\"%s\",\"n\":\"%s\",\"e\":\"%s\"}]}",
                keyId,
                n,
                e
        );
    }
    @Override
    public java.security.PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    @Override
    public String issueAccessTokenFromSubject(String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .issuer("ubicatec-auth")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(keyPair.getPrivate())
                .compact();
    }

}