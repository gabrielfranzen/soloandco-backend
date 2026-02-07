package model;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "token_recuperacao_senha", schema = "website")
public class TokenRecuperacaoSenha {

	@Id
	@SequenceGenerator(name = "token_recuperacao_senha_seq", sequenceName = "website.token_recuperacao_senha_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_recuperacao_senha_seq")
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "usuario_id", nullable = false)
	private Integer usuarioId;

	@Column(name = "token", length = 255, nullable = false, unique = true)
	private String token;

	@Column(name = "data_criacao", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataCriacao;

	@Column(name = "data_expiracao", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataExpiracao;

	@Column(name = "usado", nullable = false)
	private Boolean usado;

	@Column(name = "data_uso")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataUso;

	public TokenRecuperacaoSenha() {
		this.usado = false;
		this.dataCriacao = new Date();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Integer usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public Date getDataExpiracao() {
		return dataExpiracao;
	}

	public void setDataExpiracao(Date dataExpiracao) {
		this.dataExpiracao = dataExpiracao;
	}

	public Boolean getUsado() {
		return usado;
	}

	public void setUsado(Boolean usado) {
		this.usado = usado;
	}

	public Date getDataUso() {
		return dataUso;
	}

	public void setDataUso(Date dataUso) {
		this.dataUso = dataUso;
	}

	// Método auxiliar para verificar se o token está válido
	public boolean isValido() {
		if (usado) {
			return false;
		}
		Date agora = new Date();
		return agora.before(dataExpiracao);
	}
}

