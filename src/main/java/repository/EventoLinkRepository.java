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
                "SELECT el FROM EventoLink el LEFT JOIN FETCH el.tipo WHERE el.evento.id = :eventoId ORDER BY el.id ASC",
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

    @Transactional
    public void removerPorEventoETipoCodigo(Integer eventoId, String tipoCodigo) {
        if (eventoId == null || tipoCodigo == null) return;
        em.createQuery(
                "DELETE FROM EventoLink el WHERE el.evento.id = :eventoId AND el.tipo.codigo = :tipoCodigo")
                .setParameter("eventoId", eventoId)
                .setParameter("tipoCodigo", tipoCodigo)
                .executeUpdate();
    }

    public boolean existeLinkDoTipo(Integer eventoId, String tipoCodigo) {
        if (eventoId == null || tipoCodigo == null) return false;
        Long count = em.createQuery(
                "SELECT COUNT(el) FROM EventoLink el WHERE el.evento.id = :eventoId AND el.tipo.codigo = :tipoCodigo",
                Long.class)
                .setParameter("eventoId", eventoId)
                .setParameter("tipoCodigo", tipoCodigo)
                .getSingleResult();
        return count != null && count > 0;
    }
}
