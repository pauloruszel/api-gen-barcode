CREATE TABLE IF NOT EXISTS convidado (
     id SERIAL PRIMARY KEY,
     nome VARCHAR(255) NOT NULL,
     email VARCHAR(255) NOT NULL,
     id_unico VARCHAR(255),
     status VARCHAR(255) DEFAULT 'Não',
     qr_code TEXT,
     dt_criacao TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
     dt_atualizacao TIMESTAMP WITHOUT TIME ZONE
);
