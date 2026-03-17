package repository;

import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import model.Evento;
import repository.base.AbstractCrudRepository;

@Stateless
public class EventoRepository extends AbstractCrudRepository<Evento> {

    public List<Evento> listarPorEstabelecimento(Integer estabelecimentoId) {
        if (estabelecimentoId == null) return List.of();
        return em.createQuery(
                "SELECT e FROM Evento e WHERE e.estabelecimento.id = :estabelecimentoId ORDER BY e.dataInicio DESC, e.horarioInicio ASC",
                Evento.class)
                .setParameter("estabelecimentoId", estabelecimentoId)
                .getResultList();
    }

    public List<Evento> listarAtivosPorEstabelecimento(Integer estabelecimentoId) {
        if (estabelecimentoId == null) return List.of();
        return em.createQuery(
                "SELECT e FROM Evento e WHERE e.estabelecimento.id = :estabelecimentoId AND e.ativo = true ORDER BY e.dataInicio ASC, e.horarioInicio ASC",
                Evento.class)
                .setParameter("estabelecimentoId", estabelecimentoId)
                .getResultList();
    }

    public Optional<Evento> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        Evento evento = em.find(Evento.class, id);
        return Optional.ofNullable(evento);
    }
}
