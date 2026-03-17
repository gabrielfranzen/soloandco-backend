-- V4 - Sistema de Eventos para Estabelecimentos

-- Sequence e tabela de eventos
CREATE SEQUENCE IF NOT EXISTS website.seq_evento INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.evento (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_evento'),
    estabelecimento_id INTEGER NOT NULL,
    nome VARCHAR(200) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    horario_inicio TIME NOT NULL,
    horario_fim TIME NOT NULL,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_evento PRIMARY KEY (id),
    CONSTRAINT fk_evento_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES website.estabelecimento(id) ON DELETE CASCADE
);

ALTER SEQUENCE website.seq_evento OWNED BY website.evento.id;

-- Sequence e tabela de links de eventos
CREATE SEQUENCE IF NOT EXISTS website.seq_evento_link INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.evento_link (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_evento_link'),
    evento_id INTEGER NOT NULL,
    titulo VARCHAR(150),
    url VARCHAR(500) NOT NULL,
    criado_em TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_evento_link PRIMARY KEY (id),
    CONSTRAINT fk_evento_link_evento FOREIGN KEY (evento_id) REFERENCES website.evento(id) ON DELETE CASCADE
);

ALTER SEQUENCE website.seq_evento_link OWNED BY website.evento_link.id;

-- Indices para otimizacao
CREATE INDEX IF NOT EXISTS idx_evento_estabelecimento ON website.evento(estabelecimento_id);
CREATE INDEX IF NOT EXISTS idx_evento_data ON website.evento(data);
CREATE INDEX IF NOT EXISTS idx_evento_ativo ON website.evento(ativo);
CREATE INDEX IF NOT EXISTS idx_evento_link_evento ON website.evento_link(evento_id);
