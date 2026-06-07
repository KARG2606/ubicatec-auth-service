package com.ubicatec.auth_service.application.service;

import com.ubicatec.auth_service.domain.model.OtpChallenge;
import com.ubicatec.auth_service.domain.port.in.SendCodeUseCase;
import com.ubicatec.auth_service.domain.port.out.EmailNotifierPort;
import com.ubicatec.auth_service.domain.port.out.OtpRepositoryPort;
import com.ubicatec.auth_service.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
public class SendCodeService implements SendCodeUseCase {

    private static final String[] ALLOWED = {"estudiantec.cr", "itcr.ac.cr"};
    private static final int OTP_TTL_MIN = 10;

    private final OtpRepositoryPort otpRepo;
    private final UserRepositoryPort userRepo;
    private final EmailNotifierPort emailNotifier;

    public SendCodeService(OtpRepositoryPort otpRepo,
                           UserRepositoryPort userRepo,
                           EmailNotifierPort emailNotifier) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.emailNotifier = emailNotifier;
    }

    @Override
    public void sendCode(String email) {
        // 1. Validar dominio institucional
        String domain = email.substring(email.indexOf('@') + 1);
        boolean allowed = false;
        for (String d : ALLOWED) { if (d.equals(domain)) { allowed = true; break; } }
        if (!allowed) throw new IllegalArgumentException("Correo no institucional");

        // 2. Crear usuario si no existe
        userRepo.findByEmail(email).orElseGet(() ->
                userRepo.save(new com.ubicatec.auth_service.domain.model.User(
                        UUID.randomUUID(), email,
                        com.ubicatec.auth_service.domain.model.Role.STUDENT,
                        Instant.now(), null)));

        // 3. Generar código OTP de 6 dígitos
        String code = String.format("%06d",
                new SecureRandom().nextInt(999999));

        // 4. Hash SHA-256 del código
        String hash = sha256(code);

        // 5. Persistir el OTP
        otpRepo.save(new OtpChallenge(UUID.randomUUID(), email, hash,
                Instant.now().plusSeconds(OTP_TTL_MIN * 60), 0, null));

        // 6. Enviar por email (via n8n webhook)
        //emailNotifier.sendOtp(email, code, OTP_TTL_MIN);
    }

    private String sha256(String input) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            var sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}

