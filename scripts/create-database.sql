CREATE SCHEMA IF NOT EXISTS website;

-- Sequence para chave primária dos usuários
CREATE SEQUENCE IF NOT EXISTS website.seq_usuario INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

-- Tabela de usuários
CREATE TABLE IF NOT EXISTS website.usuario (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_usuario'),
    nome VARCHAR(100)  NOT NULL,
    email VARCHAR(150)  NOT NULL,
    telefone VARCHAR(20)   NOT NULL,
    senha VARCHAR(255)  NOT NULL,
    roles VARCHAR(100)  DEFAULT 'USER',
    refresh_token VARCHAR(255),
    data_cadastro TIMESTAMP DEFAULT NOW(),
    data_atualizacao TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_usuario_id PRIMARY KEY (id),
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT uq_usuario_telefone UNIQUE (telefone)
);

ALTER SEQUENCE website.seq_usuario OWNED BY website.usuario.id;

-- Sequence e tabela de estabelecimentos
CREATE SEQUENCE IF NOT EXISTS website.seq_estabelecimento INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.estabelecimento (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_estabelecimento'),
    nome VARCHAR(150) NOT NULL,
    latitude NUMERIC(10,6) NOT NULL,
    longitude NUMERIC(10,6) NOT NULL,
    endereco VARCHAR(255),
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_estabelecimento PRIMARY KEY (id)
);

ALTER SEQUENCE website.seq_estabelecimento OWNED BY website.estabelecimento.id;

-- Sequence e tabela de check-ins
CREATE SEQUENCE IF NOT EXISTS website.seq_checkin INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.checkin (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_checkin'),
    usuario_id INTEGER NOT NULL,
    estabelecimento_id INTEGER NOT NULL,
    distancia_m NUMERIC(10,2),
    criado_em TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_checkin PRIMARY KEY (id),
    CONSTRAINT fk_checkin_usuario FOREIGN KEY (usuario_id) REFERENCES website.usuario(id),
    CONSTRAINT fk_checkin_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES website.estabelecimento(id)
);

ALTER SEQUENCE website.seq_checkin OWNED BY website.checkin.id;