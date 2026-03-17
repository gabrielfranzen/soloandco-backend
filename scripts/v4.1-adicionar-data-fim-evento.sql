-- ============================================================
-- Migração v4.1 - Adicionar data_fim aos eventos
-- ============================================================
-- Esta migração atualiza a tabela evento para incluir data_fim
-- e renomeia a coluna 'data' para 'data_inicio'
-- ============================================================

BEGIN;

-- Verifica se a coluna 'data' existe antes de renomear
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'website' 
        AND table_name = 'evento' 
        AND column_name = 'data'
    ) THEN
        -- Renomeia coluna 'data' para 'data_inicio'
        ALTER TABLE website.evento RENAME COLUMN data TO data_inicio;
        RAISE NOTICE 'Coluna "data" renomeada para "data_inicio" com sucesso';
    ELSE
        RAISE NOTICE 'Coluna "data" não existe, provavelmente já foi renomeada';
    END IF;
END $$;

-- Adiciona coluna data_fim se não existir
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'website' 
        AND table_name = 'evento' 
        AND column_name = 'data_fim'
    ) THEN
        -- Adiciona a coluna data_fim
        ALTER TABLE website.evento ADD COLUMN data_fim DATE;
        
        -- Atualiza eventos existentes para terem data_fim igual a data_inicio
        UPDATE website.evento SET data_fim = data_inicio WHERE data_fim IS NULL;
        
        -- Torna a coluna NOT NULL
        ALTER TABLE website.evento ALTER COLUMN data_fim SET NOT NULL;
        
        RAISE NOTICE 'Coluna "data_fim" adicionada com sucesso';
    ELSE
        RAISE NOTICE 'Coluna "data_fim" já existe';
    END IF;
END $$;

-- Remove índice antigo 'idx_evento_data' se existir
DROP INDEX IF EXISTS website.idx_evento_data;

-- Cria novos índices se não existirem
CREATE INDEX IF NOT EXISTS idx_evento_data_inicio ON website.evento(data_inicio);
CREATE INDEX IF NOT EXISTS idx_evento_data_fim ON website.evento(data_fim);

COMMIT;
