package com.br.apigenbarcode.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("convidado")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Convidado {

    @Id
    private Long id;

    @Column("nome")
    private String nome;

    @Column("email")
    private String email;

    @Column("id_unico")
    private String idUnico;

    @Column("status")
    private String status;

    @Column("qr_code")
    private String qrCode;
}