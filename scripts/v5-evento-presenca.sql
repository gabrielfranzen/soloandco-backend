-- V5 - Sistema de Intenção de Presença em Eventos

CREATE SEQUENCE IF NOT EXISTS website.seq_evento_presenca INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.evento_presenca (
    id          INTEGER   NOT NULL DEFAULT nextval('website.seq_evento_presenca'),
    evento_id   INTEGER   NOT NULL,
    usuario_id  INTEGER   NOT NULL,
    criado_em   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_evento_presenca    PRIMARY KEY (id),
    CONSTRAINT fk_ep_evento          FOREIGN KEY (evento_id)  REFERENCES website.evento(id)   ON DELETE CASCADE,
    CONSTRAINT fk_ep_usuario         FOREIGN KEY (usuario_id) REFERENCES website.usuario(id)  ON DELETE CASCADE,
    CONSTRAINT uk_ep_evento_usuario  UNIQUE (evento_id, usuario_id)
);

ALTER SEQUENCE website.seq_evento_presenca OWNED BY website.evento_presenca.id;

CREATE INDEX IF NOT EXISTS idx_evento_presenca_evento   ON website.evento_presenca(evento_id);
CREATE INDEX IF NOT EXISTS idx_evento_presenca_usuario  ON website.evento_presenca(usuario_id);
