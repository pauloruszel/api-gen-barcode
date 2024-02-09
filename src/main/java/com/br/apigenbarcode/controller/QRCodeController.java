package com.br.apigenbarcode.controller;

import com.br.apigenbarcode.record.QRCodeRequest;
import com.br.apigenbarcode.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonMap;
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
        log.info("recebendo a requisicao para gerar QR Code");

        log.info("inicio da geracao do QR Code com texto: {} scala: {} foreground: {} background: {}", request.texto(), request.scale(), request.foreground(), request.background());
        final var qrCode = qrCodeService.gerarQRCode(request.texto(), request.scale(), request.foreground(), request.background());
        log.info("fim da geracao do QR Code com texto: {} scala: {} foreground: {} background: {}", request.texto(), request.scale(), request.foreground(), request.background());

        final var isDownload = "true".equalsIgnoreCase(request.download());
        final var isBase64 = "true".equalsIgnoreCase(request.base64());

        if (!isDownload && !isBase64) {
            log.error("pelo menos uma das opcoes download ou base64 deve ser true.");
            return just(ok().contentType(APPLICATION_JSON).body(singletonMap("error", "Pelo menos uma das opções download ou base64 deve ser verdadeira.")));
        }

        if (isDownload && isBase64) {
            log.error("apenas uma das opcoes download ou base64 deve ser true.");
            return just(ok().contentType(APPLICATION_JSON).body(singletonMap("error", "Apenas uma das opções download ou base64 deve ser verdadeira.")));
        }

        if (isBase64) {
            final var base64Image = qrCodeService.encodeBase64(qrCode);
            log.info("Retornando a imagem em formato Base64");
            return just(ok().contentType(APPLICATION_JSON).body(singletonMap("image", base64Image)));
        } else {
            log.info("Configurando para download da imagem");
            final var headers = new HttpHeaders();
            headers.setContentType(IMAGE_PNG);
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename("qrcode.png").build());
            return just(new ResponseEntity<>(qrCode, headers, OK));
        }
    }
}
