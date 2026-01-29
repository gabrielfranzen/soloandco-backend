package model.dto;

import java.util.Date;

public class UsuarioDTO {
    private Integer id;
    private String nome;
    private Date dataCadastro;
    private String email;
    private String roles;
    private String telefone;
    private String uuidFoto;

    // Construtor padrão
    public UsuarioDTO() {}

    // Construtor com parâmetros principais
    public UsuarioDTO(Integer id, String email, String nome) {
        this.id = id;
        this.email = email;
        this.nome = nome;
    }

    // Getters e Setters
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

    public Date getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }
    
    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getUuidFoto() {
        return uuidFoto;
    }

    public void setUuidFoto(String uuidFoto) {
        this.uuidFoto = uuidFoto;
    }
}
