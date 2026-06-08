package com.ubicatec.auth_service.infrastructure.adapter.in.web;


import com.ubicatec.auth_service.infrastructure.adapter.in.web.dto.OkResponse;
import com.ubicatec.auth_service.infrastructure.adapter.in.web.dto.SendCodeRequest;
import com.ubicatec.auth_service.infrastructure.adapter.in.web.dto.VerifyCodeRequest;
import com.ubicatec.auth_service.domain.port.in.*;
import com.ubicatec.auth_service.domain.port.out.TokenIssuerPort;
import io.jsonwebtoken.Jwts;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final SendCodeUseCase sendCode;
    private final VerifyCodeUseCase verifyCode;
    private final TokenIssuerPort tokenIssuer;

    public AuthController(SendCodeUseCase sendCode,
                          VerifyCodeUseCase verifyCode,
                          TokenIssuerPort tokenIssuer) {
        this.sendCode = sendCode;
        this.verifyCode = verifyCode;
        this.tokenIssuer = tokenIssuer;
    }


    // 1. Enviar código OTP
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@Valid @RequestBody SendCodeRequest req) {
        System.out.println("ENTRÓ AL SEND-CODE CONTROLLER");
        sendCode.sendCode(req.email());
        return ResponseEntity.ok(new OkResponse("Código enviado", req.email()));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest req) {

        verifyCode.verifyCode(req.email(), req.code());

        return ResponseEntity.ok(
                Map.of(
                        "status", "verify ejecutado"
                )
        );
    }

    // 4. Refresh — renueva el access token usando el refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token requerido"));
        }
        String refreshToken = authHeader.substring(7);
        try {
            var claims = Jwts.parser()
                    .verifyWith((java.security.interfaces.RSAPublicKey) tokenIssuer.getPublicKey())
                    .build()
                    .parseSignedClaims(refreshToken);
            if (!"refresh".equals(claims.getPayload().get("type"))) {
                return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
            }
            // Emitir nuevo access token con los datos del subject
            String newAccessToken = tokenIssuer.issueAccessTokenFromSubject(claims.getPayload().getSubject());
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token expirado o inválido"));
        }
    }

    // 5. Logout — invalida la sesión (cliente debe borrar sus tokens)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // En esta implementación stateless, el logout lo maneja el cliente
        // borrando sus tokens. El servidor confirma la operación.
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    // 3. JWKS — llaves públicas para que otros MS validen JWT
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok(tokenIssuer.getJwks());
    }
}
