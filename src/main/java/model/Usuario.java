package model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuario", schema = "website")
public class Usuario {

	@Id
	@SequenceGenerator(name = "usuario_seq", sequenceName = "website.seq_usuario", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
	@Column(name = "id", nullable = false)
	private Integer id;

	@NotBlank(message = "Nome é obrigatório")
	@Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
	@Column(name = "nome", length = 100, nullable = false)
	private String nome;

	@NotBlank(message = "Email é obrigatório")
	@Email(message = "Email deve ser válido")
	@Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
	@Column(name = "email", length = 150, nullable = false, unique = true)
	private String email;

	@NotBlank(message = "Telefone é obrigatório")
	@Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
	@Column(name = "telefone", length = 20, nullable = false, unique = true)
	private String telefone;

	@NotBlank(message = "Senha é obrigatória")
	@Size(max = 255, message = "Senha deve ter no máximo 255 caracteres")
	@Column(name = "senha", length = 255, nullable = false)
	private String senha;

	@Column(name = "roles", length = 100)
	private String roles;

	@JsonIgnore
	@Column(name = "refresh_token", length = 255)
	private String refreshToken;

	@Column(name = "data_cadastro")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataCadastro;

	@Column(name = "data_atualizacao")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataAtualizacao;

	@Column(name = "uuid_foto", length = 100)
	private String uuidFoto;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public String getUuidFoto() {
		return uuidFoto;
	}

	public void setUuidFoto(String uuidFoto) {
		this.uuidFoto = uuidFoto;
	}

	@PrePersist
	protected void aoCriar() {
		if (dataCadastro == null) {
			dataCadastro = new Date();
		}
		if (dataAtualizacao == null) {
			dataAtualizacao = new Date();
		}
		if (roles == null || roles.isBlank()) {
			roles = "USER";
		}
	}

	@PreUpdate
	protected void aoAtualizar() {
		dataAtualizacao = new Date();
	}

}