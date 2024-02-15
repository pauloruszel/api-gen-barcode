package com.br.apigenbarcode.controller;

import com.br.apigenbarcode.domain.record.QRCodeRequest;
import com.br.apigenbarcode.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.http.ContentDisposition.attachment;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @GetMapping("/qrcode")
    public Mono<ResponseEntity<?>> generateQRCode(final QRCodeRequest request) throws Exception {
        log.info("recebendo a requisicao para gerar QR Code");

        log.info("inicio da geracao do QR Code com texto: {} scala: {} foreground: {} background: {}", request.texto(), request.scale(), request.foreground(), request.background());
        final var qrCode = qrCodeService.gerarQRCode(request.texto(), request.scale(), request.foreground(), request.background());
        log.info("fim da geracao do QR Code com texto: {} scala: {} foreground: {} background: {}", request.texto(), request.scale(), request.foreground(), request.background());

        final var isDownload = "true".equalsIgnoreCase(request.download());
        final var isBase64 = "true".equalsIgnoreCase(request.base64());

        return switch (isDownload + "|" + isBase64) {
            case "true|false" ->
                    just(ok().contentType(IMAGE_PNG).header(CONTENT_DISPOSITION, attachment().filename("qrcode.png").build().toString()).body(qrCode));
            case "false|true" ->
                    just(ok().contentType(APPLICATION_JSON).body(Map.of("image", qrCodeService.encodeBase64(qrCode))));
            case "false|false" ->
                    just(badRequest().body(Map.of("error", "Pelo menos uma das opções download ou base64 deve ser verdadeira.")));
            case "true|true" ->
                    just(badRequest().body(Map.of("error", "Apenas uma das opções download ou base64 deve ser verdadeira.")));
            default -> error(new IllegalStateException("Combinação inesperada de download e base64"));
        };

    }
}
