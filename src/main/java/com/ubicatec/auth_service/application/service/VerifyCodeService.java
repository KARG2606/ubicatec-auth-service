package com.ubicatec.auth_service.application.service;

import com.ubicatec.auth_service.domain.model.OtpChallenge;
import com.ubicatec.auth_service.domain.model.User;
import com.ubicatec.auth_service.domain.port.in.VerifyCodeUseCase;
import com.ubicatec.auth_service.domain.port.out.OtpRepositoryPort;
import com.ubicatec.auth_service.domain.port.out.TokenIssuerPort;
import com.ubicatec.auth_service.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;

@Service
public class VerifyCodeService implements VerifyCodeUseCase {

    private static final int MAX_ATTEMPTS = 5;

    private final OtpRepositoryPort otpRepo;
    private final UserRepositoryPort userRepo;
    private final TokenIssuerPort tokenIssuer;

    public VerifyCodeService(OtpRepositoryPort otpRepo,
                             UserRepositoryPort userRepo,
                             TokenIssuerPort tokenIssuer) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public TokenPair verifyCode(String email, String code) {

        // 1. Buscar OTP activo para ese email
        OtpChallenge otp = otpRepo.findActiveByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay un código activo para ese correo"));

        // 2. Verificar que no está expirado
        if (Instant.now().isAfter(otp.expiresAt())) {
            throw new IllegalArgumentException("El código ha expirado");
        }

        // 3. Verificar que no está ya consumido
        if (otp.consumedAt() != null) {
            throw new IllegalArgumentException("El código ya fue utilizado");
        }

        // 4. Verificar que no excedió los intentos máximos
        if (otp.attempts() >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException("Demasiados intentos fallidos");
        }

        // 5. Comparar el hash del código ingresado
        String inputHash = sha256(code);
        if (!inputHash.equals(otp.codeHash())) {
            // Incrementar intentos fallidos
            otpRepo.save(new OtpChallenge(
                    otp.id(),
                    otp.email(),
                    otp.codeHash(),
                    otp.expiresAt(),
                    otp.attempts() + 1,
                    otp.consumedAt()
            ));
            throw new IllegalArgumentException("Código inválido");
        }

        // 6. Marcar OTP como consumido
        otpRepo.save(new OtpChallenge(
                otp.id(),
                otp.email(),
                otp.codeHash(),
                otp.expiresAt(),
                otp.attempts(),
                Instant.now()
        ));

        // 7. Obtener el usuario
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario no encontrado para el email: " + email));

        // 8. Emitir tokens JWT
        String accessToken = tokenIssuer.issueAccessToken(user);
        String refreshToken = tokenIssuer.issueRefreshToken(user);

        return new TokenPair(accessToken, refreshToken, user);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}