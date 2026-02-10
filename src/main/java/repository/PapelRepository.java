package repository;

import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import model.Papel;
import repository.base.AbstractCrudRepository;

@Stateless
public class PapelRepository extends AbstractCrudRepository<Papel> {

    public Optional<Papel> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        
        List<Papel> lista = em.createQuery(
                "select p from Papel p where p.codigo = :codigo", Papel.class)
                .setParameter("codigo", codigo)
                .setMaxResults(1)
                .getResultList();
        
        return lista.stream().findFirst();
    }

    public List<Papel> listar() {
        return em.createQuery("select p from Papel p order by p.nome", Papel.class)
                .getResultList();
    }
}

