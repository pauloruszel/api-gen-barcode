CREATE TABLE IF NOT EXISTS convidado (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    id_unico VARCHAR(255),
    status VARCHAR(255) DEFAULT 'Não',
    qr_code TEXT,
    dt_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    dt_atualizacao TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reception_event (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    revision BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reception_invite (
    event_id UUID NOT NULL REFERENCES reception_event(id),
    id UUID NOT NULL,
    label VARCHAR(160) NOT NULL,
    PRIMARY KEY (event_id, id),
    UNIQUE (event_id, label)
);

CREATE TABLE IF NOT EXISTS reception_guest (
    event_id UUID NOT NULL,
    id UUID NOT NULL,
    invite_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    rsvp VARCHAR(12) NOT NULL CHECK (rsvp IN ('UNKNOWN', 'YES', 'NO')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (event_id, id),
    FOREIGN KEY (event_id, invite_id) REFERENCES reception_invite(event_id, id)
);

CREATE TABLE IF NOT EXISTS reception_list_revision (
    event_id UUID NOT NULL REFERENCES reception_event(id),
    revision BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    additions INTEGER NOT NULL,
    updates INTEGER NOT NULL,
    removals INTEGER NOT NULL,
    PRIMARY KEY (event_id, revision)
);
