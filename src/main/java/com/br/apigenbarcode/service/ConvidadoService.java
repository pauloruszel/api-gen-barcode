package com.br.apigenbarcode.service;

import com.br.apigenbarcode.entity.Convidado;
import com.br.apigenbarcode.repository.ConvidadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.Boolean.TRUE;

@Slf4j
@Service
public class ConvidadoService {

    @Autowired
    private ConvidadoRepository convidadoRepository;

    @Autowired
    private QRCodeService qrCodeService;

    private final static String STATUS_NAO = "Não";

    private void validarDadosConvidado(final String nome, final String email) {
        log.info("inicio da validacao dos dados de convidado: nome: {} email: {}", nome, email);

        if (StringUtils.isBlank(nome)) {
            log.error("validacao falhou: O nome do convidado é necessario mas não foi fornecido.");
            throw new IllegalArgumentException("O nome do convidado não pode ser nulo ou vazio.");
        } else {
            log.info("o nome do convidado '{}' passou na validacao: não é nulo e não está vazio.", nome);
        }

        if (email == null || !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}")) {
            log.error("validacao falhou: O email fornecido '{}' é invalido.", email);
            throw new IllegalArgumentException("O email do convidado é nulo ou invalido.");
        } else {
            log.info("O email do convidado '{}' passou na validacao: é um email valido.", email);
        }
        log.info("fim da validacao dos dados de convidado: nome: {} email: {}", nome, email);
    }

    public Mono<Convidado> cadastrarConvidado(final String nome, final String email) {
        validarDadosConvidado(nome, email);

        log.info("iniciando o cadastro do convidado com nome: {} e email: {}", nome, email);
        final var idUnico = gerarIdUnico();

        final var convidado = Convidado.builder()
                .nome(nome)
                .email(email)
                .idUnico(idUnico)
                .dataCriacao(LocalDateTime.now()).build();

        return gerarQrCodeBase64(idUnico)
                .flatMap(qrCodeBase64 -> {
                    convidado.setQrCode(qrCodeBase64);
                    log.info("convidado {} cadastrado com sucesso. QR Code gerado e atribuido.", nome);
                    return convidadoRepository.save(convidado);
                }).doOnSuccess(c -> log.info("cadastro concluido para o convidado com nome: {}", nome))
                .doOnError(e -> log.error("erro ao cadastrar convidado: {}", e.getMessage()));
    }

    private String gerarIdUnico() {
        return UUID.randomUUID().toString();
    }

    private Mono<String> gerarQrCodeBase64(final String idUnico) {
        log.info("iniciando a geracao do QR Code para o ID unico: {}", idUnico);
        final var scale = 200; // Tamanho do QR Code
        final var foreground = "#000000"; // Cor do QR Code
        final var background = "#FFFFFF"; // Cor de fundo
        return qrCodeService.gerarQRCodeReativo(idUnico, scale, foreground, background)
                .map(qrCode -> qrCodeService.encodeBase64(qrCode))
                .doOnSuccess(qrCode -> log.info("QR Code gerado com sucesso para o ID unico: {}", idUnico))
                .doOnError(e -> log.error("erro ao gerar QR Code: {}", e.getMessage()));
    }

    public Mono<Boolean> confirmarPresenca(final String idUnico) {
        log.info("iniciando a confirmacao de presença para o ID unico: {}", idUnico);
        return convidadoRepository.findByIdUnicoAndStatus(idUnico, STATUS_NAO)
                .flatMap(convidado -> {
                    convidado.setStatus("Sim");
                    convidado.setDataAtualizacao(LocalDateTime.now());
                    return convidadoRepository.save(convidado).thenReturn(true);
                }).switchIfEmpty(Mono.just(false))
                .doOnSuccess(confirmado -> {
                    if (TRUE.equals(confirmado)) {
                        log.info("confirmacao de presenca concluida com sucesso para o ID unico: {}", idUnico);
                    } else {
                        log.warn("convidado com ID unico: {} nao encontrado ou ja confirmado.", idUnico);
                    }
                })
                .doOnError(e -> log.error("erro ao confirmar presenca: {}", e.getMessage()));
    }

}