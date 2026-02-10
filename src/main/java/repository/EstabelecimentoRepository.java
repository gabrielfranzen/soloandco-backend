package repository;

import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import model.Estabelecimento;
import repository.base.AbstractCrudRepository;

@Stateless
public class EstabelecimentoRepository extends AbstractCrudRepository<Estabelecimento> {

    public List<Estabelecimento> listarAtivos() {
        return em.createQuery("select e from Estabelecimento e where e.ativo = true", Estabelecimento.class)
                .getResultList();
    }

    public Optional<Estabelecimento> buscarAtivoPorId(Integer id) {
        if (id == null) return Optional.empty();
        List<Estabelecimento> lista = em.createQuery(
                "select e from Estabelecimento e where e.id = :id and e.ativo = true", Estabelecimento.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();
        return lista.stream().findFirst();
    }

    public List<Estabelecimento> listarPorProprietario(Integer usuarioId) {
        if (usuarioId == null) return List.of();
        return em.createQuery(
                "select e from Estabelecimento e where e.proprietario.id = :usuarioId and e.ativo = true order by e.criadoEm desc", 
                Estabelecimento.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    public Long contarCheckinsPorEstabelecimento(Integer estabelecimentoId) {
        if (estabelecimentoId == null) return 0L;
        return em.createQuery(
                "select count(c) from Checkin c where c.estabelecimento.id = :estabelecimentoId", 
                Long.class)
                .setParameter("estabelecimentoId", estabelecimentoId)
                .getSingleResult();
    }
}



