package com.br.apigenbarcode.service;

import com.br.apigenbarcode.entity.Convidado;
import com.br.apigenbarcode.repository.ConvidadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class ConvidadoService {

    @Autowired
    private ConvidadoRepository convidadoRepository;

    @Autowired
    private QRCodeService qrCodeService;

    public Mono<Convidado> cadastrarConvidado(final String nome, final String email) {
        final var idUnico = gerarIdUnico();

        final var convidado = Convidado.builder()
                .nome(nome)
                .email(email)
                .idUnico(idUnico)
                .status("Não").build();

        return gerarQrCodeBase64(idUnico)
                .flatMap(qrCodeBase64 -> {
                    convidado.setQrCode(qrCodeBase64);
                    return convidadoRepository.save(convidado);
                });
    }

    private String gerarIdUnico() {
        return UUID.randomUUID().toString();
    }

    private Mono<String> gerarQrCodeBase64(final String idUnico) {
        final int scale = 200; // Tamanho do QR Code
        final String foreground = "#000000"; // Cor do QR Code
        final String background = "#FFFFFF"; // Cor de fundo
        return qrCodeService.gerarQRCodeReativo(idUnico, scale, foreground, background)
                .map(qrCode -> qrCodeService.encodeBase64(qrCode));
    }

    public Mono<Boolean> confirmarPresenca(String idUnico) {
        return convidadoRepository.findByIdUnicoAndStatus(idUnico, "Não")
                .flatMap(convidado -> {
                    convidado.setStatus("Sim");
                    return convidadoRepository.save(convidado).thenReturn(true);
                }).defaultIfEmpty(false);
    }
}