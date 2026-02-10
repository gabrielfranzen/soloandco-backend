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

-- Sequence e tabela de salas de chat
CREATE SEQUENCE IF NOT EXISTS website.seq_chat_sala INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.chat_sala (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_chat_sala'),
    estabelecimento_id INTEGER NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_chat_sala PRIMARY KEY (id),
    CONSTRAINT fk_chat_sala_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES website.estabelecimento(id) ON DELETE CASCADE,
    CONSTRAINT uq_chat_sala_estabelecimento UNIQUE (estabelecimento_id)
);

ALTER SEQUENCE website.seq_chat_sala OWNED BY website.chat_sala.id;

-- Sequence e tabela de mensagens do chat
CREATE SEQUENCE IF NOT EXISTS website.seq_chat_mensagem INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.chat_mensagem (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_chat_mensagem'),
    sala_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    mensagem VARCHAR(1000) NOT NULL,
    criado_em TIMESTAMP DEFAULT NOW(),
    editado_em TIMESTAMP,
    CONSTRAINT pk_chat_mensagem PRIMARY KEY (id),
    CONSTRAINT fk_chat_mensagem_sala FOREIGN KEY (sala_id) REFERENCES website.chat_sala(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_mensagem_usuario FOREIGN KEY (usuario_id) REFERENCES website.usuario(id) ON DELETE CASCADE,
    CONSTRAINT ck_chat_mensagem_nao_vazia CHECK (LENGTH(TRIM(mensagem)) > 0)
);

ALTER SEQUENCE website.seq_chat_mensagem OWNED BY website.chat_mensagem.id;

-- Sequence e tabela de participantes do chat
CREATE SEQUENCE IF NOT EXISTS website.seq_chat_participante INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.chat_participante (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_chat_participante'),
    sala_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    checkin_id INTEGER NOT NULL,
    acesso_expira_em TIMESTAMP NOT NULL,
    criado_em TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_chat_participante PRIMARY KEY (id),
    CONSTRAINT fk_chat_participante_sala FOREIGN KEY (sala_id) REFERENCES website.chat_sala(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_participante_usuario FOREIGN KEY (usuario_id) REFERENCES website.usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_participante_checkin FOREIGN KEY (checkin_id) REFERENCES website.checkin(id) ON DELETE CASCADE,
    CONSTRAINT uq_chat_participante_usuario_sala UNIQUE (usuario_id, sala_id)
);

ALTER SEQUENCE website.seq_chat_participante OWNED BY website.chat_participante.id;

-- Índices para otimização de performance do chat

-- Índice para buscar sala por estabelecimento
CREATE INDEX IF NOT EXISTS idx_chat_sala_estabelecimento ON website.chat_sala(estabelecimento_id);

-- Índice para listar mensagens por sala
CREATE INDEX IF NOT EXISTS idx_chat_mensagem_sala ON website.chat_mensagem(sala_id);

-- Índice para ordenação temporal das mensagens
CREATE INDEX IF NOT EXISTS idx_chat_mensagem_criado_em ON website.chat_mensagem(criado_em DESC);

-- Índice composto para buscar mensagens de uma sala ordenadas por data
CREATE INDEX IF NOT EXISTS idx_chat_mensagem_sala_criado ON website.chat_mensagem(sala_id, criado_em DESC);

-- Índice composto para verificar acesso do usuário à sala
CREATE INDEX IF NOT EXISTS idx_chat_participante_usuario_sala_expira ON website.chat_participante(usuario_id, sala_id, acesso_expira_em);

-- Índice para buscar participantes por sala
CREATE INDEX IF NOT EXISTS idx_chat_participante_sala ON website.chat_participante(sala_id);

-- Índice para buscar participantes por usuário
CREATE INDEX IF NOT EXISTS idx_chat_participante_usuario ON website.chat_participante(usuario_id);

-- Índice para limpar acessos expirados
CREATE INDEX IF NOT EXISTS idx_chat_participante_expira_em ON website.chat_participante(acesso_expira_em);

----------------------------------------------------------------------------------------------------------------------

-- V2
CREATE TABLE IF NOT EXISTS website.token_recuperacao_senha (
  id SERIAL PRIMARY KEY,
  usuario_id INTEGER NOT NULL REFERENCES website.usuario(id) ON DELETE CASCADE,
  token VARCHAR(255) NOT NULL UNIQUE,
  data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_expiracao TIMESTAMP NOT NULL,
  usado BOOLEAN NOT NULL DEFAULT FALSE,
  data_uso TIMESTAMP,
  CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES website.usuario(id)
);

-- Índices para melhorar performance nas consultas
CREATE INDEX IF NOT EXISTS idx_token_recuperacao_token ON website.token_recuperacao_senha(token);
CREATE INDEX IF NOT EXISTS idx_token_recuperacao_usuario ON website.token_recuperacao_senha(usuario_id);

-- Comentários nas colunas
COMMENT ON TABLE website.token_recuperacao_senha IS 'Tokens de recuperação de senha com validade de 30 minutos';
COMMENT ON COLUMN website.token_recuperacao_senha.token IS 'Token UUID único para recuperação';
COMMENT ON COLUMN website.token_recuperacao_senha.usado IS 'Indica se o token já foi utilizado';
COMMENT ON COLUMN website.token_recuperacao_senha.data_expiracao IS 'Data e hora de expiração do token';

----------------------------------------------------------------------------------------------------------------------

-- V3 - Sistema de Papéis
-- Sequence e tabela de papéis
CREATE SEQUENCE IF NOT EXISTS website.seq_papeis INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.papeis (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_papeis'),
    nome VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    criado_em TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_papeis PRIMARY KEY (id)
);

ALTER SEQUENCE website.seq_papeis OWNED BY website.papeis.id;

-- Inserir papéis iniciais
-- IMPORTANTE: Os códigos devem corresponder às constantes em model.Papel:
--   - Papel.CODIGO_EMPRESARIO = "empresario"
--   - Papel.CODIGO_USUARIO = "usuario"

INSERT INTO website.papeis (id, nome, codigo)
SELECT nextval('website.seq_papeis'), 'Empresário', 'empresario'
WHERE NOT EXISTS (SELECT 1 FROM website.papeis WHERE codigo = 'empresario');

INSERT INTO website.papeis (id, nome, codigo)
SELECT nextval('website.seq_papeis'), 'Usuário', 'usuario'
WHERE NOT EXISTS (SELECT 1 FROM website.papeis WHERE codigo = 'usuario');

-- Tabela de atribuição de papéis aos usuários (N:N)
CREATE SEQUENCE IF NOT EXISTS website.seq_usuario_papel INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.usuario_papel (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_usuario_papel'),
    usuario_id INTEGER NOT NULL,
    papel_id INTEGER NOT NULL,
    data_atribuicao TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_usuario_papel PRIMARY KEY (id),
    CONSTRAINT fk_usuario_papel_usuario FOREIGN KEY (usuario_id) REFERENCES website.usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_papel_papel FOREIGN KEY (papel_id) REFERENCES website.papeis(id) ON DELETE CASCADE,
    CONSTRAINT uq_usuario_papel UNIQUE (usuario_id, papel_id)
);

ALTER SEQUENCE website.seq_usuario_papel OWNED BY website.usuario_papel.id;

-- Adicionar coluna usuario_proprietario_id na tabela estabelecimento
ALTER TABLE website.estabelecimento 
ADD COLUMN IF NOT EXISTS usuario_proprietario_id INTEGER REFERENCES website.usuario(id);

-- Índices para otimização
CREATE INDEX IF NOT EXISTS idx_usuario_papel_usuario ON website.usuario_papel(usuario_id);
CREATE INDEX IF NOT EXISTS idx_usuario_papel_papel ON website.usuario_papel(papel_id);
CREATE INDEX IF NOT EXISTS idx_estabelecimento_proprietario ON website.estabelecimento(usuario_proprietario_id);
