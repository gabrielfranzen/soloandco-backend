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
}


