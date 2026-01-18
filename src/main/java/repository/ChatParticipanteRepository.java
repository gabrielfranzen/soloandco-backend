package repository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import model.ChatParticipante;
import model.ChatSala;
import model.Checkin;
import model.Usuario;
import repository.base.AbstractCrudRepository;

@Stateless
public class ChatParticipanteRepository extends AbstractCrudRepository<ChatParticipante> {

    /**
     * Verifica se o usuário tem acesso válido (não expirado) à sala
     * @param usuarioId ID do usuário
     * @param salaId ID da sala
     * @return true se tem acesso válido
     */
    public boolean verificarAcessoValido(Integer usuarioId, Integer salaId) {
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(p) FROM ChatParticipante p " +
                    "WHERE p.usuario.id = :usuarioId " +
                    "AND p.sala.id = :salaId " +
                    "AND p.acessoExpiraEm > CURRENT_TIMESTAMP",
                    Long.class)
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("salaId", salaId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Busca o participante do usuário em uma sala (independente de expiração)
     * @param usuarioId ID do usuário
     * @param salaId ID da sala
     * @return Optional com o participante
     */
    public Optional<ChatParticipante> buscarParticipante(Integer usuarioId, Integer salaId) {
        try {
            ChatParticipante participante = em.createQuery(
                    "SELECT p FROM ChatParticipante p " +
                    "WHERE p.usuario.id = :usuarioId " +
                    "AND p.sala.id = :salaId",
                    ChatParticipante.class)
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("salaId", salaId)
                    .getSingleResult();
            return Optional.of(participante);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Adiciona ou atualiza a participação de um usuário na sala
     * Atualiza a data de expiração para check-in + 24 horas
     * @param sala Sala do chat
     * @param usuario Usuário
     * @param checkin Check-in que originou o acesso
     * @return Participante criado ou atualizado
     */
    public ChatParticipante adicionarOuAtualizarParticipante(ChatSala sala, Usuario usuario, Checkin checkin) {
        // Calcula data de expiração (24 horas após o check-in)
        Calendar cal = Calendar.getInstance();
        if (checkin.getCriadoEm() != null) {
            cal.setTime(checkin.getCriadoEm());
        }
        cal.add(Calendar.HOUR, 24);
        Date expiracao = cal.getTime();

        // Busca se já existe participante
        Optional<ChatParticipante> participanteOpt = buscarParticipante(usuario.getId(), sala.getId());

        if (participanteOpt.isPresent()) {
            // Atualiza a expiração
            ChatParticipante participante = participanteOpt.get();
            participante.setAcessoExpiraEm(expiracao);
            participante.setCheckin(checkin);
            atualizar(participante);
            return participante;
        } else {
            // Cria novo participante
            ChatParticipante participante = new ChatParticipante();
            participante.setSala(sala);
            participante.setUsuario(usuario);
            participante.setCheckin(checkin);
            participante.setAcessoExpiraEm(expiracao);
            return inserir(participante);
        }
    }

    /**
     * Lista participantes ativos (com acesso não expirado) de uma sala
     * @param salaId ID da sala
     * @return Lista de participantes ativos
     */
    public List<ChatParticipante> listarParticipantesAtivos(Integer salaId) {
        return em.createQuery(
                "SELECT p FROM ChatParticipante p " +
                "JOIN FETCH p.usuario u " +
                "WHERE p.sala.id = :salaId " +
                "AND p.acessoExpiraEm > CURRENT_TIMESTAMP " +
                "ORDER BY p.criadoEm DESC",
                ChatParticipante.class)
                .setParameter("salaId", salaId)
                .getResultList();
    }

    /**
     * Lista todas as salas que o usuário tem acesso válido
     * @param usuarioId ID do usuário
     * @return Lista de participações ativas
     */
    public List<ChatParticipante> listarSalasDoUsuario(Integer usuarioId) {
        return em.createQuery(
                "SELECT p FROM ChatParticipante p " +
                "JOIN FETCH p.sala " +
                "JOIN FETCH p.sala.estabelecimento " +
                "WHERE p.usuario.id = :usuarioId " +
                "AND p.acessoExpiraEm > CURRENT_TIMESTAMP " +
                "ORDER BY p.acessoExpiraEm DESC",
                ChatParticipante.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    /**
     * Conta o número de participantes ativos (com acesso não expirado) de uma sala
     * @param salaId ID da sala
     * @return Número de participantes ativos
     */
    public Integer contarParticipantesAtivos(Integer salaId) {
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(p) FROM ChatParticipante p " +
                    "WHERE p.sala.id = :salaId " +
                    "AND p.acessoExpiraEm > CURRENT_TIMESTAMP",
                    Long.class)
                    .setParameter("salaId", salaId)
                    .getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            return 0;
        }
    }
}

