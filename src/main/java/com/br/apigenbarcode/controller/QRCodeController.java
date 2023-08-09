package com.br.apigenbarcode.controller;

import com.br.apigenbarcode.record.QRCodeRequest;
import com.br.apigenbarcode.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.springframework.http.ContentDisposition.builder;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.ResponseEntity.ok;
import static reactor.core.publisher.Mono.just;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @GetMapping("/qrcode")
    public Mono<ResponseEntity<?>> generateQRCode(final QRCodeRequest request) throws Exception {
        log.info("Recebendo requisição para gerar QR Code");

        var qrCode = qrCodeService.generateQRCode(request.texto(), request.scale(), request.foreground(), request.background());

        if ("true".equalsIgnoreCase(request.base64())) {
            log.info("Retornando imagem em formato Base64");
            var base64Image = qrCodeService.encodeBase64(qrCode);
            var response = singletonMap("image", base64Image);
            return just(ResponseEntity.ok().contentType(APPLICATION_JSON).body(response));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(IMAGE_PNG);
        if ("true".equalsIgnoreCase(request.download())) {
            log.info("Configurando para download da imagem");
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename("qrcode.png").build());
        }

        log.info("Retornando imagem como PNG");
        return just(new ResponseEntity<>(qrCode, headers, OK));
    }
}
