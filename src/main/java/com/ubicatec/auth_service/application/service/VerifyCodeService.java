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

        System.out.println("=== VERIFY START ===");
        System.out.println("Email recibido: " + email);
        System.out.println("Código recibido: " + code);

        // 1. Buscar OTP activo para ese email
        OtpChallenge otp = otpRepo.findActiveByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay un código activo para ese correo"));

        System.out.println("OTP encontrado: " + otp.id());

        // 2. Verificar que no está expirado
        if (Instant.now().isAfter(otp.expiresAt())) {
            System.out.println("OTP expirado");
            throw new IllegalArgumentException("El código ha expirado");
        }

        System.out.println("OTP no expirado");

        // 3. Verificar que no está ya consumido
        if (otp.consumedAt() != null) {
            System.out.println("OTP ya consumido");
            throw new IllegalArgumentException("El código ya fue utilizado");
        }

        System.out.println("OTP no consumido");

        // 4. Verificar que no excedió los intentos máximos
        if (otp.attempts() >= MAX_ATTEMPTS) {
            System.out.println("Máximo de intentos excedido");
            throw new IllegalArgumentException("Demasiados intentos fallidos");
        }

        System.out.println("Intentos válidos");

        // 5. Comparar el hash del código ingresado
        String inputHash = sha256(code);

        System.out.println("Hash ingresado: " + inputHash);
        System.out.println("Hash guardado : " + otp.codeHash());

        if (!inputHash.equals(otp.codeHash())) {

            System.out.println("Código inválido");

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

        System.out.println("Código válido");

        // 6. Marcar OTP como consumido
        otpRepo.save(new OtpChallenge(
                otp.id(),
                otp.email(),
                otp.codeHash(),
                otp.expiresAt(),
                otp.attempts(),
                Instant.now()
        ));

        System.out.println("OTP marcado como consumido");

        // 7. Obtener el usuario
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario no encontrado para el email: " + email));

        System.out.println("Usuario encontrado: " + user.email());

        // 8. Emitir tokens JWT
        System.out.println("Generando access token...");
        String accessToken = tokenIssuer.issueAccessToken(user);

        System.out.println("Access token generado");

        System.out.println("Generando refresh token...");
        String refreshToken = tokenIssuer.issueRefreshToken(user);

        System.out.println("Refresh token generado");

        System.out.println("=== VERIFY END ===");

        return new TokenPair(accessToken, refreshToken, user);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}