package com.br.apigenbarcode.entity;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("convidado")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Convidado {

    @Id
    private Long id;

    @NotNull
    @Column("nome")
    private String nome;

    @NotNull
    @Column("email")
    private String email;

    @Column("id_unico")
    private String idUnico;

    @Column("status")
    private String status;

    @Column("qr_code")
    private String qrCode;

    @Column("dt_criacao")
    private LocalDateTime dataCriacao;

    @Column("dt_atualizacao")
    private LocalDateTime dataAtualizacao;
}