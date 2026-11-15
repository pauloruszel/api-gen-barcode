package com.br.apigenbarcode.reception;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Order(-1)
@RestControllerAdvice(basePackageClasses = ReceptionController.class)
class ReceptionErrors {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> response(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of(
                "message", exception.getReason() == null ? "Requisição inválida." : exception.getReason()));
    }
}
