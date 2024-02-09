CREATE TABLE IF NOT EXISTS convidado (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    id_unico VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    qr_code TEXT
    );