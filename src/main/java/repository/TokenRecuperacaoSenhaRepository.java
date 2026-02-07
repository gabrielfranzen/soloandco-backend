package repository;

import java.util.Date;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import model.TokenRecuperacaoSenha;

@Stateless
public class TokenRecuperacaoSenhaRepository {

	@PersistenceContext(unitName = "soloandco")
	private EntityManager em;

	// Cria um novo token de recuperação
	public TokenRecuperacaoSenha criarToken(Integer usuarioId, String token, Date dataExpiracao) {
		TokenRecuperacaoSenha tokenRecuperacao = new TokenRecuperacaoSenha();
		tokenRecuperacao.setUsuarioId(usuarioId);
		tokenRecuperacao.setToken(token);
		tokenRecuperacao.setDataCriacao(new Date());
		tokenRecuperacao.setDataExpiracao(dataExpiracao);
		tokenRecuperacao.setUsado(false);

		em.persist(tokenRecuperacao);
		return tokenRecuperacao;
	}

	// Busca um token válido (não usado e não expirado)
	public TokenRecuperacaoSenha buscarPorToken(String token) {
		try {
			TypedQuery<TokenRecuperacaoSenha> query = em.createQuery(
				"SELECT t FROM TokenRecuperacaoSenha t WHERE t.token = :token",
				TokenRecuperacaoSenha.class
			);
			query.setParameter("token", token);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	// Marca o token como usado
	public void marcarComoUsado(String token) {
		TokenRecuperacaoSenha tokenRecuperacao = buscarPorToken(token);
		if (tokenRecuperacao != null) {
			tokenRecuperacao.setUsado(true);
			tokenRecuperacao.setDataUso(new Date());
			em.merge(tokenRecuperacao);
		}
	}

	// Invalida todos os tokens anteriores do usuário (marca como usado)
	public void invalidarTokensAntigos(Integer usuarioId) {
		em.createQuery(
			"UPDATE TokenRecuperacaoSenha t SET t.usado = true, t.dataUso = :dataUso " +
			"WHERE t.usuarioId = :usuarioId AND t.usado = false"
		)
		.setParameter("usuarioId", usuarioId)
		.setParameter("dataUso", new Date())
		.executeUpdate();
	}

	// Limpa tokens expirados do banco (útil para manutenção)
	public int limparTokensExpirados() {
		return em.createQuery(
			"DELETE FROM TokenRecuperacaoSenha t WHERE t.dataExpiracao < :agora"
		)
		.setParameter("agora", new Date())
		.executeUpdate();
	}

	// Busca todos os tokens de um usuário (útil para debug/admin)
	public List<TokenRecuperacaoSenha> buscarPorUsuario(Integer usuarioId) {
		TypedQuery<TokenRecuperacaoSenha> query = em.createQuery(
			"SELECT t FROM TokenRecuperacaoSenha t WHERE t.usuarioId = :usuarioId ORDER BY t.dataCriacao DESC",
			TokenRecuperacaoSenha.class
		);
		query.setParameter("usuarioId", usuarioId);
		return query.getResultList();
	}

	// Verifica se o token é válido (não usado e não expirado)
	public boolean isTokenValido(String token) {
		TokenRecuperacaoSenha tokenRecuperacao = buscarPorToken(token);
		return tokenRecuperacao != null && tokenRecuperacao.isValido();
	}
}

