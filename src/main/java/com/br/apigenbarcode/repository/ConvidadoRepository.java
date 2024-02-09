package com.br.apigenbarcode.repository;

import com.br.apigenbarcode.entity.Convidado;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ConvidadoRepository extends ReactiveCrudRepository<Convidado, Long> {
    Mono<Convidado> findByIdUnicoAndStatus(String idUnico, String status);
}