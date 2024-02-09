package com.br.apigenbarcode.controller;

import com.br.apigenbarcode.entity.Convidado;
import com.br.apigenbarcode.service.ConvidadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/convidados")
public class ConvidadoController {

    @Autowired
    private ConvidadoService convidadoService;

    @PostMapping("/cadastrar")
    public Mono<ResponseEntity<Convidado>> cadastrarConvidado(@RequestBody Convidado convidado) {
        return convidadoService.cadastrarConvidado(convidado.getNome(), convidado.getEmail())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(badRequest().build());
    }

    @GetMapping("/confirmar/{idUnico}")
    public Mono<ResponseEntity<Object>> confirmarPresenca(@PathVariable String idUnico) {
        return convidadoService.confirmarPresenca(idUnico)
                .map(confirmado -> {
                    if (confirmado) {
                        final var responseMessage = Map.of("mensagem", "Presença confirmada com sucesso!");
                        return ok().body(responseMessage);
                    } else {
                        final var responseMessage = Map.of("mensagem", "Convidado não encontrado ou já confirmado.");
                        return status(NOT_FOUND).body(responseMessage);
                    }
                });
    }

}