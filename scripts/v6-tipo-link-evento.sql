-- V6 - Tipos de link de evento + entrada gratuita

-- Tabela de tipos de link
CREATE SEQUENCE IF NOT EXISTS website.seq_tipo_link_evento INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.tipo_link_evento (
    id      INTEGER      NOT NULL DEFAULT nextval('website.seq_tipo_link_evento'),
    codigo  VARCHAR(50)  NOT NULL,
    nome    VARCHAR(150) NOT NULL,
    CONSTRAINT pk_tipo_link_evento     PRIMARY KEY (id),
    CONSTRAINT uk_tipo_link_evento_cod UNIQUE (codigo)
);

ALTER SEQUENCE website.seq_tipo_link_evento OWNED BY website.tipo_link_evento.id;

-- Tipo inicial obrigatório
INSERT INTO website.tipo_link_evento (codigo, nome)
VALUES ('compra_de_ingresso', 'Compra de ingresso')
ON CONFLICT (codigo) DO NOTHING;

-- Adiciona referência de tipo em evento_link (nullable: links genéricos não precisam de tipo)
ALTER TABLE website.evento_link
    ADD COLUMN IF NOT EXISTS tipo_id INTEGER REFERENCES website.tipo_link_evento(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_evento_link_tipo ON website.evento_link(tipo_id);

-- Adiciona flag de entrada gratuita no evento (padrão TRUE para não quebrar dados existentes)
ALTER TABLE website.evento
    ADD COLUMN IF NOT EXISTS entrada_gratuita BOOLEAN NOT NULL DEFAULT TRUE;
