package model.dto;

import java.util.Date;

public class ChatParticipanteResponse {
    
    private Integer id;
    private Integer usuarioId;
    private String usuarioNome;
    private String usuarioEmail;
    private Date acessoExpiraEm;
    private Date criadoEm;

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

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public Date getAcessoExpiraEm() {
        return acessoExpiraEm;
    }

    public void setAcessoExpiraEm(Date acessoExpiraEm) {
        this.acessoExpiraEm = acessoExpiraEm;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }
}

