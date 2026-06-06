package com.ubicatec.auth_service.infrastructure.adapter.in.web;


import com.ubicatec.auth_service.infrastructure.adapter.in.web.dto.OkResponse;
import com.ubicatec.auth_service.infrastructure.adapter.in.web.dto.SendCodeRequest;
import com.ubicatec.auth_service.infrastructure.adapter.in.web.dto.VerifyCodeRequest;
import com.ubicatec.auth_service.domain.port.in.*;
import com.ubicatec.auth_service.domain.port.out.TokenIssuerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
        sendCode.sendCode(req.email());
        return ResponseEntity.ok(new OkResponse("Código enviado", req.email()));
    }

    // 2. Verificar código → devuelve JWT
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest req) {
        var pair = verifyCode.verifyCode(req.email(), req.code());
        return ResponseEntity.ok(pair);
    }

    // 3. JWKS — llaves públicas para que otros MS validen JWT
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok(tokenIssuer.getJwks());
    }
}
