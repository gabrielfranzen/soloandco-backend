package repository;

import java.util.Optional;

import jakarta.ejb.Stateless;
import model.ChatSala;
import model.Estabelecimento;
import repository.base.AbstractCrudRepository;

@Stateless
public class ChatSalaRepository extends AbstractCrudRepository<ChatSala> {

    /**
     * Busca uma sala de chat por estabelecimento
     * @param estabelecimentoId ID do estabelecimento
     * @return Optional com a sala se encontrada
     */
    public Optional<ChatSala> buscarPorEstabelecimento(Integer estabelecimentoId) {
        try {
            ChatSala sala = em.createQuery(
                    "SELECT s FROM ChatSala s " +
                    "WHERE s.estabelecimento.id = :estabelecimentoId",
                    ChatSala.class)
                    .setParameter("estabelecimentoId", estabelecimentoId)
                    .getSingleResult();
            return Optional.of(sala);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Cria uma nova sala de chat para um estabelecimento
     * @param estabelecimento Estabelecimento
     * @return Sala criada
     */
    public ChatSala criarParaEstabelecimento(Estabelecimento estabelecimento) {
        ChatSala sala = new ChatSala();
        sala.setEstabelecimento(estabelecimento);
        sala.setAtivo(true);
        return inserir(sala);
    }

    /**
     * Busca ou cria uma sala para o estabelecimento
     * @param estabelecimento Estabelecimento
     * @return Sala encontrada ou criada
     */
    public ChatSala buscarOuCriar(Estabelecimento estabelecimento) {
        Optional<ChatSala> salaOpt = buscarPorEstabelecimento(estabelecimento.getId());
        return salaOpt.orElseGet(() -> criarParaEstabelecimento(estabelecimento));
    }
}

