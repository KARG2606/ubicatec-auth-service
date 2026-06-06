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
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {

    private static final String APIM_THUMBPRINT =
            "F5DA71353562CD518FF7397D2DC307E299C3D806";

    @Override
    public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Validación mTLS
        String clientCert = http.getHeader("X-ARR-ClientCert");

        if (clientCert == null || !isValidApimCert(clientCert)) {
            response.sendError(403, "mTLS required");
            return;
        }

        String corrId = http.getHeader("X-Request-Id");

        if (corrId == null) {
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

    private boolean isValidApimCert(String clientCert) {
        // TODO: implementar validación real del thumbprint
        return true;
    }
}