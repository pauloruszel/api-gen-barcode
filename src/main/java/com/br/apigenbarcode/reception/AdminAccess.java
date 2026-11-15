package com.br.apigenbarcode.reception;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
class AdminAccess {
    private final String adminKey;

    AdminAccess(@Value("${reception.admin-key}") String adminKey) {
        this.adminKey = adminKey;
    }

    boolean isAdmin(String header) {
        return adminKey.length() >= 24 && header != null && MessageDigest.isEqual(
                ("Bearer " + adminKey).getBytes(StandardCharsets.UTF_8),
                header.getBytes(StandardCharsets.UTF_8));
    }

    void requireAdmin(String header) {
        if (!isAdmin(header)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso de cerimonial necessário.");
        }
    }
}
