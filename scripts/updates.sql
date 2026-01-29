-- v1 -- 
-- Adiciona a nova coluna uuid_foto
ALTER TABLE website.usuario 
  ADD COLUMN IF NOT EXISTS uuid_foto VARCHAR(100);


-- v2 -- 
