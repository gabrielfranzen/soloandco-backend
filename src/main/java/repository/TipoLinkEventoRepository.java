package repository;

import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import model.TipoLinkEvento;
import repository.base.AbstractCrudRepository;

@Stateless
public class TipoLinkEventoRepository extends AbstractCrudRepository<TipoLinkEvento> {

    public Optional<TipoLinkEvento> buscarPorCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        List<TipoLinkEvento> resultado = em.createQuery(
                "SELECT t FROM TipoLinkEvento t WHERE t.codigo = :codigo",
                TipoLinkEvento.class)
                .setParameter("codigo", codigo)
                .getResultList();
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<TipoLinkEvento> listarTodos() {
        return em.createQuery("SELECT t FROM TipoLinkEvento t ORDER BY t.nome ASC", TipoLinkEvento.class)
                .getResultList();
    }
}
