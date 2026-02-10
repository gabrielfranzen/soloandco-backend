package repository;

import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import model.Papel;
import model.Usuario;
import model.UsuarioPapel;
import repository.base.AbstractCrudRepository;

@Stateless
public class UsuarioPapelRepository extends AbstractCrudRepository<UsuarioPapel> {

    /**
     * Atribui um papel a um usuário se ainda não tiver
     */
    public UsuarioPapel atribuirPapel(Usuario usuario, Papel papel) {
        // Verificar se já existe
        Optional<UsuarioPapel> existente = buscarAtribuicao(usuario.getId(), papel.getId());
        if (existente.isPresent()) {
            return existente.get();
        }

        // Criar nova atribuição
        UsuarioPapel atribuicao = new UsuarioPapel();
        atribuicao.setUsuario(usuario);
        atribuicao.setPapel(papel);
        return inserir(atribuicao);
    }

    /**
     * Busca atribuição específica de papel para um usuário
     */
    public Optional<UsuarioPapel> buscarAtribuicao(Integer usuarioId, Integer papelId) {
        if (usuarioId == null || papelId == null) {
            return Optional.empty();
        }

        List<UsuarioPapel> lista = em.createQuery(
                "select up from UsuarioPapel up " +
                "join fetch up.papel " +
                "where up.usuario.id = :usuarioId and up.papel.id = :papelId",
                UsuarioPapel.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("papelId", papelId)
                .setMaxResults(1)
                .getResultList();

        return lista.stream().findFirst();
    }

    /**
     * Lista todos os papéis de um usuário
     */
    public List<UsuarioPapel> listarPorUsuario(Integer usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }

        return em.createQuery(
                "select up from UsuarioPapel up " +
                "join fetch up.papel " +
                "where up.usuario.id = :usuarioId " +
                "order by up.dataAtribuicao desc",
                UsuarioPapel.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    /**
     * Verifica se usuário possui determinado papel pelo código
     */
    public boolean usuarioTemPapel(Integer usuarioId, String codigoPapel) {
        if (usuarioId == null || codigoPapel == null || codigoPapel.isBlank()) {
            return false;
        }

        Long count = em.createQuery(
                "select count(up) from UsuarioPapel up " +
                "join up.papel p " +
                "where up.usuario.id = :usuarioId and p.codigo = :codigo",
                Long.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("codigo", codigoPapel)
                .getSingleResult();

        return count != null && count > 0;
    }

    /**
     * Remove um papel de um usuário
     */
    public void removerPapel(Integer usuarioId, Integer papelId) {
        if (usuarioId == null || papelId == null) {
            return;
        }

        em.createQuery(
                "delete from UsuarioPapel up " +
                "where up.usuario.id = :usuarioId and up.papel.id = :papelId")
                .setParameter("usuarioId", usuarioId)
                .setParameter("papelId", papelId)
                .executeUpdate();
    }

    /**
     * Retorna lista de códigos dos papéis do usuário separados por vírgula
     */
    public String obterPapeisComoString(Integer usuarioId) {
        if (usuarioId == null) {
            return "";
        }

        List<UsuarioPapel> papeis = listarPorUsuario(usuarioId);
        if (papeis == null || papeis.isEmpty()) {
            return "";
        }

        return papeis.stream()
                .filter(up -> up.getPapel() != null)
                .map(up -> up.getPapel().getCodigo())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}

