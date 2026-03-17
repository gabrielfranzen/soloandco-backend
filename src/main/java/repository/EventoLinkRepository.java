package repository;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import model.EventoLink;
import repository.base.AbstractCrudRepository;

@Stateless
public class EventoLinkRepository extends AbstractCrudRepository<EventoLink> {

    public List<EventoLink> listarPorEvento(Integer eventoId) {
        if (eventoId == null) return List.of();
        return em.createQuery(
                "SELECT el FROM EventoLink el WHERE el.evento.id = :eventoId ORDER BY el.id ASC",
                EventoLink.class)
                .setParameter("eventoId", eventoId)
                .getResultList();
    }

    @Transactional
    public void removerPorEvento(Integer eventoId) {
        if (eventoId == null) return;
        em.createQuery("DELETE FROM EventoLink el WHERE el.evento.id = :eventoId")
                .setParameter("eventoId", eventoId)
                .executeUpdate();
    }
}
