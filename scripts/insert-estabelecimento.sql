-- Script para cadastrar um estabelecimento
-- Coordenadas: -28.025466, -48.623450
-- Estabelecimento: CAFECITO CO

INSERT INTO website.estabelecimento (nome, latitude, longitude, endereco, ativo)
VALUES (
    'CAFECITO CO',              -- Nome do estabelecimento
    -28.025466,                 -- Latitude
    -48.623450,                 -- Longitude
    'Rua 30 de Dezembro, Garopaba - SC', -- Endereço aproximado
    TRUE                         -- Ativo
);

-- Para verificar se foi inserido corretamente:
-- SELECT * FROM website.estabelecimento WHERE latitude = -28.025466 AND longitude = -48.623450;

