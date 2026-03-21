package repository;

import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import model.EventoPresenca;
import repository.base.AbstractCrudRepository;

@Stateless
public class EventoPresencaRepository extends AbstractCrudRepository<EventoPresenca> {

    public long contarPorEvento(Integer eventoId) {
        if (eventoId == null) return 0;
        Long count = em.createQuery(
                "SELECT COUNT(ep) FROM EventoPresenca ep WHERE ep.evento.id = :eventoId",
                Long.class)
                .setParameter("eventoId", eventoId)
                .getSingleResult();
        return count != null ? count : 0;
    }

    public Optional<EventoPresenca> buscarPorEventoEUsuario(Integer eventoId, Integer usuarioId) {
        if (eventoId == null || usuarioId == null) return Optional.empty();
        List<EventoPresenca> resultado = em.createQuery(
                "SELECT ep FROM EventoPresenca ep WHERE ep.evento.id = :eventoId AND ep.usuario.id = :usuarioId",
                EventoPresenca.class)
                .setParameter("eventoId", eventoId)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Transactional
    public void removerPorEventoEUsuario(Integer eventoId, Integer usuarioId) {
        em.createQuery(
                "DELETE FROM EventoPresenca ep WHERE ep.evento.id = :eventoId AND ep.usuario.id = :usuarioId")
                .setParameter("eventoId", eventoId)
                .setParameter("usuarioId", usuarioId)
                .executeUpdate();
    }
}
