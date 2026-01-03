package repository;

import java.util.List;

import jakarta.ejb.Stateless;
import model.Checkin;
import model.Estabelecimento;
import model.Usuario;
import repository.base.AbstractCrudRepository;

@Stateless
public class CheckinRepository extends AbstractCrudRepository<Checkin> {

    public Checkin registrar(Usuario usuario, Estabelecimento estabelecimento, Double distanciaMetros) {
        Checkin checkin = new Checkin();
        checkin.setUsuario(usuario);
        checkin.setEstabelecimento(estabelecimento);
        checkin.setDistanciaMetros(distanciaMetros);
        inserir(checkin);
        return checkin;
    }

    public List<Checkin> listarPorEstabelecimento(Integer estabelecimentoId) {
        return em.createQuery(
                "select c from Checkin c "
                        + "join fetch c.usuario u "
                        + "join fetch c.estabelecimento e "
                        + "where e.id = :id "
                        + "order by c.criadoEm desc",
                Checkin.class)
                .setParameter("id", estabelecimentoId)
                .getResultList();
    }
}


