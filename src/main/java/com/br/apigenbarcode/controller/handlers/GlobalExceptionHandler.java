package com.br.apigenbarcode.controller.handlers;

import com.br.apigenbarcode.domain.exception.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex, ServerHttpRequest request) {
        log.error("IllegalArgumentException", ex);

        var errorTitle = "";
        if (ex.getMessage().contains("nulo ou vazio")) {
            errorTitle = "Dados de Entrada Inválidos";
        } else if (ex.getMessage().contains("nulo ou invalido")) {
            errorTitle = "E-mail Inválido";
        } else {
            errorTitle = "Erro de Validação";
        }

        final var apiError = ApiError.builder()
                .type("validation_error")
                .status(BAD_REQUEST.value())
                .instance(request.getURI().getPath())
                .title(errorTitle)
                .detail(ex.getMessage())
                .errors(Collections.singletonList(ex.getMessage()))
                .build();

        return ResponseEntity.status(BAD_REQUEST).body(apiError);
    }
}