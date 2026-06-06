-- Usuarios del sistema
CREATE TABLE users (
    id         UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    email      NVARCHAR(255) NOT NULL UNIQUE,
    role       NVARCHAR(20)  NOT NULL DEFAULT 'STUDENT',
    created_at DATETIME2     NOT NULL DEFAULT GETUTCDATE(),
    last_login_at DATETIME2
);

-- Códigos OTP de verificación
CREATE TABLE otp_challenges (
    id           UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    email        NVARCHAR(255) NOT NULL,
    code_hash    NVARCHAR(64)  NOT NULL,
    expires_at   DATETIME2     NOT NULL,
    attempts     INT           NOT NULL DEFAULT 0,
    consumed_at  DATETIME2     NULL,
    created_at   DATETIME2     NOT NULL DEFAULT GETUTCDATE()
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id          UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    user_id     UNIQUEIDENTIFIER NOT NULL REFERENCES users(id),
    token_hash  NVARCHAR(64)     NOT NULL UNIQUE,
    expires_at  DATETIME2        NOT NULL,
    revoked_at  DATETIME2        NULL,
    created_at  DATETIME2        NOT NULL DEFAULT GETUTCDATE()
);

CREATE INDEX idx_otp_email
ON otp_challenges(email, consumed_at, expires_at);

CREATE INDEX idx_refresh_hash
ON refresh_tokens(token_hash);