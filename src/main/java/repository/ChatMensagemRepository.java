package repository;

import java.util.List;

import jakarta.ejb.Stateless;
import model.ChatMensagem;
import model.ChatSala;
import model.Usuario;
import repository.base.AbstractCrudRepository;
import utils.AesCryptoUtil;

@Stateless
public class ChatMensagemRepository extends AbstractCrudRepository<ChatMensagem> {

    /**
     * Lista as últimas mensagens de uma sala ordenadas por data (mais antigas primeiro)
     * @param salaId ID da sala
     * @param limit Número máximo de mensagens
     * @return Lista de mensagens (CRIPTOGRAFADAS - descriptografar ao usar)
     */
    public List<ChatMensagem> listarPorSala(Integer salaId, Integer limit) {
        return em.createQuery(
                "SELECT m FROM ChatMensagem m " +
                "JOIN FETCH m.usuario " +
                "WHERE m.sala.id = :salaId " +
                "ORDER BY m.criadoEm ASC",
                ChatMensagem.class)
                .setParameter("salaId", salaId)
                .setMaxResults(limit != null ? limit : 50)
                .getResultList();
    }

    /**
     * Insere uma mensagem criptografando automaticamente o conteúdo
     * @param sala Sala do chat
     * @param usuario Usuário que está enviando
     * @param mensagemPlain Mensagem em texto plano
     * @return Mensagem salva (com texto criptografado)
     * @throws Exception Se houver erro na criptografia
     */
    public ChatMensagem inserirMensagem(ChatSala sala, Usuario usuario, String mensagemPlain) throws Exception {
        if (mensagemPlain == null || mensagemPlain.trim().isEmpty()) {
            throw new IllegalArgumentException("Mensagem não pode ser vazia");
        }

        // Criptografa a mensagem antes de salvar
        String mensagemCriptografada = AesCryptoUtil.criptografar(mensagemPlain);

        ChatMensagem mensagem = new ChatMensagem();
        mensagem.setSala(sala);
        mensagem.setUsuario(usuario);
        mensagem.setMensagem(mensagemCriptografada);

        return inserir(mensagem);
    }

    /**
     * Descriptografa o conteúdo de uma mensagem
     * @param mensagemCriptografada Texto criptografado
     * @return Texto descriptografado
     * @throws Exception Se houver erro na descriptografia
     */
    public String descriptografar(String mensagemCriptografada) throws Exception {
        return AesCryptoUtil.descriptografar(mensagemCriptografada);
    }

    /**
     * Busca a última mensagem enviada em uma sala
     * @param salaId ID da sala
     * @return Última mensagem ou null se não houver mensagens
     */
    public ChatMensagem buscarUltimaMensagemDaSala(Integer salaId) {
        try {
            return em.createQuery(
                    "SELECT m FROM ChatMensagem m " +
                    "JOIN FETCH m.usuario " +
                    "WHERE m.sala.id = :salaId " +
                    "ORDER BY m.criadoEm DESC",
                    ChatMensagem.class)
                    .setParameter("salaId", salaId)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lista mensagens anteriores a uma mensagem específica (para paginação/scroll up)
     * @param salaId ID da sala
     * @param beforeId ID da mensagem de referência
     * @param limit Número máximo de mensagens
     * @return Lista de mensagens (CRIPTOGRAFADAS - descriptografar ao usar)
     */
    public List<ChatMensagem> listarMensagensAnteriores(Integer salaId, Integer beforeId, Integer limit) {
        return em.createQuery(
                "SELECT m FROM ChatMensagem m " +
                "JOIN FETCH m.usuario " +
                "WHERE m.sala.id = :salaId AND m.id < :beforeId " +
                "ORDER BY m.criadoEm DESC",
                ChatMensagem.class)
                .setParameter("salaId", salaId)
                .setParameter("beforeId", beforeId)
                .setMaxResults(limit != null ? limit : 20)
                .getResultList();
    }

    /**
     * Lista mensagens posteriores a uma mensagem específica (para Long Polling)
     * @param salaId ID da sala
     * @param afterId ID da mensagem de referência
     * @return Lista de mensagens (CRIPTOGRAFADAS - descriptografar ao usar)
     */
    public List<ChatMensagem> listarMensagensPosteriores(Integer salaId, Integer afterId) {
        return em.createQuery(
                "SELECT m FROM ChatMensagem m " +
                "JOIN FETCH m.usuario " +
                "WHERE m.sala.id = :salaId AND m.id > :afterId " +
                "ORDER BY m.criadoEm ASC",
                ChatMensagem.class)
                .setParameter("salaId", salaId)
                .setParameter("afterId", afterId)
                .getResultList();
    }

    /**
     * Lista as últimas N mensagens de uma sala (para carregamento inicial)
     * @param salaId ID da sala
     * @param limit Número máximo de mensagens
     * @return Lista de mensagens ordenadas do mais antigo para o mais novo (CRIPTOGRAFADAS)
     */
    public List<ChatMensagem> listarUltimasMensagens(Integer salaId, Integer limit) {
        List<ChatMensagem> mensagens = em.createQuery(
                "SELECT m FROM ChatMensagem m " +
                "JOIN FETCH m.usuario " +
                "WHERE m.sala.id = :salaId " +
                "ORDER BY m.criadoEm DESC",
                ChatMensagem.class)
                .setParameter("salaId", salaId)
                .setMaxResults(limit != null ? limit : 20)
                .getResultList();
        
        // Reverter para ordem cronológica (mais antigo primeiro)
        java.util.Collections.reverse(mensagens);
        return mensagens;
    }
}

