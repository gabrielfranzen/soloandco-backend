-- Script de atualização V3 - Sistema de Papéis para Empresários (REFATORADO)
-- Executar este script para adicionar o sistema de papéis ao banco de dados existente

-- 1. Remover coluna papel_id se existir (era o esquema antigo)
ALTER TABLE website.usuario DROP COLUMN IF EXISTS papel_id;

-- 2. Criar sequence e tabela de papéis
CREATE SEQUENCE IF NOT EXISTS website.seq_papeis INCREMENT BY 1 MINVALUE 1 START WITH 1 NO CYCLE;

CREATE TABLE IF NOT EXISTS website.papeis (
    id INTEGER NOT NULL DEFAULT nextval('website.seq_papeis'),
    nome VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    criado_em TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_papeis PRIMARY KEY (id)
);

ALTER SEQUENCE website.seq_papeis OWNED BY website.papeis.id;

-- 3. Inserir papéis iniciais
-- IMPORTANTE: Os códigos devem corresponder às constantes em model.Papel:
--   - Papel.CODIGO_EMPRESARIO = "empresario"
--   - Papel.CODIGO_USUARIO = "usuario"

INSERT INTO website.papeis (id, nome, codigo)
SELECT nextval('website.seq_papeis'), 'Empresário', 'empresario'
WHERE NOT EXISTS (SELECT 1 FROM website.papeis WHERE codigo = 'empresario');

INSERT INTO website.papeis (id, nome, codigo)
SELECT nextval('website.seq_papeis'), 'Usuário', 'usuario'
WHERE NOT EXISTS (SELECT 1 FROM website.papeis WHERE codigo = 'usuario');

-- 4. Criar tabela de atribuição de papéis (N:N)
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

-- 5. Adicionar coluna usuario_proprietario_id na tabela estabelecimento
ALTER TABLE website.estabelecimento 
ADD COLUMN IF NOT EXISTS usuario_proprietario_id INTEGER REFERENCES website.usuario(id);

-- 6. Criar índices para otimização
CREATE INDEX IF NOT EXISTS idx_usuario_papel_usuario ON website.usuario_papel(usuario_id);
CREATE INDEX IF NOT EXISTS idx_usuario_papel_papel ON website.usuario_papel(papel_id);
CREATE INDEX IF NOT EXISTS idx_estabelecimento_proprietario ON website.estabelecimento(usuario_proprietario_id);

-- 7. Adicionar comentários
COMMENT ON TABLE website.papeis IS 'Tabela de papéis/perfis disponíveis no sistema';
COMMENT ON COLUMN website.papeis.codigo IS 'Código único do papel para uso na aplicação';
COMMENT ON TABLE website.usuario_papel IS 'Tabela de atribuição de papéis aos usuários (N:N)';
COMMENT ON COLUMN website.usuario_papel.data_atribuicao IS 'Data em que o papel foi atribuído ao usuário';
COMMENT ON COLUMN website.estabelecimento.usuario_proprietario_id IS 'Proprietário/empresário do estabelecimento';

-- 8. Atribuir papel padrão "usuario" para todos os usuários existentes
-- Usa Papel.CODIGO_USUARIO = "usuario"
INSERT INTO website.usuario_papel (usuario_id, papel_id)
SELECT u.id, p.id
FROM website.usuario u
CROSS JOIN website.papeis p
WHERE p.codigo = 'usuario'
AND NOT EXISTS (
    SELECT 1 FROM website.usuario_papel up
    WHERE up.usuario_id = u.id AND up.papel_id = p.id
);

-- 9. Mensagem de conclusão
DO $$
BEGIN
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Script V3 - Sistema de Papéis aplicado com sucesso!';
    RAISE NOTICE '================================================';
    RAISE NOTICE 'Tabela papeis criada com papéis: empresario, usuario';
    RAISE NOTICE 'Tabela usuario_papel criada (relacionamento N:N)';
    RAISE NOTICE 'Coluna usuario_proprietario_id adicionada em estabelecimento';
    RAISE NOTICE 'Índices criados para otimização';
    RAISE NOTICE 'Todos os usuários existentes receberam papel padrão "usuario"';
    RAISE NOTICE '================================================';
END $$;


