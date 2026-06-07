package com.ubicatec.auth_service.infrastructure.adapter.in.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {

    private static final String APIM_CERT_THUMBPRINT =
            "F5DA71353562CD518FF7397D2DC307E299C3D806";

    @Override
    public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Validación mTLS — solo rechaza si hay cert y es inválido
        // Si no hay cert (ej: pruebas locales con mTLS en Ignorar), deja pasar
        String clientCert = http.getHeader("X-ARR-ClientCert");

        if (clientCert != null && !isValidApimCert(clientCert)) {
            response.sendError(403, "mTLS required: invalid certificate");
            return;
        }

        // Correlación de requests — propaga o genera X-Request-Id
        String corrId = http.getHeader("X-Request-Id");

        if (corrId == null || corrId.isBlank()) {
            corrId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", corrId);
        response.setHeader("X-Request-Id", corrId);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }

    private boolean isValidApimCert(String clientCertHeader) {
        try {
            // Azure envía el cert en Base64 (PEM sin headers)
            // Limpiamos saltos de línea y espacios por si acaso
            String cleaned = clientCertHeader
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s+", "");

            byte[] certBytes = Base64.getDecoder().decode(cleaned);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(
                    new java.io.ByteArrayInputStream(certBytes));

            // Calcular thumbprint SHA-1 del certificado
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] thumbprintBytes = md.digest(cert.getEncoded());

            // Convertir a hex string uppercase
            StringBuilder sb = new StringBuilder();
            for (byte b : thumbprintBytes) {
                sb.append(String.format("%02X", b));
            }
            String thumbprint = sb.toString();

            return APIM_CERT_THUMBPRINT.equalsIgnoreCase(thumbprint);

        } catch (Exception e) {
            // Si no se puede parsear el cert, rechazar
            return false;
        }
    }
}