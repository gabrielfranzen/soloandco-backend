-- Script para apagar um estabelecimento pelo ID, respeitando as chaves estrangeiras.
--
-- Ordem de exclusão necessária:
--   1. checkin: não tem ON DELETE CASCADE vindo de estabelecimento, então é apagado manualmente.
--              Ao apagar checkin, o ON DELETE CASCADE em chat_participante(checkin_id) remove os participantes do chat automaticamente.
--   2. estabelecimento: o CASCADE cuida do restante:
--              chat_sala → chat_mensagem, chat_participante
--              evento → evento_link, evento_presenca
--
-- Substitua :id_estabelecimento pelo ID desejado antes de executar.

BEGIN;

DELETE FROM website.checkin
WHERE estabelecimento_id = :id_estabelecimento;

DELETE FROM website.estabelecimento
WHERE id = :id_estabelecimento;

COMMIT;
